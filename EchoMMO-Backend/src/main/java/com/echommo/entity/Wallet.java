package com.echommo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Integer walletId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore // Ngắt vòng lặp JSON khi gọi API User
    @ToString.Exclude
    private User user;

    // --- Currency ---
    @Column(precision = 18, scale = 2)
    private BigDecimal gold = BigDecimal.valueOf(100.00);

    @Column(name = "diamonds")
    private Integer diamonds = 0; // Legacy

    @Column(name = "echo_coin")
    private Integer echoCoin = 0; // New Currency

    // --- Resources ---
    // Gỗ (Wood)
    private Integer wood = 0;        // ID 1
    private Integer driedWood = 0;   // ID 2
    private Integer coldWood = 0;    // ID 3
    private Integer strangeWood = 0; // ID 4

    // Khoáng sản (Minerals)
    private Integer coal = 0;        // [FIX] ID 5 (Đã đổi từ Stone -> Coal)
    private Integer copperOre = 0;   // ID 6
    private Integer ironOre = 0;     // ID 7
    private Integer platinum = 0;    // ID 8

    // Thực phẩm (Food/Fish)
    private Integer fish = 0;        // ID 9
    private Integer shark = 0;       // ID 10

    // Khác
    private Integer unknownMaterial = 0; // ID 12

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}