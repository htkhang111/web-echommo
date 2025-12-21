package com.echommo.entity;

import com.echommo.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder; // [NEW]
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder // [FIX] Thêm Builder để dùng trong Service
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
    @JsonIgnore
    private String passwordHash;

    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Builder.Default // [FIX] Bắt buộc có dòng này
    private Role role = Role.USER;

    // [NEW] Logic mở rộng kho đồ
    @Column(name = "inventory_slots")
    @Builder.Default
    private Integer inventorySlots = 50;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("user")
    private Wallet wallet;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Character character;

    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MarketListing> marketListings;

    @Builder.Default // [FIX]
    private Boolean isActive = true;
    private String banReason;
    private LocalDateTime bannedAt;

    @Builder.Default // [FIX]
    private Boolean isCaptchaLocked = false;

    @Builder.Default // [FIX]
    private Integer captchaFailCount = 0;

    private LocalDateTime captchaLockedUntil;
    private String otpCode;
    private LocalDateTime otpExpiry;

    @Column(name = "avatar_url")
    @Builder.Default // [FIX]
    private String avatarUrl = "🐲";

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}