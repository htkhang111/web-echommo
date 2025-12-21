package com.echommo.service;

import com.echommo.config.GameConstants;
import com.echommo.dto.SubStatDTO;
import com.echommo.entity.UserItem;
import com.echommo.entity.Wallet;
import com.echommo.repository.UserItemRepository;
import com.echommo.repository.WalletRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final UserItemRepository userItemRepo;
    private final WalletRepository walletRepo;
    private final ItemGenerationService itemGenService;
    private final CharacterService characterService; // [FIX] Tiêm service để cập nhật chỉ số nhân vật
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    // --- LOGIC MỚI: NÂNG CẤP SAO MYTHIC (1-10 SAO) ---
    @Transactional
    public UserItem enhanceMythicStars(Long userItemId, Integer userId) {
        UserItem item = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Trang bị không tồn tại!"));

        if (!item.getCharacter().getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Vật phẩm không thuộc về bạn!");
        }

        if (!Boolean.TRUE.equals(item.getIsMythic())) {
            throw new RuntimeException("Chỉ trang bị Thần Thoại (Mythic) mới có thể nâng sao!");
        }

        int currentStars = item.getMythicStars() == null ? 0 : item.getMythicStars();
        if (currentStars >= 10) {
            throw new RuntimeException("Trang bị đã đạt tối đa 10 Sao!");
        }

        int nextStar = currentStars + 1;
        long goldCost;
        BigDecimal coinCost;
        int successRate;

        switch (nextStar) {
            case 1 -> { goldCost = 1_000_000; coinCost = BigDecimal.valueOf(1); successRate = 100; }
            case 2 -> { goldCost = 2_000_000; coinCost = BigDecimal.valueOf(2); successRate = 80; }
            case 3 -> { goldCost = 3_500_000; coinCost = BigDecimal.valueOf(3); successRate = 60; }
            case 4 -> { goldCost = 5_000_000; coinCost = BigDecimal.valueOf(5); successRate = 50; }
            case 5 -> { goldCost = 7_500_000; coinCost = BigDecimal.valueOf(7); successRate = 40; }
            case 6 -> { goldCost = 10_000_000; coinCost = BigDecimal.valueOf(10); successRate = 30; }
            case 7 -> { goldCost = 15_000_000; coinCost = BigDecimal.valueOf(15); successRate = 20; }
            case 8 -> { goldCost = 20_000_000; coinCost = BigDecimal.valueOf(20); successRate = 15; }
            case 9 -> { goldCost = 30_000_000; coinCost = BigDecimal.valueOf(30); successRate = 10; }
            case 10 -> { goldCost = 50_000_000; coinCost = BigDecimal.valueOf(50); successRate = 5; }
            default -> throw new RuntimeException("Lỗi cấp sao!");
        }

        Wallet w = walletRepo.findByUser_UserId(userId).orElseThrow();

        if (w.getGold().compareTo(BigDecimal.valueOf(goldCost)) < 0) {
            throw new RuntimeException("Thiếu Vàng! Cần " + goldCost);
        }
        if (w.getEchoCoin().compareTo(coinCost) < 0) {
            throw new RuntimeException("Thiếu Echo Coin! Cần " + coinCost);
        }

        w.setGold(w.getGold().subtract(BigDecimal.valueOf(goldCost)));
        w.setEchoCoin(w.getEchoCoin().subtract(coinCost));
        walletRepo.save(w);

        UserItem savedItem;
        if (random.nextInt(100) < successRate) {
            item.setMythicStars(nextStar);
            BigDecimal currentVal = item.getMainStatValue();
            BigDecimal boost = currentVal.multiply(BigDecimal.valueOf(0.1));
            item.setMainStatValue(currentVal.add(boost));
            savedItem = userItemRepo.save(item);

            // [FIX] Cập nhật lại chỉ số nhân vật nếu đang mặc món đồ này
            if (Boolean.TRUE.equals(item.getIsEquipped())) {
                characterService.recalculateStats(item.getCharacter());
            }
        } else {
            throw new RuntimeException("Nâng cấp THẤT BẠI! Bạn mất tài nguyên nhưng trang bị vẫn an toàn.");
        }
        return savedItem;
    }

    private void checkAndConsumeResources(UserItem targetItem, Map<Integer, Integer> materialCosts, int goldCost) {
        Wallet wallet = walletRepo.findByUser_UserId(targetItem.getCharacter().getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví!"));

        BigDecimal cost = BigDecimal.valueOf(goldCost);
        if (wallet.getGold().compareTo(cost) < 0) {
            throw new RuntimeException("Không đủ vàng (Cần " + goldCost + ")!");
        }

        Map<Integer, UserItem> foundItems = new HashMap<>();
        if (materialCosts != null) {
            for (Map.Entry<Integer, Integer> entry : materialCosts.entrySet()) {
                UserItem mat = userItemRepo.findByCharacter_CharIdAndItem_ItemId(targetItem.getCharacter().getCharId(), entry.getKey())
                        .stream().filter(i -> !i.getIsEquipped()).findFirst().orElse(null);

                if (mat == null || mat.getQuantity() < entry.getValue()) {
                    throw new RuntimeException("Thiếu nguyên liệu ID: " + entry.getKey());
                }
                foundItems.put(entry.getKey(), mat);
            }
        }

        wallet.setGold(wallet.getGold().subtract(cost));
        walletRepo.save(wallet);

        if (materialCosts != null) {
            for (Map.Entry<Integer, Integer> entry : materialCosts.entrySet()) {
                UserItem itemToReduce = foundItems.get(entry.getKey());
                itemToReduce.setQuantity(itemToReduce.getQuantity() - entry.getValue());
                if (itemToReduce.getQuantity() <= 0) userItemRepo.delete(itemToReduce);
                else userItemRepo.save(itemToReduce);
            }
        }
    }

    @Transactional
    public UserItem enhanceItem(Long userItemId) {
        UserItem item = userItemRepo.findById(userItemId).orElseThrow(() -> new RuntimeException("Item not found"));
        if (Boolean.TRUE.equals(item.getIsMythic())) throw new RuntimeException("Trang bị Mythic phải dùng chức năng Nâng Cấp Sao!");

        int nextLv = item.getEnhanceLevel() + 1;
        if (nextLv > 30) throw new RuntimeException("Đã đạt cấp tối đa (+30). Hãy Đột Phá Mythic!");

        Map<Integer, Integer> mats = new HashMap<>();
        int gold = nextLv * 2000;

        // Logic nguyên liệu (Giữ nguyên)
        if (nextLv <= 10) {
            mats.put(GameConstants.MAT_COAL, 5);
            mats.put(GameConstants.MAT_WOOD_OAK, 5);
        } else if (nextLv <= 20) {
            mats.put(GameConstants.MAT_ORE_IRON, 10);
            mats.put(GameConstants.MAT_WOOD_DRIED, 10);
        } else {
            mats.put(GameConstants.MAT_ORE_PLATINUM, 15);
            mats.put(GameConstants.MAT_WOOD_COLD, 15);
        }

        checkAndConsumeResources(item, mats, gold);
        item.setEnhanceLevel(nextLv);

        // Mỗi 3 cấp cộng thêm chỉ số phụ
        if (nextLv % 3 == 0) applySubStatRoll(item);

        // [FIX] Tăng Main Stat mỗi cấp cường hóa (Giả sử mỗi cấp +5% so với gốc)
        BigDecimal baseVal = item.getOriginalMainStatValue() != null ? item.getOriginalMainStatValue() : item.getMainStatValue();
        BigDecimal growth = baseVal.multiply(BigDecimal.valueOf(0.05));
        item.setMainStatValue(item.getMainStatValue().add(growth));

        UserItem saved = userItemRepo.save(item);

        // [FIX] Cập nhật lại chỉ số nhân vật
        if (Boolean.TRUE.equals(item.getIsEquipped())) {
            characterService.recalculateStats(item.getCharacter());
        }
        return saved;
    }

    @Transactional
    public UserItem evolveToMythic(Long userItemId) {
        UserItem item = userItemRepo.findById(userItemId).orElseThrow(() -> new RuntimeException("Item not found"));
        if (item.getEnhanceLevel() < 30) throw new RuntimeException("Cần đạt +30 để đột phá!");
        if (Boolean.TRUE.equals(item.getIsMythic())) throw new RuntimeException("Trang bị đã là Mythic rồi!");

        Wallet w = walletRepo.findByUser_UserId(item.getCharacter().getUser().getUserId()).orElseThrow();
        int diamondCost = 500;
        BigDecimal goldCost = BigDecimal.valueOf(1000000);

        if (w.getDiamonds() < diamondCost) throw new RuntimeException("Thiếu " + diamondCost + " Kim Cương!");
        if (w.getGold().compareTo(goldCost) < 0) throw new RuntimeException("Thiếu 1.000.000 Vàng!");

        w.setDiamonds(w.getDiamonds() - diamondCost);
        w.setGold(w.getGold().subtract(goldCost));
        walletRepo.save(w);

        item.setIsMythic(true);
        item.setMythicStars(1);
        item.setMainStatValue(item.getMainStatValue().multiply(BigDecimal.valueOf(1.5)));

        UserItem saved = userItemRepo.save(item);

        if (Boolean.TRUE.equals(item.getIsEquipped())) {
            characterService.recalculateStats(item.getCharacter());
        }
        return saved;
    }

    private void applySubStatRoll(UserItem item) {
        try {
            List<SubStatDTO> stats = objectMapper.readValue(item.getSubStats(), new TypeReference<List<SubStatDTO>>() {});
            int tier = item.getItem().getTier() != null ? item.getItem().getTier() : 1;

            if (stats.size() < 4) {
                // Nếu chưa đủ 4 dòng -> Thêm dòng mới
                stats.add(itemGenService.generateRandomSubStat(item, stats));
            } else {
                // Nếu đã đủ 4 dòng -> Random chọn 1 dòng để tăng giá trị
                // [FIX] Dùng hàm getEnhanceRollValue đã fix ở ItemGenerationService
                SubStatDTO s = stats.get(random.nextInt(stats.size()));
                double bonus = itemGenService.getEnhanceRollValue(s.getCode(), tier);
                s.setValue(s.getValue() + bonus);
            }
            item.setSubStats(objectMapper.writeValueAsString(stats));
        } catch (Exception e) {}
    }
}