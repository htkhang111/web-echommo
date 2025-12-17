package com.echommo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // [FIX QUAN TRỌNG]
    // 1. Dùng @JsonIgnore để API không trả về toàn bộ thông tin nhân vật trong BattleSession
    // 2. nullable = false để đảm bảo không bao giờ có session "ma" không chủ
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "char_id", unique = true, nullable = false)
    @JsonIgnore
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

    // --- Anti-Cheat Fields ---
    private LocalDateTime lastActionTime = LocalDateTime.now();
    private Integer spamCount = 0;

    // --- Log System ---
    @Column(columnDefinition = "TEXT")
    private String log;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}