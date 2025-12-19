package com.echommo.service;

import com.echommo.dto.ChangePasswordRequest;
import com.echommo.dto.UpdateProfileRequest;
import com.echommo.entity.User;
import com.echommo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // [FIX] Thêm hàm public này để Controller gọi được
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByUsername(username);
    }

    public User updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
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