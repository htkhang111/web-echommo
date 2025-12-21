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

        // Fix logic BigDecimal
        if (wallet.getGold().compareTo(BigDecimal.valueOf(goldCost)) < 0) throw new RuntimeException("Không đủ vàng!");

        Map<Integer, UserItem> foundItems = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : materialCosts.entrySet()) {
            UserItem mat = userItemRepo.findByCharacter_CharIdAndItem_ItemId(targetItem.getCharacter().getCharId(), entry.getKey())
                    .orElse(null);
            if (mat == null || mat.getQuantity() < entry.getValue()) throw new RuntimeException("Thiếu nguyên liệu!");
            foundItems.put(entry.getKey(), mat);
        }

        wallet.setGold(wallet.getGold().subtract(BigDecimal.valueOf(goldCost)));
        walletRepo.save(wallet);

        for (Map.Entry<Integer, Integer> entry : materialCosts.entrySet()) {
            UserItem itemToReduce = foundItems.get(entry.getKey());
            itemToReduce.setQuantity(itemToReduce.getQuantity() - entry.getValue());
            if (itemToReduce.getQuantity() <= 0) userItemRepo.delete(itemToReduce);
            else userItemRepo.save(itemToReduce);
        }
    }

    @Transactional
    public UserItem enhanceItem(Long userItemId) {
        UserItem item = userItemRepo.findById(userItemId).orElseThrow(() -> new RuntimeException("Not found"));
        int nextLv = item.getEnhanceLevel() + 1;
        Map<Integer, Integer> mats = new HashMap<>();
        int gold = nextLv * 2000;

        if (nextLv <= 10) { mats.put(GameConstants.MAT_COAL, 10); mats.put(GameConstants.MAT_WOOD_OAK, 5); }
        else if (nextLv <= 20) { mats.put(GameConstants.MAT_ORE_IRON, 15); mats.put(GameConstants.MAT_WOOD_DRIED, 10); }
        else { mats.put(GameConstants.MAT_ORE_PLATINUM, 20); mats.put(GameConstants.MAT_WOOD_COLD, 15); }

        checkAndConsumeResources(item, mats, gold);
        item.setEnhanceLevel(nextLv);
        if (nextLv % 3 == 0) applySubStatRoll(item);
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