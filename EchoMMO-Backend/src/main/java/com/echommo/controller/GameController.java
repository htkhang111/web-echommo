package com.echommo.controller;

import com.echommo.entity.User;
import com.echommo.repository.UserRepository;
import com.echommo.service.CharacterService;
import com.echommo.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    @Autowired private GameService gameService;
    @Autowired private CharacterService characterService;
    @Autowired private UserRepository userRepo;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/character")
    public ResponseEntity<?> getMyCharacterInfo() {
        try {
            User user = getCurrentUser();

            if (user.getCharacter() == null) {
                return ResponseEntity.ok(characterService.createDefaultCharacter(user));
            }

            return ResponseEntity.ok(user.getCharacter());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi Server: " + e.getMessage()));
        }
    }

    @PostMapping("/explore")
    public ResponseEntity<Map<String, Object>> explore() {
        try {
            return ResponseEntity.ok(gameService.explore(getCurrentUser().getUserId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
