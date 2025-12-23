package com.echommo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_listings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer listingId;

    // Quan hệ với người bán
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id", nullable = false)
    @JsonIgnoreProperties({
            "password", "passwordHash", "wallet", "roles",
            "hibernateLazyInitializer", "handler",
            "listings", "marketListings", "inventory", "userItems", "tokens", "friends"
    })
    private User seller;

    // Quan hệ với vật phẩm cụ thể (đang chứa stats, rarity, durability...)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_item_id")
    @JsonIgnoreProperties({"character", "user", "hibernateLazyInitializer", "handler"})
    private UserItem userItem;

    // Quan hệ với định nghĩa Item gốc
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    private Integer quantity;

    private BigDecimal price;

    private String status; // ACTIVE, SOLD, CANCELLED

    @Builder.Default
    @Column(name = "currency_type")
    private String currencyType = "GOLD";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
        if (currencyType == null) currencyType = "GOLD";
    }
}