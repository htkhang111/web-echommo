package com.echommo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor // [FIX] Tạo constructor có tham số
@NoArgsConstructor  // [FIX] Tạo constructor mặc định
public class SpaStatusResponse {
    private String message;
    private Integer currentHp;
    private Integer maxHp;
    private Integer currentEnergy;
    private Integer maxEnergy;
    private BigDecimal remainingGold;
    private Long secondsRemaining; // Thêm trường này để khớp với SpaService
}