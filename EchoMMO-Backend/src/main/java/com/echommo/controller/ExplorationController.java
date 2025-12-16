package com.echommo.controller;

import com.echommo.dto.ExplorationResponse;
import com.echommo.service.ExplorationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/exploration")
public class ExplorationController {
    @Autowired private ExplorationService explorationService;

    // [MODIFIED] Nhận tham số mapId
    @PostMapping("/explore")
    public ResponseEntity<?> explore(@RequestBody Map<String, String> payload) {
        try {
            String mapId = payload.getOrDefault("mapId", "MAP_01");
            return ResponseEntity.ok(explorationService.explore(mapId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/gather")
    public ResponseEntity<?> gather(@RequestBody Map<String, Object> payload) {
        try {
            return ResponseEntity.ok(explorationService.gatherResource((String)payload.get("type"), (int)payload.get("amount")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}