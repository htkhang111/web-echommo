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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "char_id", unique = true, nullable = false)
    @JsonIgnore
    private Character character;

    private Integer enemyId;
    private String enemyName;
    private Integer enemyMaxHp;
    private Integer enemyCurrentHp;
    private Integer enemyAtk;
    private Integer enemyDef;
    private Integer enemySpeed;

    private Integer playerCurrentHp;
    private Integer playerMaxHp;
    private Integer playerCurrentEnergy;

    private Integer currentTurn = 0;

    private boolean isQteActive = false;
    private LocalDateTime qteExpiryTime;

    private LocalDateTime lastActionTime = LocalDateTime.now();
    private Integer spamCount = 0;

    @Column(columnDefinition = "TEXT")
    private String log;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getter/Setter thủ công để tránh lỗi Lombok
    public Character getCharacter() { return character; }
    public void setCharacter(Character character) { this.character = character; }
    public Integer getEnemyId() { return enemyId; }
    public void setEnemyId(Integer enemyId) { this.enemyId = enemyId; }
    public String getEnemyName() { return enemyName; }
    public void setEnemyName(String enemyName) { this.enemyName = enemyName; }
    public Integer getEnemyMaxHp() { return enemyMaxHp; }
    public void setEnemyMaxHp(Integer enemyMaxHp) { this.enemyMaxHp = enemyMaxHp; }
    public Integer getEnemyCurrentHp() { return enemyCurrentHp; }
    public void setEnemyCurrentHp(Integer enemyCurrentHp) { this.enemyCurrentHp = enemyCurrentHp; }
    public Integer getEnemyAtk() { return enemyAtk; }
    public void setEnemyAtk(Integer enemyAtk) { this.enemyAtk = enemyAtk; }
    public Integer getEnemyDef() { return enemyDef; }
    public void setEnemyDef(Integer enemyDef) { this.enemyDef = enemyDef; }
    public Integer getEnemySpeed() { return enemySpeed; }
    public void setEnemySpeed(Integer enemySpeed) { this.enemySpeed = enemySpeed; }
    public Integer getPlayerCurrentHp() { return playerCurrentHp; }
    public void setPlayerCurrentHp(Integer playerCurrentHp) { this.playerCurrentHp = playerCurrentHp; }
    public Integer getPlayerMaxHp() { return playerMaxHp; }
    public void setPlayerMaxHp(Integer playerMaxHp) { this.playerMaxHp = playerMaxHp; }
    public Integer getPlayerCurrentEnergy() { return playerCurrentEnergy; }
    public void setPlayerCurrentEnergy(Integer playerCurrentEnergy) { this.playerCurrentEnergy = playerCurrentEnergy; }
    public Integer getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(Integer currentTurn) { this.currentTurn = currentTurn; }
    public boolean isQteActive() { return isQteActive; }
    public void setQteActive(boolean qteActive) { isQteActive = qteActive; }
    public LocalDateTime getQteExpiryTime() { return qteExpiryTime; }
    public void setQteExpiryTime(LocalDateTime qteExpiryTime) { this.qteExpiryTime = qteExpiryTime; }
}