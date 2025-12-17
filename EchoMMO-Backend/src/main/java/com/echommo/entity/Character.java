package com.echommo.entity;

import com.echommo.enums.CharacterStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "characters")
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "char_id")
    private Integer charId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(nullable = false, unique = true)
    private String name;

    @Builder.Default private Integer level = 1;
    @Builder.Default private Long currentExp = 0L;
    @Column(name = "character_class")
    @Builder.Default private String characterClass = "Nhà Thám Hiểm";
    @Enumerated(EnumType.STRING)
    @Builder.Default private CharacterStatus status = CharacterStatus.IDLE;

    // --- MAIN STATS ---
    @Builder.Default private Integer statPoints = 0;
    @Builder.Default private Integer str = 5;
    @Builder.Default private Integer vit = 5;
    @Builder.Default private Integer agi = 5;
    @Builder.Default private Integer dex = 5;
    @Builder.Default private Integer intelligence = 5;
    @Builder.Default private Integer luck = 5;

    // --- COMPUTED STATS ---
    @Builder.Default private Integer currentHp = 100;
    @Builder.Default private Integer maxHp = 100;
    @Builder.Default private Integer currentEnergy = 50;
    @Builder.Default private Integer maxEnergy = 50;
    @Builder.Default private Integer baseAtk = 10;
    @Builder.Default private Integer baseDef = 5;
    @Builder.Default private Integer baseSpeed = 10;
    @Builder.Default private Integer baseCritRate = 50;
    @Builder.Default private Integer baseCritDmg = 150;
    @Builder.Default private String currentLocation = "Làng Tân Thủ";
    @Builder.Default @Column(name = "monster_kills") private Integer monsterKills = 0;

    // --- SPA SYSTEM ---
    @Column(name = "spa_start_time") private LocalDateTime spaStartTime;
    @Column(name = "spa_end_time") private LocalDateTime spaEndTime;
    @Column(name = "spa_package_type") private String spaPackageType;

    // --- [NEW] GATHERING SECURITY STATE ---
    @Column(name = "gathering_item_id")
    private Integer gatheringItemId;

    @Column(name = "gathering_remaining_amount")
    @Builder.Default
    private Integer gatheringRemainingAmount = 0;

    @Column(name = "gathering_expiry")
    private LocalDateTime gatheringExpiry;

    // --- TIME ---
    private LocalDateTime createdAt;
    private LocalDateTime lastActive;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        lastActive = LocalDateTime.now();
    }
}