package com.echommo.controller;

import com.echommo.service.ExplorationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/exploration")
@CrossOrigin(origins = "http://localhost:5173")
public class ExplorationController {

    @Autowired
    private ExplorationService explorationService;

    // 1. API Khám phá (Tìm quái/tài nguyên)
    @PostMapping("/explore")
    public ResponseEntity<?> explore(@RequestBody Map<String, String> req) {
        try {
            String mapId = req.get("mapId");
            if (mapId == null || mapId.isEmpty()) {
                throw new IllegalArgumentException("Thiếu thông tin bản đồ (mapId)!");
            }
            return ResponseEntity.ok(explorationService.explore(mapId));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(400).body(error);
        }
    }

    // 2. API Thu hoạch (Đã fix để nhận itemId và amount)
    @PostMapping("/gather")
    public ResponseEntity<?> gather(@RequestBody Map<String, Object> req) {
        try {
            // Kiểm tra input từ Frontend
            if (req.get("itemId") == null) {
                throw new RuntimeException("Thiếu ID vật phẩm!");
            }

            // Convert an toàn từ JSON sang Integer
            int itemId = Integer.parseInt(req.get("itemId").toString());
            int amount = 1;
            if (req.get("amount") != null) {
                try {
                    amount = (int) Double.parseDouble(req.get("amount").toString());
                } catch (NumberFormatException e) {
                    amount = 1;
                }
            }

            return ResponseEntity.ok(explorationService.gatherResource(itemId, amount));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi thu hoạch: " + e.getMessage());
            // Trả về 400 để Frontend hiển thị lỗi đỏ
            return ResponseEntity.status(400).body(error);
        }
    }
}