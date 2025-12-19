package com.echommo.entity;

import com.echommo.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // [QUAN TRỌNG] Nhớ import cái này
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Role role = Role.USER;

    // --- QUAN HỆ VÍ TIỀN (NGUYÊN NHÂN GÂY LỖI 500) ---
    // [FIX] Thêm dòng này để khi lấy User thì lấy Wallet, nhưng trong Wallet KHÔNG lấy ngược lại User nữa
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("user")
    private Wallet wallet;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Character character;

    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MarketListing> marketListings;

    // --- CÁC TRƯỜNG PHỤ ---
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