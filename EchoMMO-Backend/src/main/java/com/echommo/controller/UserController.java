package com.echommo.controller;

import com.echommo.dto.ChangePasswordRequest;
import com.echommo.dto.UpdateProfileRequest;
import com.echommo.entity.User;
import com.echommo.repository.UserRepository;
import com.echommo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*") // Cho phép Frontend gọi API không bị chặn CORS
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    // --- 1. Lấy thông tin cá nhân (Kèm Vàng/Ngọc) ---
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        // Lấy username từ Token đang đăng nhập
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // [QUAN TRỌNG] Dùng hàm findByUsernameWithWallet thay vì findByUsername thường
        // Hàm này dùng JOIN FETCH để lôi thông tin Wallet lên cùng lúc -> Fix lỗi hiển thị vàng
        User user = userRepository.findByUsernameWithWallet(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }

    // --- 2. Cập nhật thông tin (Tên, Avatar...) ---
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            return ResponseEntity.ok(userService.updateProfile(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- 3. Đổi mật khẩu ---
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            return ResponseEntity.ok(userService.changePassword(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}