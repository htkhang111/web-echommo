package com.echommo.service;

import com.echommo.config.GameConstants;
import com.echommo.dto.SubStatDTO;
import com.echommo.entity.Item;
import com.echommo.entity.UserItem;
import com.echommo.enums.SlotType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ItemGenerationService {

    private final Random random = new Random();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void randomizeNewItem(UserItem userItem) {
        Item baseItem = userItem.getItem();

        // 1. Random biến thể hình ảnh (0-4)
        userItem.setVisualVariant(random.nextInt(GameConstants.MAX_ASSET_VARIANTS));

        // 2. Random Main Stat (+/- 10%)
        if (userItem.getMainStatValue() != null) {
            double variance = 0.9 + (random.nextDouble() * 0.2);
            BigDecimal newVal = userItem.getMainStatValue().multiply(BigDecimal.valueOf(variance));
            userItem.setMainStatValue(newVal);
        }

        // 3. Random Sub Stats dựa trên Rarity
        int initialSubs = switch (baseItem.getRarity()) {
            case UNCOMMON -> 1;
            case RARE -> 2;
            case EPIC -> 3;
            case LEGENDARY, MYTHIC -> 4;
            default -> 0;
        };

        List<SubStatDTO> stats = new ArrayList<>();
        for (int i = 0; i < initialSubs; i++) {
            stats.add(generateRandomSubStat(userItem, stats));
        }

        try {
            userItem.setSubStats(objectMapper.writeValueAsString(stats));
        } catch (Exception e) {
            userItem.setSubStats("[]");
        }
    }

    public SubStatDTO generateRandomSubStat(UserItem userItem, List<SubStatDTO> currentStats) {
        SlotType slot = userItem.getItem().getSlotType();
        int tier = userItem.getItem().getTier();

        while (true) {
            String candidateType = rollStatTypeWithWeight();
            boolean isDuplicate = currentStats.stream().anyMatch(s -> s.getCode().equals(candidateType));
            if (isDuplicate || isBlacklisted(slot, candidateType)) continue;

            double value = rollValue(candidateType, tier);
            boolean isPercent = candidateType.contains("PERCENT") ||
                    List.of("CRIT_RATE", "CRIT_DMG", "ACCURACY", "EVASION").contains(candidateType);

            return new SubStatDTO(candidateType, value, null, isPercent);
        }
    }

    private String rollStatTypeWithWeight() {
        int totalWeight = GameConstants.STAT_WEIGHTS.values().stream().mapToInt(Integer::intValue).sum();
        int roll = random.nextInt(totalWeight);
        int currentSum = 0;

        // [FIX] Sử dụng java.util.Map để tránh lỗi compiler
        for (java.util.Map.Entry<String, Integer> entry : GameConstants.STAT_WEIGHTS.entrySet()) {
            currentSum += entry.getValue();
            if (roll < currentSum) return entry.getKey();
        }
        return "HP_FLAT";
    }

    private boolean isBlacklisted(SlotType slot, String statType) {
        if (slot == SlotType.WEAPON) return statType.contains("DEF") || statType.equals("EVASION");
        if (slot == SlotType.ARMOR) return statType.contains("ATK") || statType.equals("ACCURACY");
        return false;
    }

    private double rollValue(String statType, int tier) {
        double[] range = GameConstants.ROLL_RANGES.getOrDefault(statType, new double[]{1.0, 2.0});
        double val = range[0] + (range[1] - range[0]) * random.nextDouble();
        return Math.round(val * tier * 100.0) / 100.0;
    }
}