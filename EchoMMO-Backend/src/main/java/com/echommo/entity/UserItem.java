package com.echommo.entity;

import com.echommo.enums.Rarity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // --- [QUAN TRỌNG] THAY ĐỔI TỪ USER -> CHARACTER ---
    // Để mỗi nhân vật có túi đồ riêng biệt
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "char_id", nullable = false)
    @JsonIgnore
    private Character character;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // --- BASIC STATS ---
    @Column(name = "is_equipped")
    private Boolean isEquipped = false;

    private Integer quantity = 1;

    @Column(name = "acquired_at")
    private LocalDateTime acquiredAt;

    // --- UPGRADE SYSTEM ---
    @Column(name = "enhance_level")
    private Integer enhancementLevel = 0;

    // Helper method: Để tương thích với code cũ nếu lỡ gọi getEnhanceLevel
    public Integer getEnhanceLevel() {
        return this.enhancementLevel;
    }

    public void setEnhanceLevel(Integer level) {
        this.enhancementLevel = level;
    }

    // --- STATS SYSTEM ---
    @Enumerated(EnumType.STRING)
    private Rarity rarity; // Phẩm chất riêng của item này (VD: Kiếm gỗ nhưng phẩm Legendary)

    @Column(name = "main_stat_type")
    private String mainStatType;

    @Column(name = "main_stat_value")
    private BigDecimal mainStatValue;

    // Lưu Substats dưới dạng JSON String
    // Nếu database báo lỗi JSON type, hãy đổi thành columnDefinition = "TEXT"
    @Column(name = "sub_stats", columnDefinition = "json")
    private String subStats;

    // --- MYTHIC / RED EVOLUTION FIELDS ---
    @Column(name = "is_mythic")
    private boolean isMythic = false;

    @Column(name = "mythic_level")
    private Integer mythicLevel = 0;

    // Lưu chỉ số Main gốc để nhân % khi lên cấp
    @Column(name = "original_main_stat_value")
    private BigDecimal originalMainStatValue;

    @Column(name = "luck_points")
    private Integer luckPoints = 0;

    @PrePersist
    protected void onCreate() {
        if (acquiredAt == null) {
            acquiredAt = LocalDateTime.now();
        }
    }
}