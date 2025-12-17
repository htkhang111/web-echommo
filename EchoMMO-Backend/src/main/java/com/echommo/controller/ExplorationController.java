package com.echommo.controller;

import com.echommo.dto.ExplorationResponse;
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

    // API Khám phá - Thêm bọc try-catch để bắt lỗi 500
    @PostMapping("/explore")
    public ResponseEntity<?> explore(@RequestParam String mapId) {
        try {
            return ResponseEntity.ok(explorationService.explore(mapId));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(400).body(error);
        }
    }

    // API Thu hoạch
    @PostMapping("/gather")
    public ResponseEntity<?> gather(@RequestBody Map<String, Object> req) {
        try {
            // Kiểm tra itemId đầu vào
            if (req.get("itemId") == null) {
                throw new RuntimeException("Thiếu ID vật phẩm!");
            }

            int itemId = Integer.parseInt(req.get("itemId").toString());
            int amount = 1;

            // Xử lý amount an toàn hơn
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
            return ResponseEntity.status(400).body(error);
        }
    }
}