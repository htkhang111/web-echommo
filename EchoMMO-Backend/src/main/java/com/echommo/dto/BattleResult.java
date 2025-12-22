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

    // [MỚI] Các trường phục vụ tính năng Drop Item
    private boolean hasDrop;        // Có rơi đồ không
    private String dropName;        // Tên vật phẩm
    private String dropRarity;      // Phẩm chất: COMMON, RARE, EPIC...
    private String dropImage;       // URL ảnh vật phẩm
    private boolean inventoryFull;  // Cờ báo túi đầy (để Frontend xử lý hỏi user)
}