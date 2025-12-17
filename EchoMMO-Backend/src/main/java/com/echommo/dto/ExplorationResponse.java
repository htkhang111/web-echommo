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
    private String message;
    private String type;       // GATHERING, ENEMY, ITEM, TEXT...
    private BigDecimal goldGained;

    private Long currentExp;
    private Integer currentLv;
    private Integer currentEnergy;
    private Integer maxEnergy;

    // Cấp độ mới nếu có lên cấp (null nếu không lên)
    private Integer newLevel;

    private String rewardName;
    private Integer rewardAmount;

    // ID của item (dùng cho cả nhặt được hoặc phát hiện mỏ)
    private Integer rewardItemId;
}