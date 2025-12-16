package com.echommo.service;

import com.echommo.dto.SubStatDTO;
import com.echommo.entity.UserItem;
import com.echommo.enums.SlotType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ItemGenerationService {

    private final Random random = new Random();

    // --- 1. CORE: Hàm sinh dòng phụ ---
    public SubStatDTO generateRandomSubStat(UserItem userItem, List<SubStatDTO> currentStats) {
        SlotType slot = userItem.getItem().getSlotType();
        String mainStat = userItem.getMainStatType();

        while (true) {
            String candidateType = rollStatType();

            // Rule: Không trùng Main Stat
            if (candidateType.equals(mainStat)) continue;

            // Rule: Không trùng dòng phụ đã có
            boolean isDuplicate = currentStats.stream()
                    .anyMatch(s -> s.getCode().equals(candidateType));
            if (isDuplicate) continue;

            // Rule: Blacklist (Luật cấm)
            if (isBlacklisted(slot, candidateType)) continue;

            // Roll giá trị
            double value = getBaseRollValue(candidateType, userItem.getItem().getTier());

            // Check Percent để hiển thị
            boolean isPercent = candidateType.contains("PERCENT")
                    || candidateType.equals("CRIT_RATE")
                    || candidateType.equals("CRIT_DMG");

            return new SubStatDTO(candidateType, value, null, isPercent);
        }
    }

    // --- 2. SUPPORT: Roll loại chỉ số ---
    private String rollStatType() {
        String[] stats = {
                "ATK_FLAT", "ATK_PERCENT",
                "DEF_FLAT", "DEF_PERCENT",
                "HP_FLAT", "HP_PERCENT",
                "SPEED",
                "CRIT_RATE", "CRIT_DMG",
                "ACCURACY", "EVASION" // [MỚI] Thêm 2 chỉ số này
        };
        return stats[random.nextInt(stats.length)];
    }

    // --- 3. SUPPORT: Blacklist ---
    private boolean isBlacklisted(SlotType slot, String statType) {
        switch (slot) {
            case WEAPON: // Kiếm: Không Thủ, Không Né
                return statType.contains("DEF") || statType.equals("EVASION");
            case ARMOR: // Áo: Không Công, Không Chính xác
                return statType.contains("ATK") || statType.equals("ACCURACY");
            default:
                return false;
        }
    }

    // --- 4. SUPPORT: Giá trị Roll (Tier 1) ---
    private double getBaseRollValue(String statType, int tier) {
        int multiplier = Math.max(1, tier);

        switch (statType) {
            case "SPEED": return random.nextInt(3) + 1 + (tier > 3 ? 1 : 0);
            case "CRIT_RATE": return random.nextInt(3) + 2;
            case "CRIT_DMG": return random.nextInt(4) + 4;
            case "ATK_PERCENT":
            case "HP_PERCENT":
            case "DEF_PERCENT": return random.nextInt(5) + 4;

            // [MỚI] Roll phẳng cho ACC/EVA (10-20 điểm ở tier 1)
            case "ACCURACY":
            case "EVASION": return (random.nextInt(11) + 10) * multiplier;

            default: return (random.nextInt(10) + 10) * multiplier;
        }
    }

    public double getEnhanceRollValue(String statType, int tier) {
        return getBaseRollValue(statType, tier);
    }
}