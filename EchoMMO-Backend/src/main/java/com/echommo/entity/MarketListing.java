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

    // NGƯỜI BÁN
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id", nullable = false)
    @JsonIgnoreProperties({"password", "passwordHash", "wallet", "roles", "hibernateLazyInitializer", "handler"})
    private User seller;

    // VẬT PHẨM (UserItem) - Chứa thông tin cường hóa, option
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_item_id")
    @JsonIgnoreProperties({"character", "user", "hibernateLazyInitializer", "handler"}) // Ngắt vòng lặp ở đây
    private UserItem userItem;

    // ITEM GỐC (Để lấy tên/ảnh)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    private Integer quantity;
    private BigDecimal price;
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
    }

    public void setEnhanceLevel(int i) {
    }
}