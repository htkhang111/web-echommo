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
    private BigDecimal echoCoin; // [FIX]
    private Long gold;
    private String avatarUrl;
}