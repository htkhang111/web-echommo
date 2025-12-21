package com.echommo.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DumpResponse {
    private boolean success;
    private String message;
    private BigDecimal totalGold;
    private BigDecimal totalEcho;
    // Map<Tên vật phẩm, Số lượng>
    private Map<String, Integer> receivedItems;
}