package com.echommo.controller;

import com.echommo.entity.User;
import com.echommo.enums.Role;
import com.echommo.service.AdminService;
import com.echommo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    // API lấy thống kê server
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    // API phát quà (Cộng tiền)
    @PostMapping("/give-reward")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> giveReward(@RequestBody Map<String, String> payload) {
        try {
            // [FIX] Parse Integer userId
            Integer userId = Integer.parseInt(payload.get("userId"));
            String type = payload.get("type");
            String amount = payload.get("amount");

            adminService.giveReward(userId, type, amount);
            return ResponseEntity.ok("Đã cộng thưởng thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}