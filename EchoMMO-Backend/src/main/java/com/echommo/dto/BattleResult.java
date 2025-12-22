package com.echommo.dto;

import lombok.Data;
import java.util.List;

@Data
public class BattleResult {
    private String enemyName;
    private Integer enemyHp;
    private Integer enemyMaxHp;
    private Integer playerHp;
    private Integer playerMaxHp;
    private List<String> combatLog;
    private String status; // "ONGOING", "VICTORY", "DEFEAT"

    // [MỚI] Thông tin Drop Item
    private boolean hasDrop;
    private String dropName;
    private String dropRarity;
    private String dropImage;
    private boolean inventoryFull;
}