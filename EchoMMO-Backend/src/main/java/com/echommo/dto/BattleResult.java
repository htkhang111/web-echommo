package com.echommo.dto;

import com.echommo.entity.BattleSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleResult {
    private String enemyName;
    private Integer enemyHp;
    private Integer enemyMaxHp;
    private Integer playerHp;
    private Integer playerMaxHp;
    private List<String> combatLog;
    private String status; // ONGOING, VICTORY, DEFEAT

    // [FIX] Constructor này giúp map từ Session mới sang Result nhanh gọn
    public BattleResult(BattleSession session, String status, List<String> logs) {
        // Map các trường "phẳng" từ BattleSession
        this.enemyName = session.getEnemyName();
        this.enemyHp = session.getEnemyCurrentHp();
        this.enemyMaxHp = session.getEnemyMaxHp();

        this.playerHp = session.getPlayerCurrentHp();
        this.playerMaxHp = session.getPlayerMaxHp();

        this.status = status;
        this.combatLog = logs;
    }
}