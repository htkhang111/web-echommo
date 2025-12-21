//package com.echommo.service;
//
//import com.echommo.config.GameConstants;
//import com.echommo.dto.SubStatDTO;
//import com.echommo.entity.UserItem;
//import com.echommo.enums.SlotType;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//
//@Service
//@RequiredArgsConstructor
//public class ItemGenerationService {
//
//    private final Random random = new Random();
//
//    // --- 1. CORE: Hàm sinh dòng phụ ---
//    public SubStatDTO generateRandomSubStat(UserItem userItem, List<SubStatDTO> currentStats) {
//        SlotType slot = userItem.getItem().getSlotType();
//        int tier = userItem.getItem().getTier();
//
//        while (true) {
//            String candidateType = rollStatTypeWithWeight();
//
//            // Rule 1: Không trùng loại dòng phụ đã có
//            boolean isDuplicate = currentStats.stream()
//                    .anyMatch(s -> s.getCode().equals(candidateType));
//            if (isDuplicate) continue;
//
//            // Rule 2: Blacklist
//            if (isBlacklisted(slot, candidateType)) continue;
//
//            // Rule 3: Roll giá trị
//            double value = rollValue(candidateType, tier);
//
//            // Xác định hiển thị %
//            boolean isPercent = candidateType.contains("PERCENT")
//                    || candidateType.equals("CRIT_RATE")
//                    || candidateType.equals("CRIT_DMG")
//                    || candidateType.equals("ACCURACY")
//                    || candidateType.equals("EVASION"); // Mấy cái này là Flat Point nhưng UI game thường hiện %
//
//            return new SubStatDTO(candidateType, value, null, isPercent);
//        }
//    }
//
//    // --- 2. SUPPORT: Weighted Random ---
//    private String rollStatTypeWithWeight() {
//        int totalWeight = GameConstants.STAT_WEIGHTS.values().stream().mapToInt(Integer::intValue).sum();
//        int roll = random.nextInt(totalWeight);
//        int currentSum = 0;
//
//        for (Map.Entry<String, Integer> entry : GameConstants.STAT_WEIGHTS.entrySet()) {
//            currentSum += entry.getValue();
//            if (roll < currentSum) {
//                return entry.getKey();
//            }
//        }
//        return "HP_FLAT"; // Fallback
//    }
//
//    // --- 3. SUPPORT: Blacklist ---
//    private boolean isBlacklisted(SlotType slot, String statType) {
//        switch (slot) {
//            case WEAPON: // Kiếm: Cấm Thủ, Cấm Né
//                return statType.contains("DEF") || statType.equals("EVASION");
//            case ARMOR: // Áo: Cấm Công, Cấm Chính xác
//                return statType.contains("ATK") || statType.equals("ACCURACY");
//            default:
//                return false;
//        }
//    }
//
//    // --- 4. SUPPORT: Roll Value ---
//    private double rollValue(String statType, int tier) {
//        double[] range = GameConstants.ROLL_RANGES.getOrDefault(statType, new double[]{1.0, 2.0});
//        double min = range[0];
//        double max = range[1];
//
//        // Công thức: Random trong range * Tier
//        // Ví dụ: Tier 2, Range ATK% [4, 8] -> Random(4,8) * 2 -> Kết quả từ 8 đến 16
//        double baseRoll = min + (max - min) * random.nextDouble();
//
//        // Làm tròn 2 chữ số thập phân
//        return Math.round(baseRoll * tier * 100.0) / 100.0;
//    }
//
//    // Dùng cho cường hóa nhảy số
//    public double getEnhanceRollValue(String statType, int tier) {
//        return rollValue(statType, tier);
//    }
//}

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

    // --- MAIN: Khởi tạo Item mới nhặt được ---
    public void randomizeNewItem(UserItem userItem) {
        Item baseItem = userItem.getItem();

        // 1. Random Visual Variant (0-4)
        userItem.setVisualVariant(random.nextInt(GameConstants.MAX_ASSET_VARIANTS));

        // 2. Random Main Stat (Biến thiên +/- 10% so với base)
        // Ví dụ: Base DB là 10 -> Random ra 9 đến 11
        if (userItem.getMainStatValue() != null) {
            double variance = 0.9 + (random.nextDouble() * 0.2); // 0.9 -> 1.1
            BigDecimal newVal = userItem.getMainStatValue().multiply(BigDecimal.valueOf(variance));
            userItem.setMainStatValue(newVal);
        }

        // 3. Random Sub Stats (Tùy độ hiếm)
        // Common: 0 dòng, Uncommon: 1 dòng, Rare: 2 dòng...
        int initialSubs = switch (baseItem.getRarity()) {
            case UNCOMMON -> 1;
            case RARE -> 2;
            case EPIC -> 3;
            case LEGENDARY -> 4;
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

    // ... (Giữ nguyên hàm generateRandomSubStat, rollStatTypeWithWeight, rollValue cũ) ...
    // Copy lại các hàm private cũ vào đây
    public SubStatDTO generateRandomSubStat(UserItem userItem, List<SubStatDTO> currentStats) {
        SlotType slot = userItem.getItem().getSlotType();
        int tier = userItem.getItem().getTier();
        while (true) {
            String candidateType = rollStatTypeWithWeight();
            boolean isDuplicate = currentStats.stream().anyMatch(s -> s.getCode().equals(candidateType));
            if (isDuplicate || isBlacklisted(slot, candidateType)) continue;
            double value = rollValue(candidateType, tier);
            boolean isPercent = candidateType.contains("PERCENT") || List.of("CRIT_RATE", "CRIT_DMG", "ACCURACY", "EVASION").contains(candidateType);
            return new SubStatDTO(candidateType, value, null, isPercent);
        }
    }

    private String rollStatTypeWithWeight() {
        int totalWeight = GameConstants.STAT_WEIGHTS.values().stream().mapToInt(Integer::intValue).sum();
        int roll = random.nextInt(totalWeight);
        int currentSum = 0;
        for (Map.Entry<String, Integer> entry : GameConstants.STAT_WEIGHTS.entrySet()) {
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
        return Math.round((range[0] + (range[1] - range[0]) * random.nextDouble()) * tier * 100.0) / 100.0;
    }

    public double getEnhanceRollValue(String statType, int tier) { return rollValue(statType, tier); }
}