//package com.echommo.entity;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//import lombok.Data;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//@Entity
//@Data
//@Table(name = "wallet")
//public class Wallet {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "wallet_id")
//    private Integer walletId;
//
//    // [FIX] Thêm @JsonIgnore để ngắt vòng lặp vô tận
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
//    @JsonIgnore
//    private User user;
//
//    @Column(columnDefinition = "DECIMAL(18,2) DEFAULT 100.00")
//    private BigDecimal gold;
//
//    @Column(columnDefinition = "INT DEFAULT 0")
//    private Integer diamonds;
//
//    @Column(columnDefinition = "INT DEFAULT 0")
//    private Integer wood;
//
//    @Column(columnDefinition = "INT DEFAULT 0")
//    private Integer stone;
//
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//    @PrePersist
//    @PreUpdate
//    public void updateTimestamp() {
//        this.updatedAt = LocalDateTime.now();
//    }
//}
//



package com.echommo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // [QUAN TRỌNG] Để dùng được Wallet.builder() trong Service
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Integer walletId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore       // [BẮT BUỘC] Ngắt vòng lặp JSON vô tận
    @ToString.Exclude // [BẮT BUỘC] Ngắt vòng lặp khi log ra console
    private User user;

    // --- CURRENCY ---
    @Builder.Default
    private BigDecimal gold = BigDecimal.valueOf(100.00);

    @Builder.Default
    private Integer diamonds = 0;

    // --- NHÓM GỖ (WOODS) ---
    @Column(name = "wood")
    @Builder.Default private Integer wood = 0;          // ID 1: Gỗ Xoài

    @Column(name = "dried_wood")
    @Builder.Default private Integer driedWood = 0;     // ID 2: Gỗ Khô

    @Column(name = "cold_wood")
    @Builder.Default private Integer coldWood = 0;      // ID 3: Gỗ Lạnh

    @Column(name = "strange_wood")
    @Builder.Default private Integer strangeWood = 0;   // ID 4: Gỗ Lạ

    // --- NHÓM KHOÁNG SẢN (MINERALS) ---
    @Column(name = "stone")
    @Builder.Default private Integer stone = 0;         // ID 5: Đá

    @Column(name = "copper_ore")
    @Builder.Default private Integer copperOre = 0;     // ID 6: Quặng Đồng

    @Column(name = "iron_ore")
    @Builder.Default private Integer ironOre = 0;       // ID 7: Sắt

    @Column(name = "platinum")
    @Builder.Default private Integer platinum = 0;      // ID 8: Bạch Kim

    // --- NHÓM THỰC PHẨM (FOOD) ---
    @Column(name = "fish")
    @Builder.Default private Integer fish = 0;          // ID 9: Cá

    @Column(name = "shark")
    @Builder.Default private Integer shark = 0;         // ID 10: Cá Mập

    // --- NHÓM ĐẶC BIỆT (SPECIAL) ---
    @Column(name = "echo_coin")
    @Builder.Default private Integer echoCoin = 0;      // ID 11: Echo Coin

    @Column(name = "unknown_material")
    @Builder.Default private Integer unknownMaterial = 0; // ID 12: Nguyên liệu lạ

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
