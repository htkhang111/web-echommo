package com.echommo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExplorationResponse {
    // --- LEGACY ---
    private String message;
    private String type;
    private BigDecimal goldGained; // Changed type, logic compatible
    private Long currentExp;
    private Integer currentLv;
    private Integer currentEnergy;
    private Integer maxEnergy;
    private Integer newLevel;
    private String rewardName;
    private Integer rewardAmount;
    private Integer rewardItemId;
    private String itemReceived;
    private int quantity;

    // --- NEW ---
    private String log;
    private String rewardType;
    private String rewardValue;
    private String imageUrl;
    private boolean specialReward;
}