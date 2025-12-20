package com.echommo.entity;

import com.echommo.enums.Rarity;
import com.echommo.enums.SlotType;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Map tự động với cột 'item_id' trong SQL mới
    private Integer itemId;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String type; // Giữ String để tương thích với DataInitializer cũ

    @Enumerated(EnumType.STRING)
    private SlotType slotType;

    private Integer tier;

    // [FIX LỖI COMPILER]
    // Dùng Enum Rarity để khớp với DataInitializer.java (item.setRarity(...))
    // Map vào cột 'base_rarity' trong Database
    @Enumerated(EnumType.STRING)
    @Column(name = "base_rarity")
    private Rarity rarity;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_system_item")
    private Boolean isSystemItem;

    @Column(name = "atk_bonus")
    private Integer atkBonus;

    @Column(name = "def_bonus")
    private Integer defBonus;

    @Column(name = "hp_bonus")
    private Integer hpBonus;

    @Column(name = "speed_bonus")
    private Integer speedBonus;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}