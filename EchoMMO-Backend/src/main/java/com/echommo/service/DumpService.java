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
        // 1. Validate User & Character
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Character character = characterRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        int amount = request.getAmount();
        if (amount <= 0) throw new RuntimeException("Số lượng không hợp lệ");

        // 2. Xác định loại cá đầu vào và Tỷ lệ ra Megalodon
        String inputItemCode;
        double megalodonRate; // Tỷ lệ phần trăm (0.0 - 100.0)

        String type = request.getType().toUpperCase();
        switch (type) {
            case "NORMAL": // Cá thường
                inputItemCode = "f_fish";
                megalodonRate = 0.1; // 0.1% cơ hội
                break;
            case "SHARK": // Cá mập
                inputItemCode = "f_shark";
                megalodonRate = 2.5; // 2.5% cơ hội
                break;
            case "WHITE_SHARK": // Cá mập trắng (Hàng xịn)
                inputItemCode = "f_whiteshark";
                megalodonRate = 10.0; // 10% cơ hội
                break;
            default:
                throw new RuntimeException("Loại cá không hợp lệ!");
        }

        // 3. Trừ item trong kho
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

        // 4. Tính toán phần thưởng (Gacha Loop)
        BigDecimal totalGold = BigDecimal.ZERO;
        BigDecimal totalEcho = BigDecimal.ZERO;
        Map<String, DumpResponse.RewardItem> rewardMap = new HashMap<>();

        Item megalodonItem = itemRepository.findByCode("f_megalodon").orElse(null);

        for (int i = 0; i < amount; i++) {
            // --- LỚP 1: CHECK MEGALODON (Dựa trên loại mồi) ---
            double rollMegalodon = random.nextDouble() * 100; // 0.0 - 100.0

            if (rollMegalodon < megalodonRate && megalodonItem != null) {
                // Trúng Jackpot: Megalodon
                addItemToRewardMap(rewardMap, megalodonItem);
                giveItemToUser(character, megalodonItem);
                continue; // Xong lượt này, qua con tiếp theo
            }

            // --- LỚP 2: CHECK BẢNG THƯỞNG CỐ ĐỊNH ---
            // Nếu không ra Megalodon thì chạy bảng tỷ lệ:
            // 70% Vàng | 20% Rác | 9% Hiếm | 1% Echo
            int standardRoll = random.nextInt(100); // 0 - 99

            if (standardRoll < 70) {
                // [0-69] 70% ra Vàng (10 - 20) -> Cộng Ví
                totalGold = totalGold.add(BigDecimal.valueOf(random.nextInt(11) + 10));

            } else if (standardRoll < 90) {
                // [70-89] 20% ra Rác (COMMON Material) -> Cộng Túi
                // Lấy random item Common (trừ cá ra)
                Item junkItem = getRandomItemByRarityExcludingFish(Rarity.COMMON);
                if (junkItem != null) {
                    addItemToRewardMap(rewardMap, junkItem);
                    giveItemToUser(character, junkItem);
                } else {
                    // Fallback nếu lỗi DB: Thêm ít vàng
                    totalGold = totalGold.add(BigDecimal.valueOf(5));
                }

            } else if (standardRoll < 99) {
                // [90-98] 9% ra Nguyên liệu hiếm (RARE/EPIC) -> Cộng Túi
                Rarity rarity = random.nextBoolean() ? Rarity.RARE : Rarity.EPIC;
                Item rareItem = getRandomItemByRarityExcludingFish(rarity);
                if (rareItem != null) {
                    addItemToRewardMap(rewardMap, rareItem);
                    giveItemToUser(character, rareItem);
                } else {
                    totalGold = totalGold.add(BigDecimal.valueOf(50));
                }

            } else {
                // [99] 1% JACKPOT ECHO -> Cộng Ví
                double echoVal = 0.001 + (0.499 * random.nextDouble()); // 0.001 - 0.5
                totalEcho = totalEcho.add(BigDecimal.valueOf(echoVal));
            }
        }

        // 5. Cộng Tiền Tệ vào Ví (Wallet)
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (totalGold.compareTo(BigDecimal.ZERO) > 0) {
            wallet.setGold(wallet.getGold().add(totalGold));
        }

        if (totalEcho.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentEcho = wallet.getEchoCoin() != null ? wallet.getEchoCoin() : BigDecimal.ZERO;
            wallet.setEchoCoin(currentEcho.add(totalEcho));
        }

        walletRepository.save(wallet);

        // 6. Trả về kết quả
        return DumpResponse.builder()
                .success(true)
                .message("Đã thả " + amount + " " + inputItem.getName())
                .totalGold(totalGold)
                .totalEcho(totalEcho)
                // List này chỉ chứa Vật phẩm (Megalodon, Gỗ, Khoáng...), không chứa Echo/Vàng
                .receivedItems(new ArrayList<>(rewardMap.values()))
                .build();
    }

    // --- Helper Methods ---

    private void addItemToRewardMap(Map<String, DumpResponse.RewardItem> map, Item item) {
        if (map.containsKey(item.getCode())) {
            DumpResponse.RewardItem reward = map.get(item.getCode());
            reward.setQuantity(reward.getQuantity() + 1);
        } else {
            map.put(item.getCode(), DumpResponse.RewardItem.builder()
                    .code(item.getCode())
                    .name(item.getName())
                    .rarity(item.getRarity())
                    .quantity(1)
                    .build());
        }
    }

    private void giveItemToUser(Character character, Item item) {
        Optional<UserItem> existing = userItemRepository.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), item.getItemId());
        if (existing.isPresent()) {
            UserItem ui = existing.get();
            ui.setQuantity(ui.getQuantity() + 1);
            userItemRepository.save(ui);
        } else {
            UserItem newItem = new UserItem();
            newItem.setCharacter(character);
            newItem.setItem(item);
            newItem.setQuantity(1);
            newItem.setIsEquipped(false);
            userItemRepository.save(newItem);
        }
    }

    private Item getRandomItemByRarityExcludingFish(Rarity rarity) {
        List<Item> items = itemRepository.findByRarity(rarity);
        if (items.isEmpty()) return null;

        // Lọc bỏ item có code bắt đầu bằng 'f_' (fish) để tránh trả lại cá
        List<Item> filtered = items.stream()
                .filter(i -> !i.getCode().startsWith("f_"))
                .toList();

        if (filtered.isEmpty()) return null;
        return filtered.get(random.nextInt(filtered.size()));
    }
}