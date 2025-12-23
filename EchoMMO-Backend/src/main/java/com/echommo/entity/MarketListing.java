package com.echommo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "market_listings")
@Data
public class MarketListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "listing_id")
    private Long listingId;

    // Sửa lại quan hệ với UserItem cho khớp với database mới
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_item_id", referencedColumnName = "user_item_id", nullable = false)
    private UserItem userItem;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "listed_at")
    private LocalDateTime listedAt;

    @PrePersist
    protected void onCreate() {
        listedAt = LocalDateTime.now();
    }
}