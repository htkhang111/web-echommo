package com.echommo.controller;

import com.echommo.dto.ExplorationResponse;
import com.echommo.service.ExplorationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/explore")
public class ExplorationController {

    @Autowired
    private ExplorationService explorationService;

    @PostMapping("/{mapId}")
    public ResponseEntity<ExplorationResponse> explore(@PathVariable String mapId) {
        return ResponseEntity.ok(explorationService.explore(mapId));
    }

    @PostMapping("/gather")
    public ResponseEntity<Map<String, Object>> gather(@RequestBody Map<String, Object> req) {
        String type = (String) req.get("type");
        // Ép kiểu an toàn từ Object sang int
        int amount = Integer.parseInt(req.get("amount").toString());
        // Gọi phương thức gatherResource(String, int) đã có trong ExplorationService
        return ResponseEntity.ok(explorationService.gatherResource(type, amount));
    }
}