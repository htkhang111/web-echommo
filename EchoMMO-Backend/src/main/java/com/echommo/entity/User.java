package com.echommo.entity;

import com.echommo.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore; // [QUAN TRỌNG] Import cái này
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @JsonIgnore // [FIX] Ẩn mật khẩu khi trả về API
    private String passwordHash;

    @Column(name = "password", nullable = false)
    @JsonIgnore // [FIX] Ẩn mật khẩu plain-text
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    // --- CÁC QUAN HỆ GÂY LỖI VÒNG LẶP (INFINITE RECURSION) ---

    // 1. Quan hệ với Wallet
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // [FIX] Ngắt vòng lặp: User -> Wallet -> User
    private Wallet wallet;

    // 2. Quan hệ với Character
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // [FIX] Ngắt vòng lặp: User -> Character -> User
    private Character character;

    // 3. [BỔ SUNG] Quan hệ với MarketListing (Danh sách đang bán)
    // Cần thêm cái này để Hibernate hiểu quan hệ, nhưng phải Ignore JSON
    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY)
    @JsonIgnore // [FIX] Ngắt vòng lặp: Listing -> User -> Listings
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