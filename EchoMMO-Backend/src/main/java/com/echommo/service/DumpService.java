package com.echommo.service;

import com.echommo.dto.DumpRequest;
import com.echommo.dto.DumpResponse;
import com.echommo.entity.*;
import com.echommo.entity.Character;
import com.echommo.enums.Rarity;
import com.echommo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DumpService {

    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;
    private final UserItemRepository userItemRepository;
    private final WalletRepository walletRepository;
    private final ItemRepository itemRepository;

    private final Random random = new Random();

    @Transactional
    public DumpResponse processDump(String username, DumpRequest request) {
        long startTime = System.currentTimeMillis(); // Benchmark log

        // 1. Validate User & Character
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Character character = characterRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        int amount = request.getAmount();
        if (amount <= 0) throw new RuntimeException("Số lượng không hợp lệ");

        // 2. Xác định loại cá và tỷ lệ
        String inputItemCode;
        double megalodonRate;

        String type = request.getType().toUpperCase();
        switch (type) {
            case "NORMAL":
                inputItemCode = "f_fish";
                megalodonRate = 0.1;
                break;
            case "SHARK":
                inputItemCode = "f_shark";
                megalodonRate = 2.5;
                break;
            case "WHITE_SHARK":
                inputItemCode = "f_whiteshark";
                megalodonRate = 10.0;
                break;
            default:
                throw new RuntimeException("Loại cá không hợp lệ!");
        }

        // 3. Trừ item trong kho (Batch update logic)
        Item inputItem = itemRepository.findByCode(inputItemCode)
                .orElseThrow(() -> new RuntimeException("Item code " + inputItemCode + " không tồn tại"));

        UserItem userItem = userItemRepository.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), inputItem.getItemId())
                .orElseThrow(() -> new RuntimeException("Bạn không sở hữu " + inputItem.getName()));

        if (userItem.getQuantity() < amount) {
            throw new RuntimeException("Không đủ số lượng " + inputItem.getName() + " để thả!");
        }

        userItem.setQuantity(userItem.getQuantity() - amount);
        if (userItem.getQuantity() <= 0) {
            userItemRepository.delete(userItem);
        } else {
            userItemRepository.save(userItem);
        }

        // 4. PRE-FETCH DATA (Lấy hết item cần thiết ra trước để không query trong loop)
        Item megalodonItem = itemRepository.findByCode("f_megalodon").orElse(null);
        List<Item> commonItems = getCachedItemsByRarity(Rarity.COMMON);
        List<Item> rareItems = getCachedItemsByRarity(Rarity.RARE);
        List<Item> epicItems = getCachedItemsByRarity(Rarity.EPIC);

        // 5. Tính toán phần thưởng (In-Memory Loop - Cực nhanh)
        BigDecimal totalGold = BigDecimal.ZERO;
        BigDecimal totalEcho = BigDecimal.ZERO;

        // Map dùng để gom số lượng item (VD: "w_wood" -> 50 cái)
        Map<String, Integer> accumulatedItems = new HashMap<>();

        for (int i = 0; i < amount; i++) {
            // Check Megalodon
            if (megalodonItem != null && (random.nextDouble() * 100) < megalodonRate) {
                accumulatedItems.merge(megalodonItem.getCode(), 1, Integer::sum);
                continue;
            }

            // Check bảng thường
            int standardRoll = random.nextInt(100);
            if (standardRoll < 70) {
                // 70% Vàng
                totalGold = totalGold.add(BigDecimal.valueOf(random.nextInt(11) + 10));
            } else if (standardRoll < 90) {
                // 20% Rác
                Item junk = getRandomItemFromList(commonItems);
                if (junk != null) accumulatedItems.merge(junk.getCode(), 1, Integer::sum);
                else totalGold = totalGold.add(BigDecimal.valueOf(5));
            } else if (standardRoll < 99) {
                // 9% Hiếm
                List<Item> pool = random.nextBoolean() ? rareItems : epicItems;
                Item rare = getRandomItemFromList(pool);
                if (rare != null) accumulatedItems.merge(rare.getCode(), 1, Integer::sum);
                else totalGold = totalGold.add(BigDecimal.valueOf(50));
            } else {
                // 1% Echo
                double echoVal = 0.001 + (0.499 * random.nextDouble());
                totalEcho = totalEcho.add(BigDecimal.valueOf(echoVal));
            }
        }

        // 6. Ghi xuống DB (Batch Update Items)
        // Chỉ lặp qua danh sách các item ĐÃ nhận được (số vòng lặp rất ít)
        List<DumpResponse.RewardItem> responseItems = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : accumulatedItems.entrySet()) {
            String itemCode = entry.getKey();
            int qty = entry.getValue();

            // Tìm item entity (Có thể cache map code->entity để nhanh hơn nếu cần)
            Item item = itemRepository.findByCode(itemCode).orElse(null);
            if (item == null) continue;

            // Update kho
            Optional<UserItem> existing = userItemRepository.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), item.getItemId());
            if (existing.isPresent()) {
                UserItem ui = existing.get();
                ui.setQuantity(ui.getQuantity() + qty);
                userItemRepository.save(ui);
            } else {
                UserItem newItem = new UserItem();
                newItem.setCharacter(character);
                newItem.setItem(item);
                newItem.setQuantity(qty);
                newItem.setIsEquipped(false);
                userItemRepository.save(newItem);
            }

            // Add vào response
            responseItems.add(DumpResponse.RewardItem.builder()
                    .code(item.getCode())
                    .name(item.getName())
                    .rarity(item.getRarity())
                    .quantity(qty)
                    .build());
        }

        // 7. Update Wallet (Một lần duy nhất)
        if (totalGold.compareTo(BigDecimal.ZERO) > 0 || totalEcho.compareTo(BigDecimal.ZERO) > 0) {
            Wallet wallet = walletRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Wallet not found"));

            if (totalGold.compareTo(BigDecimal.ZERO) > 0)
                wallet.setGold(wallet.getGold().add(totalGold));
            if (totalEcho.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal current = wallet.getEchoCoin() != null ? wallet.getEchoCoin() : BigDecimal.ZERO;
                wallet.setEchoCoin(current.add(totalEcho));
            }
            walletRepository.save(wallet);
        }

        System.out.println("Dump processed " + amount + " items in: " + (System.currentTimeMillis() - startTime) + "ms");

        return DumpResponse.builder()
                .success(true)
                .message("Đã thả " + amount + " " + inputItem.getName())
                .totalGold(totalGold)
                .totalEcho(totalEcho)
                .receivedItems(responseItems)
                .build();
    }

    // --- Helper Optimized ---
    private List<Item> getCachedItemsByRarity(Rarity rarity) {
        List<Item> items = itemRepository.findByRarity(rarity);
        if (items == null) return new ArrayList<>();
        // Lọc bỏ cá ngay từ đầu
        return items.stream()
                .filter(i -> !i.getCode().startsWith("f_"))
                .collect(Collectors.toList());
    }

    private Item getRandomItemFromList(List<Item> items) {
        if (items == null || items.isEmpty()) return null;
        return items.get(random.nextInt(items.size()));
    }
}