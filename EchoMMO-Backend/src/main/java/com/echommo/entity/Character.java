package com.echommo.entity;

import com.echommo.enums.CharacterStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Thêm Builder để dễ tạo object mới
@Table(name = "characters")
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "char_id")
    private Integer charId;

    // --- MỐI QUAN HỆ ---

    // Ngắt vòng lặp JSON: Character -> User -> Character
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    // Lưu ý: Đã XÓA List<UserItem> inventory ở đây để CHẶN ĐỨNG lỗi 500 bên Market.
    // Muốn lấy item, hãy gọi InventoryService.

    // --- THÔNG TIN CƠ BẢN ---

    @Column(nullable = false, unique = true)
    private String name;

    @Builder.Default // Giá trị mặc định khi dùng Builder
    private Integer level = 1;

    // [GÓP Ý] Nên dùng Long cho EXP vì lên level cao số này rất to (vượt 2 tỷ của Integer)
    @Builder.Default
    private Long currentExp = 0L;

    @Column(name = "character_class")
    @Builder.Default
    private String characterClass = "Nhà Thám Hiểm";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CharacterStatus status = CharacterStatus.IDLE;

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

    @Builder.Default private Integer baseCritRate = 50; // 5.0%
    @Builder.Default private Integer baseCritDmg = 150; // 150%

    @Builder.Default
    private String currentLocation = "Làng Tân Thủ";

    @Builder.Default
    @Column(name = "monster_kills")
    private Integer monsterKills = 0;
    // --- SPA SYSTEM ---
    @Column(name = "spa_start_time")
    private LocalDateTime spaStartTime;

    @Column(name = "spa_end_time")
    private LocalDateTime spaEndTime;

    @Column(name = "spa_package_type")
    private String spaPackageType;

    // --- TIME TRACKING ---
    private LocalDateTime createdAt;
    private LocalDateTime lastActive;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        lastActive = LocalDateTime.now();
    }
}