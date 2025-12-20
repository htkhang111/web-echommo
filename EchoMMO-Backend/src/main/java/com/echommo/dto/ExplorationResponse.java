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
    // --- CÁC TRƯỜNG CŨ (BẮT BUỘC GIỮ) ---
    private String message;
    private String type;       // GATHERING, ENEMY, ITEM, TEXT...
    private BigDecimal goldGained; // Đổi sang BigDecimal cho đồng bộ, hoặc giữ Long nếu FE ép kiểu

    private Long currentExp;
    private Integer currentLv;
    private Integer currentEnergy;
    private Integer maxEnergy;
    private Integer newLevel;

    private String rewardName;
    private Integer rewardAmount;
    private Integer rewardItemId;

    // Legacy fields (có thể giữ để tương thích ngược nếu code cũ gọi)
    private String itemReceived;
    private int quantity;

    // --- CÁC TRƯỜNG MỚI (LOGIC ECHO/GOLD) ---
    private String log;
    private String rewardType;     // GOLD, ITEM, CURRENCY
    private String rewardValue;
    private String imageUrl;
    private boolean specialReward;
}