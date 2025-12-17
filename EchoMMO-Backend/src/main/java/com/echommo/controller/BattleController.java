package com.echommo.controller;

import com.echommo.dto.BattleResult;
import com.echommo.service.BattleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/battle")
public class BattleController {

    @Autowired
    private BattleService battleService;

    @PostMapping("/start")
    public ResponseEntity<BattleResult> startBattle() {
        return ResponseEntity.ok(battleService.startBattle());
    }

    @PostMapping("/attack")
    public ResponseEntity<BattleResult> attack(@RequestBody Map<String, Object> payload) {
        // Sử dụng phương thức processTurn("ATTACK") hoặc attackEnemy tùy theo BattleService hiện tại
        return ResponseEntity.ok(battleService.processTurn("ATTACK"));
    }
}