package com.echommo.controller;

import com.echommo.dto.BattleResult;
import com.echommo.service.BattleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/battle")
@CrossOrigin(origins = "http://localhost:5173") // Cho phép Frontend gọi
public class BattleController {

    @Autowired
    private BattleService battleService;

    // 1. Bắt đầu trận đấu
    @PostMapping("/start")
    public ResponseEntity<BattleResult> startBattle() {
        return ResponseEntity.ok(battleService.startBattle());
    }

    // 2. Tấn công tự động (Auto Turn)
    @PostMapping("/attack")
    public ResponseEntity<BattleResult> attack(@RequestBody Map<String, Object> payload) {
        // Mặc định là tấn công thường
        // Nếu sau này muốn dùng Skill, check payload.get("isBuffed") rồi truyền vào Service
        return ResponseEntity.ok(battleService.processTurn("ATTACK"));
    }

    // 3. Thực hiện hành động cụ thể (Dùng cho QTE: BLOCK, IGNORE_QTE...)
    @PostMapping("/action")
    public ResponseEntity<BattleResult> action(@RequestBody Map<String, String> payload) {
        String actionType = payload.getOrDefault("action", "ATTACK");
        return ResponseEntity.ok(battleService.processTurn(actionType));
    }
}