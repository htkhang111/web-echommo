package com.echommo.service;

import com.echommo.dto.AuthRequest;
import com.echommo.dto.ChangePasswordRequest;
import com.echommo.dto.UpdateProfileRequest;
import com.echommo.entity.User;
import com.echommo.entity.Wallet;
import com.echommo.enums.Role;
import com.echommo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByUsername(username);
    }

    @Transactional
    public User registerUser(AuthRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng!");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setPassword(req.getPassword()); // Dev only
        user.setFullName(req.getFullName());
        user.setAvatarUrl("🐲");
        user.setIsActive(true);
        user.setRole(Role.USER);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setGold(1000L);
        user.setWallet(wallet);

        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();

        // 1. Cập nhật FullName
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        // 2. Cập nhật Username (Check trùng)
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Tên đăng nhập đã tồn tại!");
            }
            user.setUsername(request.getUsername());
        }

        // 3. Cập nhật Email (Check trùng)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email đã được sử dụng!");
            }
            user.setEmail(request.getEmail());
        }

        // 4. Cập nhật Password
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setPassword(request.getPassword());
        }

        // 5. Cập nhật Skin Game
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        // 6. Cập nhật Ảnh Upload
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        return userRepository.save(user);
    }

    public String changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng!");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPassword(request.getNewPassword());
        userRepository.save(user);
        return "Đổi mật khẩu thành công!";
    }
}