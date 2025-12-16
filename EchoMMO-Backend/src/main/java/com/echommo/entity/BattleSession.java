package com.echommo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "battle_sessions")
public class BattleSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [FIX] Liên kết với Character để ExplorationService gọi được setCharacter()
    @OneToOne
    @JoinColumn(name = "character_id", unique = true, nullable = false)
    private Character character;

    // --- Enemy Info (Snapshot) ---
    private Integer enemyId;
    private String enemyName;
    private Integer enemyMaxHp;
    private Integer enemyCurrentHp;
    private Integer enemyAtk;
    private Integer enemyDef;
    private Integer enemySpeed;

    // --- Player State ---
    private Integer playerCurrentHp;
    private Integer playerMaxHp;
    private Integer playerCurrentEnergy;

    private Integer currentTurn = 0;

    // --- QTE System ---
    private boolean isQteActive = false;
    private LocalDateTime qteExpiryTime;

    // --- [NEW] Anti-Cheat Fields ---
    private LocalDateTime lastActionTime = LocalDateTime.now();
    private Integer spamCount = 0;

    // --- Log System (Quan trọng để hiển thị text mở đầu) ---
    @Column(columnDefinition = "TEXT")
    private String log;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}