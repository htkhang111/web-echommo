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
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

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
                        .orElse(null);
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

        if (Boolean.TRUE.equals(item.getIsMythic())) {
            throw new RuntimeException("Trang bị Mythic phải dùng chức năng Nâng Cấp Mythic!");
        }

        // [FIX] Sử dụng getter/setter chuẩn của UserItem mới
        int nextLv = item.getEnhanceLevel() + 1;
        if (nextLv > 30) throw new RuntimeException("Đã đạt cấp tối đa (+30). Hãy Đột Phá Mythic!");

        Map<Integer, Integer> mats = new HashMap<>();
        int gold = nextLv * 2000;

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

        if (nextLv % 3 == 0) applySubStatRoll(item);

        return userItemRepo.save(item);
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

        return userItemRepo.save(item);
    }

    @Transactional
    public UserItem enhanceMythic(Long userItemId) {
        UserItem item = userItemRepo.findById(userItemId).orElseThrow(() -> new RuntimeException("Item not found"));

        if (!Boolean.TRUE.equals(item.getIsMythic())) throw new RuntimeException("Không phải trang bị Mythic!");
        if (item.getMythicStars() >= 10) throw new RuntimeException("Đã đạt giới hạn 10 Sao!");

        int nextStar = item.getMythicStars() + 1;
        int diamondCost = nextStar * 100;
        BigDecimal goldCost = BigDecimal.valueOf(500000L * nextStar);

        Wallet w = walletRepo.findByUser_UserId(item.getCharacter().getUser().getUserId()).orElseThrow();
        if (w.getDiamonds() < diamondCost) throw new RuntimeException("Thiếu " + diamondCost + " Kim Cương!");
        if (w.getGold().compareTo(goldCost) < 0) throw new RuntimeException("Thiếu vàng!");

        w.setDiamonds(w.getDiamonds() - diamondCost);
        w.setGold(w.getGold().subtract(goldCost));
        walletRepo.save(w);

        item.setMythicStars(nextStar);
        item.setMainStatValue(item.getMainStatValue().multiply(BigDecimal.valueOf(1.1)));

        return userItemRepo.save(item);
    }

    private void applySubStatRoll(UserItem item) {
        try {
            List<SubStatDTO> stats = objectMapper.readValue(item.getSubStats(), new TypeReference<List<SubStatDTO>>() {});
            if (stats.size() < 4) stats.add(itemGenService.generateRandomSubStat(item, stats));
            else {
                SubStatDTO s = stats.get(random.nextInt(stats.size()));
                s.setValue(s.getValue() + itemGenService.getEnhanceRollValue(s.getCode(), item.getItem().getTier()));
            }
            item.setSubStats(objectMapper.writeValueAsString(stats));
        } catch (Exception e) {}
    }
}