package com.echommo.config;

import java.util.Map;
import static java.util.Map.entry; // Import này giúp viết code ngắn gọn hơn

public class GameConstants {

    // --- 1. GENERAL CONFIG ---
    public static final int MAX_LEVEL = 70;
    public static final int STAT_POINTS_PER_LEVEL = 5; // Công thức: (lv-1) * 5
    public static final double CONVERSION_RATE = 10.0; // 10 Point = 1%

    // --- 2. HARD CAPS (TRẦN CHỈ SỐ - FLAT VALUE) ---
    // Ví dụ: ACCURACY 800 điểm = 80%.
    public static final Map<String, Integer> STAT_CAPS = Map.of(
            "ACCURACY", 800,   // Max 80%
            "EVASION", 750,    // Max 75%
            "CRIT_RATE", 850,  // Max 85%
            "CRIT_DMG", 3000   // Max 300%
            // SPEED không giới hạn
    );

    // --- 3. ITEM GENERATION: WEIGHTED RANDOM (TỶ LỆ RA DÒNG) ---
    // [FIX] Dùng Map.ofEntries vì có trên 10 phần tử
    public static final Map<String, Integer> STAT_WEIGHTS = Map.ofEntries(
            entry("ATK_PERCENT", 20),
            entry("DEF_PERCENT", 20),
            entry("HP_PERCENT", 20),
            entry("ATK_FLAT", 15),
            entry("DEF_FLAT", 15),
            entry("HP_FLAT", 15),
            entry("CRIT_RATE", 10),
            entry("CRIT_DMG", 10),
            entry("ACCURACY", 10),
            entry("EVASION", 10),
            entry("SPEED", 2) // Siêu hiếm
    );

    // --- 4. ITEM GENERATION: ROLL RANGES (GIÁ TRỊ RANDOM TIER 1) ---
    // [Min, Max]. Với chỉ số Flat, đây là điểm số.
    public static final Map<String, double[]> ROLL_RANGES = Map.ofEntries(
            entry("ATK_PERCENT", new double[]{4.0, 8.0}),
            entry("DEF_PERCENT", new double[]{4.0, 8.0}),
            entry("HP_PERCENT", new double[]{4.0, 8.0}),
            entry("ATK_FLAT", new double[]{20.0, 40.0}),
            entry("DEF_FLAT", new double[]{20.0, 40.0}),
            entry("HP_FLAT", new double[]{100.0, 200.0}),
            // Flat special stats
            entry("SPEED", new double[]{1.0, 3.0}),
            entry("CRIT_RATE", new double[]{20.0, 40.0}), // 2% - 4%
            entry("CRIT_DMG", new double[]{40.0, 70.0}),  // 4% - 7%
            entry("ACCURACY", new double[]{10.0, 20.0}),  // 1% - 2%
            entry("EVASION", new double[]{10.0, 20.0})    // 1% - 2%
    );

    // --- 5. MYTHIC CONFIG ---
    public static final double[] MYTHIC_EVOLUTION_RANGE = {1.005, 1.01}; // Nhân 0.5% - 1% khi tiến hóa
    public static final double MYTHIC_MAIN_GROWTH = 0.01; // Tăng 1% Main Stat mỗi cấp Mythic
    public static final double[] MYTHIC_SUB_GROWTH = {0.005, 0.01}; // Tăng 0.5% - 1% Sub Stat mỗi cấp
}