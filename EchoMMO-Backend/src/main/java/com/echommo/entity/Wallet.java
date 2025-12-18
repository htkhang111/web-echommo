package com.echommo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer walletId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private User user;

    // --- CURRENCY ---
    private BigDecimal gold = BigDecimal.valueOf(100);
    private Integer diamonds = 0;

    // --- GROUP 1: GỖ (ID 1-4) ---
    private Integer wood = 0;          // ID 1
    private Integer driedWood = 0;     // ID 2
    private Integer coldWood = 0;      // ID 3
    private Integer strangeWood = 0;   // ID 4

    // --- GROUP 2: KHOÁNG SẢN (ID 5-8) ---
    private Integer stone = 0;         // ID 5
    private Integer copperOre = 0;     // ID 6
    private Integer ironOre = 0;       // ID 7
    private Integer platinum = 0;      // ID 8

    // --- GROUP 3: THỰC PHẨM (ID 9-10) ---
    private Integer fish = 0;          // ID 9
    private Integer shark = 0;         // ID 10

    // --- GROUP 4: ĐẶC BIỆT (ID 11-12) ---
    private Integer echoCoin = 0;      // ID 11
    private Integer unknownMaterial = 0; // ID 12

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}