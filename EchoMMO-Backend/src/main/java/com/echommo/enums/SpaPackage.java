package com.echommo.enums;

import java.math.BigDecimal;

public enum SpaPackage {
    // [FIX] Đổi tên STANDARD thành BASIC để khớp với SpaService
    // Gói Cơ Bản: 500 Vàng, 30 phút (1800 giây)
    BASIC("Gói Cơ Bản", new BigDecimal("500"), 1800),

    // Gói VIP: 5 Coin, 60 phút (3600 giây)
    VIP("Gói Thượng Hạng", new BigDecimal("5"), 3600);

    private final String name;
    private final BigDecimal cost;
    private final int durationSeconds;

    SpaPackage(String name, BigDecimal cost, int durationSeconds) {
        this.name = name;
        this.cost = cost;
        this.durationSeconds = durationSeconds;
    }

    public String getName() { return name; }
    public BigDecimal getCost() { return cost; }
    public int getDurationSeconds() { return durationSeconds; }

    // Alias để tránh lỗi nếu code cũ gọi getPrice
    public BigDecimal getPrice() { return cost; }
}