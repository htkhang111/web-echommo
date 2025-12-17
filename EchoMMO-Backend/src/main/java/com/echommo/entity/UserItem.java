package com.echommo.entity;

import com.echommo.enums.Rarity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    // [QUAN TRỌNG] Dùng JsonIgnoreProperties thay vì JsonIgnore
    // Để Frontend vẫn biết item này của char_id nào (để so sánh chủ sở hữu)
    // nhưng KHÔNG load tiếp inventory gây vòng lặp.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "char_id", nullable = false)
    @JsonIgnoreProperties({"inventory", "user", "battleSessions", "hibernateLazyInitializer", "handler"})
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

    // [FIX] Đổi "json" thành "TEXT" để an toàn với mọi Database
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

    // --- GETTER/SETTER THỦ CÔNG (GIỮ NGUYÊN ĐỂ TRÁNH LỖI) ---
    public Long getUserItemId() { return userItemId; }

    public Character getCharacter() { return character; }
    public void setCharacter(Character character) { this.character = character; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public Boolean getIsEquipped() { return isEquipped; }
    public void setIsEquipped(Boolean equipped) { isEquipped = equipped; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getEnhancementLevel() { return enhancementLevel; }
    public void setEnhancementLevel(Integer enhancementLevel) { this.enhancementLevel = enhancementLevel; }

    public String getMainStatType() { return mainStatType; }
    public void setMainStatType(String mainStatType) { this.mainStatType = mainStatType; }

    public BigDecimal getMainStatValue() { return mainStatValue; }
    public void setMainStatValue(BigDecimal mainStatValue) { this.mainStatValue = mainStatValue; }

    public String getSubStats() { return subStats; }
    public void setSubStats(String subStats) { this.subStats = subStats; }

    public Rarity getRarity() { return rarity; }
    public void setRarity(Rarity rarity) { this.rarity = rarity; }

    public void setAcquiredAt(LocalDateTime acquiredAt) { this.acquiredAt = acquiredAt; }

    // Tương thích Mythic
    public boolean isMythic() { return isMythic; }
    public void setMythic(boolean mythic) { isMythic = mythic; }
    public Integer getMythicLevel() { return mythicLevel; }
    public void setMythicLevel(Integer mythicLevel) { this.mythicLevel = mythicLevel; }
    public BigDecimal getOriginalMainStatValue() { return originalMainStatValue; }
    public void setOriginalMainStatValue(BigDecimal val) { this.originalMainStatValue = val; }

    // [QUAN TRỌNG] Alias cho code cũ
    public Integer getEnhanceLevel() { return enhancementLevel; }
    public void setEnhanceLevel(Integer level) { this.enhancementLevel = level; }

    @PrePersist
    protected void onCreate() { if (acquiredAt == null) acquiredAt = LocalDateTime.now(); }
}