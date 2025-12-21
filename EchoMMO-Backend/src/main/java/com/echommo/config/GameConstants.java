package com.echommo.config;

import java.math.BigDecimal;
import java.util.Map;
import static java.util.Map.entry;

public class GameConstants {
    // --- CẤU HÌNH CHUNG ---
    public static final int MAX_LEVEL = 70;
    public static final int STAT_POINTS_PER_LEVEL = 5;
    public static final double CONVERSION_RATE = 10.0;
    public static final int MAX_ASSET_VARIANTS = 5; // [FIX] Hỗ trợ biến thể ảnh 0-4

    // --- MÃ VẬT PHẨM (ID & ALIAS) ---
    public static final int MAT_WOOD_OAK = 1;
    public static final int MAT_WOOD_DRIED = 2;
    public static final int MAT_WOOD_COLD = 3;
    public static final int MAT_WOOD_BLACK = 4;
    public static final int MAT_WOOD_STRANGE = MAT_WOOD_BLACK;

    public static final int MAT_COAL = 5;
    public static final int MAT_STONE = MAT_COAL; // [FIX] Đá thay bằng Than

    public static final int MAT_ORE_COPPER = 6;
    public static final int MAT_ORE_IRON = 7;
    public static final int MAT_ORE_PLATINUM = 8;
    public static final int MAT_ORE_STRANGE = 12;
    public static final int MAT_UNKNOWN = MAT_ORE_STRANGE;

    public static final int MAT_ORE_GOLD = 14;

    public static final int MAT_FISH = 9;
    public static final int MAT_FISH_SHARK = 10;
    public static final int MAT_FISH_MEGALODON = 13;
    public static final int MAT_ECHO_COIN = 11;

    // --- CẤU HÌNH MYTHIC ---
    public static final double[] MYTHIC_EVOLUTION_RANGE = {1.005, 1.01};
    public static final double MYTHIC_MAIN_GROWTH = 0.01;
    public static final double[] MYTHIC_SUB_GROWTH = {0.005, 0.01};

    // --- CẤU HÌNH CHỈ SỐ ---
    public static final Map<String, Integer> STAT_WEIGHTS = Map.ofEntries(
            entry("ATK_PERCENT", 20), entry("DEF_PERCENT", 20), entry("HP_PERCENT", 20),
            entry("ATK_FLAT", 15), entry("DEF_FLAT", 15), entry("HP_FLAT", 15),
            entry("CRIT_RATE", 10), entry("CRIT_DMG", 10), entry("ACCURACY", 10),
            entry("EVASION", 10), entry("SPEED", 2)
    );

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
}