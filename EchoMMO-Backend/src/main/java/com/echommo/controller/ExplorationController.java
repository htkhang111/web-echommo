package com.echommo.controller;

import com.echommo.dto.ExplorationResponse;
import com.echommo.dto.GatherRequest; // Import DTO mới
import com.echommo.entity.User;
import com.echommo.service.ExplorationService;
import com.echommo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/exploration")
public class ExplorationController extends BaseController {

    @Autowired
    private ExplorationService explorationService;

    @Autowired
    private UserService userService;

    @PostMapping("/explore")
    public ResponseEntity<ExplorationResponse> explore(@RequestParam String mapId, Authentication auth) {
        User user = userService.getUserFromAuth(auth);
        return ResponseEntity.ok(explorationService.explore(user, mapId));
    }

    // [FIX] SỬ DỤNG @RequestBody VÀ DTO ĐỂ NHẬN JSON TỪ FRONTEND
    @PostMapping("/gather")
    @Transactional
    public ResponseEntity<?> gather(@RequestBody GatherRequest request, Authentication auth) {
        User user = userService.getUserFromAuth(auth);
        try {
            // Gọi service với dữ liệu từ request body
            Map<String, Object> result = explorationService.gatherResource(
                    user,
                    request.getItemId(),
                    request.getAmount() != null ? request.getAmount() : 1
            );
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}