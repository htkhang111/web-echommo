package com.echommo.config;

import java.util.Map;
import static java.util.Map.entry;

public class GameConstants {

    // --- 1. GENERAL CONFIG ---
    public static final int MAX_LEVEL = 70;
    public static final int STAT_POINTS_PER_LEVEL = 5;
    public static final double CONVERSION_RATE = 10.0;

    // --- 2. MATERIAL IDS (Mapping theo file CSV) ---
    // Gỗ (Dùng cho Giáp/Phụ kiện hoặc làm nguyên liệu phụ cho Vũ khí)
    public static final int MAT_WOOD_OAK = 1;      // Gỗ Sồi (Common)
    public static final int MAT_WOOD_DRIED = 2;    // Gỗ Khô (Common)
    public static final int MAT_WOOD_COLD = 3;     // Gỗ Lạnh (Uncommon)
    public static final int MAT_WOOD_STRANGE = 4;  // Gỗ Lạ (Rare)

    // Khoáng sản (Dùng cho Vũ khí/Giáp nặng)
    public static final int MAT_STONE = 5;         // Đá (Common)
    public static final int MAT_ORE_COPPER = 6;    // Quặng Đồng (Common)
    public static final int MAT_ORE_IRON = 7;      // Sắt (Rare)
    public static final int MAT_ORE_PLATINUM = 8;  // Bạch Kim (Epic)

    // Vật phẩm đặc biệt (End-game)
    public static final int MAT_ECHO_COIN = 11;    // Echo Coin (Legendary)
    public static final int MAT_UNKNOWN = 12;      // Nguyên liệu lạ (Epic)

    // --- 3. HARD CAPS (GIỚI HẠN CHỈ SỐ) ---
    public static final Map<String, Integer> STAT_CAPS = Map.of(
            "ACCURACY", 800,   // 80%
            "EVASION", 750,    // 75%
            "CRIT_RATE", 850,  // 85%
            "CRIT_DMG", 3000   // 300%
    );

    // --- 4. ITEM GENERATION: WEIGHTED RANDOM ---
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
            entry("SPEED", 2)
    );

    // --- 5. ITEM GENERATION: ROLL RANGES ---
    public static final Map<String, double[]> ROLL_RANGES = Map.ofEntries(
            entry("ATK_PERCENT", new double[]{4.0, 8.0}),
            entry("DEF_PERCENT", new double[]{4.0, 8.0}),
            entry("HP_PERCENT", new double[]{4.0, 8.0}),
            entry("ATK_FLAT", new double[]{20.0, 40.0}),
            entry("DEF_FLAT", new double[]{20.0, 40.0}),
            entry("HP_FLAT", new double[]{100.0, 200.0}),
            entry("SPEED", new double[]{1.0, 3.0}),
            entry("CRIT_RATE", new double[]{20.0, 40.0}),
            entry("CRIT_DMG", new double[]{40.0, 70.0}),
            entry("ACCURACY", new double[]{10.0, 20.0}),
            entry("EVASION", new double[]{10.0, 20.0})
    );

    // --- 6. MYTHIC CONFIG ---
    public static final double[] MYTHIC_EVOLUTION_RANGE = {1.005, 1.01};
    public static final double MYTHIC_MAIN_GROWTH = 0.01;
    public static final double[] MYTHIC_SUB_GROWTH = {0.005, 0.01};
}