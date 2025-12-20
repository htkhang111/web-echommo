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
@Data // [QUAN TRỌNG] Tự sinh getter/setter (setStatus, setPrice...)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer listingId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id", nullable = false)
    @JsonIgnoreProperties({
            "password", "passwordHash", "wallet", "roles",
            "hibernateLazyInitializer", "handler",
            "listings", "marketListings", "inventory", "userItems", "tokens", "friends"
    })
    private User seller;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_item_id")
    @JsonIgnoreProperties({"character", "user", "hibernateLazyInitializer", "handler"})
    private UserItem userItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    private Integer quantity;

    // [FIX] Dùng BigDecimal để tránh lỗi incompatible types
    private BigDecimal price;

    // [FIX] Dùng String cho status (ACTIVE, SOLD, CANCELLED)
    private String status;

    // [FIX] Thêm trường này để phân loại tiền tệ (GOLD/ECHO)
    @Builder.Default
    @Column(name = "currency_type")
    private String currencyType = "GOLD";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Trường ảo để tránh lỗi code cũ nếu lỡ gọi
    @Transient
    private Integer enhanceLevel;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
        if (currencyType == null) currencyType = "GOLD";
    }
}