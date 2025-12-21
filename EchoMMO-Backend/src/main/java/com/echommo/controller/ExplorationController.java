//


package com.echommo.controller;

import com.echommo.entity.User;
import com.echommo.repository.UserRepository;
import com.echommo.service.ExplorationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/exploration")
@CrossOrigin(origins = "*")
public class ExplorationController {

    @Autowired private ExplorationService explorationService;
    @Autowired private UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 1. Explore
    @PostMapping("/explore")
    public ResponseEntity<?> explore(@RequestBody Map<String, String> req) {
        try {
            // [FIX] Nhận trực tiếp chuỗi mã map (Ví dụ: "MAP_01")
            String mapCode = req.get("mapId");

            // Fallback nếu không có mã map thì mặc định MAP_01
            if (mapCode == null || mapCode.isEmpty()) {
                mapCode = "MAP_01";
            }

            // Gọi service với mã map chuẩn
            return ResponseEntity.ok(explorationService.explore(getCurrentUser(), mapCode));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 2. Gather
    @PostMapping("/gather")
    public ResponseEntity<?> gather(@RequestBody Map<String, Object> req) {
        try {
            if (req.get("itemId") == null) throw new RuntimeException("Thiếu Item ID");

            // Xử lý an toàn cho itemId (có thể là Integer hoặc String)
            int itemId = Integer.parseInt(req.get("itemId").toString());

            int amount = 1;
            if (req.get("amount") != null) {
                amount = Integer.parseInt(req.get("amount").toString());
            }

            return ResponseEntity.ok(explorationService.gatherResource(getCurrentUser(), itemId, amount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}