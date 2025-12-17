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

    // --- NGƯỜI BÁN (FIX LỖI 500 TẠI ĐÂY) ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id", nullable = false)
    // LuNu chú ý: Tôi đã thêm "listings", "marketListings", "inventory", "userItems" vào dòng dưới
    // Để khi lấy tên người bán, nó KHÔNG lôi theo cả kho đồ của họ nữa.
    @JsonIgnoreProperties({
            "password", "passwordHash", "wallet", "roles",
            "hibernateLazyInitializer", "handler",
            "listings", "marketListings", "inventory", "userItems", "tokens", "friends"
    })
    private User seller;

    // --- VẬT PHẨM (UserItem) ---
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_item_id")
    // Giữ nguyên cái này vì bạn đã làm đúng
    @JsonIgnoreProperties({"character", "user", "hibernateLazyInitializer", "handler"})
    private UserItem userItem;

    // --- ITEM GỐC ---
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

    // Hàm dummy để tránh lỗi code cũ nếu có gọi
    public void setEnhanceLevel(int i) {
    }
}