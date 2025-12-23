package com.echommo.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateListingRequest {
    private Integer userItemId; // [FIX] Dùng Integer
    private Integer quantity;
    private BigDecimal price;
}