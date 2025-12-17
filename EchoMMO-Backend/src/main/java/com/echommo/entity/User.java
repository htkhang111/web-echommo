package com.echommo.entity;

import com.echommo.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    @JsonIgnore // GIỮ LẠI: Ẩn mật khẩu là đúng
    private String passwordHash;

    @Column(name = "password", nullable = false)
    @JsonIgnore // GIỮ LẠI: Ẩn mật khẩu là đúng
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    // --- CÁC QUAN HỆ ---

    // 1. Quan hệ với Wallet
    // [QUAN TRỌNG] ĐÃ XÓA @JsonIgnore Ở ĐÂY
    // Nếu để @JsonIgnore, Frontend sẽ không bao giờ nhận được số dư Vàng/Ngọc
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Wallet wallet;

    // 2. Quan hệ với Character
    // Thường thì API /user/me không cần trả về Character (đã có API riêng),
    // nhưng nếu bạn muốn hiển thị tên nhân vật ngay trên Header thì có thể BỎ @JsonIgnore ở đây luôn.
    // Tạm thời mình để lại @JsonIgnore để tránh dữ liệu quá lớn.
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Character character;

    // 3. Quan hệ với MarketListing
    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY)
    @JsonIgnore // Giữ lại để tránh load danh sách bán hàng dài dòng không cần thiết
    private List<MarketListing> marketListings;

    // ---------------------------------------------------------

    private Boolean isActive = true;
    private String banReason;
    private LocalDateTime bannedAt;

    private Boolean isCaptchaLocked = false;
    private Integer captchaFailCount = 0;
    private LocalDateTime captchaLockedUntil;

    private String otpCode;
    private LocalDateTime otpExpiry;

    private String avatarUrl = "🐲";

    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}