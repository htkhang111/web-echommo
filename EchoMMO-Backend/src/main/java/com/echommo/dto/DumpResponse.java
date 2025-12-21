package com.echommo.dto;

import com.echommo.enums.Rarity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DumpResponse {
    private boolean success;
    private String message;
    private BigDecimal totalGold;
    private BigDecimal totalEcho;

    // [UPDATED] Thay đổi từ Map<String, Integer> sang List các object chi tiết
    private List<RewardItem> receivedItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardItem {
        private String code;     // Để frontend lấy ảnh: getAssetUrl(code)
        private String name;     // Tên hiển thị
        private Rarity rarity;   // Để hiển thị màu sắc: COMMON, RARE, EPIC...
        private int quantity;    // Số lượng
    }
}