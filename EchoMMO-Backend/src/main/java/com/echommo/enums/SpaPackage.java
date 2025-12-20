package com.echommo.enums;

import java.math.BigDecimal;

public enum SpaPackage {
    // Gói Lỏ: 60 giây, 50 Vàng
    STANDARD("Gói Bình Dân", new BigDecimal("50"), 60),

    // Gói Xịn: 10 giây, 200 Echo (Hoặc Vàng tùy logic, ở đây set tạm BigDecimal để tính toán)
    VIP("Gói Thượng Hạng", new BigDecimal("200"), 10);

    private final String name;
    private final BigDecimal cost;
    private final int durationSeconds;

    SpaPackage(String name, BigDecimal cost, int durationSeconds) {
        this.name = name;
        this.cost = cost;
        this.durationSeconds = durationSeconds;
    }

    // [FIX] Getter chuẩn để Service gọi
    public String getName() { return name; }
    public BigDecimal getCost() { return cost; }
    public int getDurationSeconds() { return durationSeconds; }

    // Alias cho getCost nếu code cũ lỡ gọi getPrice
    public BigDecimal getPrice() { return cost; }
}