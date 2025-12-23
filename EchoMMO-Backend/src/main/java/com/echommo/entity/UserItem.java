package com.echommo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Quan trọng: Khớp với AUTO_INCREMENT trong SQL
    @Column(name = "user_item_id")
    private Long userItemId;

    // Quan trọng: Fix lỗi 'char_id cannot be null' bằng cách bắt buộc quan hệ này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "char_id", nullable = false)
    private Character character;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "durability")
    private int durability;

    @Column(name = "is_equipped")
    private boolean isEquipped;

    // Các field cường hóa/nâng cấp khác (nếu có)
    @Column(name = "upgrade_level")
    private int upgradeLevel = 0;
}