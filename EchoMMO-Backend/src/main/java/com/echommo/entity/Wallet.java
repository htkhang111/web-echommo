package com.echommo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    private Long gold = 0L;
    private Integer diamonds = 0;

    // [FIX] Decimal logic
    @Column(name = "echo_coin", precision = 20, scale = 8)
    private BigDecimal echoCoin = BigDecimal.ZERO;

    private Integer wood = 0;
    private Integer driedWood = 0;
    private Integer coldWood = 0;
    private Integer strangeWood = 0;

    // [FIX] Renamed to coal
    private Integer coal = 0;

    private Integer copperOre = 0;
    private Integer ironOre = 0;
    private Integer platinum = 0;
    private Integer fish = 0;
    private Integer shark = 0;
    private Integer unknownMaterial = 0;
}