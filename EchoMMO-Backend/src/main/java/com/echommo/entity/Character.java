package com.echommo.entity;

import com.echommo.enums.CharacterStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
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

    @Column(name = "avatar_url")
    @Builder.Default
    private String avatarUrl = "https://i.imgur.com/7Y7t5Xp.png";

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
    @Builder.Default private Integer currentHp = 200;
    @Builder.Default private Integer maxHp = 200;
    @Builder.Default private Integer currentEnergy = 100;
    @Builder.Default private Integer maxEnergy = 100;
    @Builder.Default private Integer baseAtk = 10;
    @Builder.Default private Integer baseDef = 5;
    @Builder.Default private Integer baseSpeed = 10;
    @Builder.Default private Integer baseCritRate = 50;
    @Builder.Default private Integer baseCritDmg = 150;

    // --- PVP ---
    @Column(name = "total_power")
    @Builder.Default private Integer totalPower = 0;

    @Column(name = "pvp_points")
    @Builder.Default private Integer pvpPoints = 1000;

    @Column(name = "pvp_wins")
    @Builder.Default private Integer pvpWins = 0;

    @Column(name = "pvp_matches_played")
    @Builder.Default private Integer pvpMatchesPlayed = 0;

    // --- MAP ---
    @Builder.Default
    @Column(name = "current_map_id")
    private String currentMapId = "MAP_01";

    @Builder.Default private String currentLocation = "Làng Tân Thủ";
    @Builder.Default @Column(name = "monster_kills") private Integer monsterKills = 0;

    // --- SPA SYSTEM ---
    @Column(name = "spa_start_time") private LocalDateTime spaStartTime;
    @Column(name = "spa_end_time") private LocalDateTime spaEndTime;
    @Column(name = "spa_package_type") private String spaPackageType;

    // [NEW] Theo dõi lần dùng Spa miễn phí cuối cùng
    @Column(name = "last_free_spa_use")
    private LocalDateTime lastFreeSpaUse;

    // --- GATHERING SYSTEM ---
    // [NEW] Level nghề & Exp nghề
    @Column(name = "gathering_level")
    @Builder.Default
    private Integer gatheringLevel = 1;

    @Column(name = "gathering_exp")
    @Builder.Default
    private Long gatheringExp = 0L;

    @Column(name = "gathering_item_id") private Integer gatheringItemId;
    @Column(name = "gathering_remaining_amount") @Builder.Default private Integer gatheringRemainingAmount = 0;
    @Column(name = "gathering_expiry") private LocalDateTime gatheringExpiry;

    // --- TIME ---
    private LocalDateTime createdAt;
    private LocalDateTime lastActive;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        lastActive = LocalDateTime.now();
    }
}