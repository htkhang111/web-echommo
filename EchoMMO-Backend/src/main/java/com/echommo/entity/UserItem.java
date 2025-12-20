package com.echommo.entity;

import com.echommo.enums.Rarity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userItemId;

    // Ngắt vòng lặp JSON để tránh lỗi StackOverflow
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "char_id", nullable = false)
    @JsonIgnore
    private Character character;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Builder.Default
    @Column(name = "is_equipped")
    private Boolean isEquipped = false;

    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "acquired_at")
    private LocalDateTime acquiredAt;

    @Builder.Default
    @Column(name = "enhance_level")
    private Integer enhancementLevel = 0;

    @Enumerated(EnumType.STRING)
    private Rarity rarity;

    @Column(name = "main_stat_type")
    private String mainStatType;

    @Column(name = "main_stat_value")
    private BigDecimal mainStatValue;

    @Column(name = "sub_stats", columnDefinition = "TEXT")
    private String subStats;

    @Builder.Default
    @Column(name = "is_mythic")
    private boolean isMythic = false;

    @Builder.Default
    @Column(name = "mythic_level")
    private Integer mythicLevel = 0;

    @Column(name = "original_main_stat_value")
    private BigDecimal originalMainStatValue;

    @PrePersist
    protected void onCreate() { if (acquiredAt == null) acquiredAt = LocalDateTime.now(); }

    // --- [FIX] HELPER METHODS (QUAN TRỌNG) ---

    // 1. Hàm lấy User nhanh (Fix lỗi EquipmentService gọi .getUser())
    public User getUser() {
        return this.character != null ? this.character.getUser() : null;
    }

    // 2. Alias cho Enhancement Level (Fix lỗi gọi .getLevel())
    public Integer getLevel() {
        return this.enhancementLevel != null ? this.enhancementLevel : 0;
    }

    public void setLevel(Integer level) {
        this.enhancementLevel = level;
    }

    // 3. Alias cho EnhanceLevel (cho chắc cốp)
    public Integer getEnhanceLevel() {
        return this.enhancementLevel != null ? this.enhancementLevel : 0;
    }

    public void setEnhanceLevel(Integer level) {
        this.enhancementLevel = level;
    }
}