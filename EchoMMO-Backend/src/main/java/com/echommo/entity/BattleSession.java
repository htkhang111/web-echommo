package com.echommo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList; // Fix lỗi List
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "battle_sessions")
public class BattleSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "char_id")
    private Character character;

    // --- Thông tin Quái (Snapshot) ---
    // Lưu lại chỉ số quái lúc bắt đầu trận, tránh việc Admin sửa quái giữa chừng làm lỗi trận đấu
    @Column(name = "enemy_id")
    private Integer enemyId;

    @Column(name = "enemy_name")
    private String enemyName;

    @Column(name = "enemy_max_hp")
    private Integer enemyMaxHp;

    @Column(name = "enemy_current_hp")
    private Integer enemyCurrentHp;

    @Column(name = "enemy_atk")
    private Integer enemyAtk;

    @Column(name = "enemy_def")
    private Integer enemyDef;

    @Column(name = "enemy_speed")
    private Integer enemySpeed;

    // --- Thông tin Người chơi (Snapshot) ---
    @Column(name = "player_max_hp")
    private Integer playerMaxHp;

    @Column(name = "player_current_hp")
    private Integer playerCurrentHp;

    @Column(name = "player_current_energy")
    private Integer playerCurrentEnergy;

    // --- Trạng thái trận đấu ---
    @Column(name = "current_turn")
    private Integer currentTurn = 0;

    @Column(columnDefinition = "TEXT")
    private String log; // Lưu log trận đấu dạng JSON hoặc String dài

    // --- Helper Methods ---
    // [FIX] Nếu code cũ có gọi hàm này thì giờ nó sẽ hoạt động
    public void setEnemy(Enemy e) {
        this.enemyId = e.getEnemyId(); // Gọi hàm alias vừa thêm
        this.enemyName = e.getName();
        this.enemyMaxHp = e.getHp();
        this.enemyCurrentHp = e.getHp();
        this.enemyAtk = e.getAtk();
        this.enemyDef = e.getDef();
        this.enemySpeed = e.getSpeed();
    }
}