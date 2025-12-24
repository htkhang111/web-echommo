package com.echommo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_listings")
@Data
public class MarketListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "listing_id")
    private Long listingId;

    // [FIXED] Đổi LAZY -> EAGER để sửa lỗi Jackson Serialization
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_item_id", referencedColumnName = "user_item_id", nullable = false)
    private UserItem userItem;

    // [FIXED] Đổi LAZY -> EAGER để hiển thị thông tin người bán
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    // [KEEP] Giữ nguyên EAGER
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "status")
    private String status;

    @Column(name = "currency_type")
    private String currencyType;

    @Column(name = "listed_at")
    private LocalDateTime listedAt;

    @PrePersist
    protected void onCreate() {
        listedAt = LocalDateTime.now();
    }
}