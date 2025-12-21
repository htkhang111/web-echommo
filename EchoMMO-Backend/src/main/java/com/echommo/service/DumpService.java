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

        // 2. Xác định loại cá và mã item tương ứng
        String fishCode = request.getType().equalsIgnoreCase("SHARK") ? "f_shark" : "f_fish";

        // 3. Kiểm tra trong túi đồ
        Item fishItem = itemRepository.findByCode(fishCode)
                .orElseThrow(() -> new RuntimeException("Item code " + fishCode + " không tồn tại trong hệ thống"));

        UserItem userFish = userItemRepository.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), fishItem.getItemId())
                .orElseThrow(() -> new RuntimeException("Bạn không sở hữu loại cá này"));

        if (userFish.getQuantity() < request.getAmount()) {
            throw new RuntimeException("Không đủ số lượng cá để thả!");
        }

        // 4. Trừ số lượng cá (Xóa nếu hết)
        userFish.setQuantity(userFish.getQuantity() - request.getAmount());
        if (userFish.getQuantity() <= 0) {
            userItemRepository.delete(userFish);
        } else {
            userItemRepository.save(userFish);
        }

        // 5. Tính toán phần thưởng (RNG)
        BigDecimal totalGold = BigDecimal.ZERO;
        BigDecimal totalEcho = BigDecimal.ZERO;

        // [UPDATED] Dùng Map để gom nhóm item theo Code trước khi chuyển thành List
        Map<String, DumpResponse.RewardItem> rewardMap = new HashMap<>();

        boolean isShark = request.getType().equalsIgnoreCase("SHARK");

        for (int i = 0; i < request.getAmount(); i++) {
            if (isShark) {
                // --- LOGIC CÁ MẬP (SHARK) ---
                int roll = random.nextInt(100); // 0-99

                if (roll < 50) {
                    // 50% ra Vàng (100 - 500)
                    totalGold = totalGold.add(BigDecimal.valueOf(random.nextInt(401) + 100));
                } else if (roll < 80) {
                    // 30% ra Echo (0.1 - 0.5)
                    double echoVal = 0.1 + (0.4 * random.nextDouble());
                    totalEcho = totalEcho.add(BigDecimal.valueOf(echoVal));
                } else {
                    // 20% ra Item RARE hoặc EPIC
                    Rarity rarity = (random.nextBoolean()) ? Rarity.RARE : Rarity.EPIC;
                    Item rewardItem = getRandomItemByRarity(rarity);
                    if (rewardItem != null) {
                        addItemToRewardMap(rewardMap, rewardItem);
                        giveItemToUser(character, rewardItem);
                    } else {
                        // Fallback: tặng vàng
                        totalGold = totalGold.add(BigDecimal.valueOf(200));
                    }
                }

            } else {
                // --- LOGIC CÁ THƯỜNG (NORMAL) ---
                int roll = random.nextInt(100);

                if (roll < 70) {
                    // 70% ra Vàng (10 - 50)
                    totalGold = totalGold.add(BigDecimal.valueOf(random.nextInt(41) + 10));
                } else if (roll < 95) {
                    // 25% ra rác (COMMON)
                    Item rewardItem = getRandomItemByRarity(Rarity.COMMON);
                    if (rewardItem != null) {
                        addItemToRewardMap(rewardMap, rewardItem);
                        giveItemToUser(character, rewardItem);
                    }
                } else {
                    // 5% May mắn cực độ: Ra 1 ít Echo (0.01 - 0.05)
                    double echoVal = 0.01 + (0.04 * random.nextDouble());
                    totalEcho = totalEcho.add(BigDecimal.valueOf(echoVal));
                }
            }
        }

        // 6. Cộng tiền vào ví
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setGold(wallet.getGold().add(totalGold));
        BigDecimal currentEcho = wallet.getEchoCoin() != null ? wallet.getEchoCoin() : BigDecimal.ZERO;
        wallet.setEchoCoin(currentEcho.add(totalEcho));

        walletRepository.save(wallet);

        // 7. Trả về kết quả
        return DumpResponse.builder()
                .success(true)
                .message("Đã thả " + request.getAmount() + " con " + (isShark ? "Cá Mập" : "Cá Thường"))
                .totalGold(totalGold)
                .totalEcho(totalEcho)
                .receivedItems(new ArrayList<>(rewardMap.values())) // Chuyển map values thành list
                .build();
    }

    // --- Helper Methods ---

    private Item getRandomItemByRarity(Rarity rarity) {
        List<Item> items = itemRepository.findByRarity(rarity);
        if (items.isEmpty()) return null;
        return items.get(random.nextInt(items.size()));
    }

    // [UPDATED] Helper mới để quản lý Map reward
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
}