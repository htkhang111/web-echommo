package com.echommo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExplorationResponse {
    private String message;
    private String type;       // NOTHING, GOLD, ITEM, COMBAT, REST...
    private BigDecimal goldGained;

    // 👇 [QUAN TRỌNG] Đã đổi từ Integer sang Long để khớp với Entity Character
    private Long currentExp;

    private Integer currentLv;
    private Integer currentEnergy;
    private Integer maxEnergy;
    private Integer newLevel;

    private String rewardName;
    private Integer rewardAmount;
}