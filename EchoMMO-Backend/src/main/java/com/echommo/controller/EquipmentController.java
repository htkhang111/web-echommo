package com.echommo.controller;

import com.echommo.entity.User;
import com.echommo.service.EquipmentService;
import com.echommo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final UserService userService;

    // Enhance thường (+1 -> +30)
    @PostMapping("/enhance/{userItemId}")
    public ResponseEntity<?> enhance(@PathVariable Long userItemId) {
        try {
            return ResponseEntity.ok(equipmentService.enhanceItem(userItemId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Đột phá lên Mythic (Lv 30 -> Mythic 1 Sao)
    @PostMapping("/evolve-mythic/{userItemId}")
    public ResponseEntity<?> evolve(@PathVariable Long userItemId) {
        try {
            return ResponseEntity.ok(equipmentService.evolveToMythic(userItemId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // [NEW] Nâng cấp sao Mythic (1 Sao -> 10 Sao)
    @PostMapping("/enhance-mythic-star/{userItemId}")
    public ResponseEntity<?> enhanceMythicStar(@PathVariable Long userItemId, Authentication auth) {
        User user = userService.getUserFromAuth(auth);
        try {
            // Truyền UserId để check chính chủ
            return ResponseEntity.ok(equipmentService.enhanceMythicStars(userItemId, user.getUserId()));
        } catch (RuntimeException e) {
            // Frontend cần check message này để hiện popup đặc biệt
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}