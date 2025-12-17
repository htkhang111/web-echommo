package com.echommo.controller;

import com.echommo.dto.ExplorationResponse;
import com.echommo.service.ExplorationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
// [SỬA QUAN TRỌNG] Đổi thành /exploration để khớp với Frontend
@RequestMapping("/api/exploration")
@CrossOrigin(origins = "http://localhost:5173") // Cho phép Frontend gọi
public class ExplorationController {

    @Autowired
    private ExplorationService explorationService;

    // API Khám phá (Đánh quái/Nhặt đồ)
    // URL: /api/exploration/explore?mapId=MAP_01
    @PostMapping("/explore")
    public ResponseEntity<ExplorationResponse> explore(@RequestParam String mapId) {
        return ResponseEntity.ok(explorationService.explore(mapId));
    }

    // API Thu hoạch (Đào khoáng/Chặt cây)
    // URL: /api/exploration/gather
    @PostMapping("/gather")
    public ResponseEntity<Map<String, Object>> gather(@RequestBody Map<String, Object> req) {
        String type = (String) req.get("type");
        // Ép kiểu an toàn tránh lỗi
        int amount = Integer.parseInt(req.get("amount").toString());

        return ResponseEntity.ok(explorationService.gatherResource(type, amount));
    }
}