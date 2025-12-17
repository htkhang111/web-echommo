package com.echommo.controller;

import com.echommo.dto.LeaderboardEntry;
import com.echommo.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@CrossOrigin(origins = "*") // Cho phép Frontend gọi API không bị chặn
public class LeaderboardController {

    @Autowired
    private LeaderboardService leaderboardService;

    // 1. BXH Cấp Độ (Level)
    @GetMapping("/level")
    public ResponseEntity<List<LeaderboardEntry>> getLevelBoard() {
        return ResponseEntity.ok(leaderboardService.getLevelLeaderboard());
    }

    // 2. BXH Tài Phú (Vàng)
    @GetMapping("/wealth")
    public ResponseEntity<List<LeaderboardEntry>> getWealthBoard() {
        return ResponseEntity.ok(leaderboardService.getWealthLeaderboard());
    }

    // 3. BXH Diệt Quái (Monster Kills)
    @GetMapping("/monster")
    public ResponseEntity<List<LeaderboardEntry>> getMonsterBoard() {
        return ResponseEntity.ok(leaderboardService.getMonsterKillLeaderboard());
    }
}