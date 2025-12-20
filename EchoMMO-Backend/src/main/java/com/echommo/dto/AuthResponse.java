package com.echommo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long id;
    private String username;
    private String email;
    private String role;

    // [FIX] Đổi từ Long -> BigDecimal để tránh lỗi AuthController
    private BigDecimal echoCoin;
    private Long gold;

    // ... các trường khác (avatarUrl, v.v.) giữ nguyên theo code cũ của bạn
    private String avatarUrl;
}