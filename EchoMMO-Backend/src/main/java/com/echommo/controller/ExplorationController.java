package com.echommo.controller;

import com.echommo.entity.User;
import com.echommo.repository.UserRepository;
import com.echommo.service.ExplorationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    // 1. Explore (60/20/11/9 Logic)
    @PostMapping("/explore")
    public ResponseEntity<?> explore(@RequestBody Map<String, String> req) {
        try {
            // ... Logic convert mapId như cũ ...
            int mapId = 1;
            try { mapId = Integer.parseInt(req.get("mapId")); } catch (Exception e) {}

            return ResponseEntity.ok(explorationService.explore(getCurrentUser(), mapId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 2. Gather (Dành cho 20% trường hợp ra mỏ)
    @PostMapping("/gather")
    public ResponseEntity<?> gather(@RequestBody Map<String, Object> req) {
        try {
            if (req.get("itemId") == null) throw new RuntimeException("Thiếu Item ID");
            int itemId = Integer.parseInt(req.get("itemId").toString());

            int amount = 1; // Mặc định thu hoạch 1 cái mỗi lần click
            if (req.get("amount") != null) amount = Integer.parseInt(req.get("amount").toString());

            return ResponseEntity.ok(explorationService.gatherResource(getCurrentUser(), itemId, amount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}