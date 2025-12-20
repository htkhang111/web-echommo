package com.echommo.controller;

import com.echommo.entity.Character;
import com.echommo.entity.PvpMatch;
import com.echommo.entity.PvpQueue;
import com.echommo.entity.User;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.PvpMatchRepository;
import com.echommo.repository.PvpQueueRepository;
import com.echommo.repository.UserRepository;
import com.echommo.service.PvpService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pvp")
public class PvpController {

    @Autowired private PvpService pvpService;
    @Autowired private UserRepository userRepo;
    @Autowired private CharacterRepository charRepo;
    @Autowired private PvpMatchRepository matchRepo;
    @Autowired private PvpQueueRepository queueRepo;

    // --- 1. LẤY TRẠNG THÁI TRẬN ĐẤU (POLLING) ---
    @GetMapping("/status")
    public ResponseEntity<?> getMatchStatus(@AuthenticationPrincipal UserDetails userDetails) {
        Character myChar = getCharacterFromUser(userDetails);
        if (myChar == null) return ResponseEntity.badRequest().body("Character not found");

        MatchResponse response = new MatchResponse();
        response.setMyId(myChar.getCharId());

        // [FIX] LUÔN ĐIỀN THÔNG TIN CỦA MÌNH VÀO SLOT P1 TRƯỚC (Để hiển thị ở Lobby)
        // Frontend sẽ tự nhận diện myId == p1Id để hiển thị Level/Power mới nhất từ DB
        response.setP1Id(myChar.getCharId());
        response.setP1Name(myChar.getName());
        response.setP1Level(myChar.getLevel());
        response.setP1Power(myChar.getTotalPower()); // Lực chiến
        response.setP1Hp(myChar.getCurrentHp());
        response.setP1MaxHp(myChar.getMaxHp());

        // A. Kiểm tra xem có đang trong trận đấu không
        Optional<PvpMatch> matchOpt = matchRepo.findActiveMatchByCharId(myChar.getCharId());
        if (matchOpt.isPresent()) {
            PvpMatch match = matchOpt.get();
            mapMatchToResponse(match, myChar.getCharId(), response);
            return ResponseEntity.ok(response);
        }

        // B. Nếu không trong trận, kiểm tra hàng chờ
        Optional<PvpQueue> queueOpt = queueRepo.findByCharId(myChar.getCharId());
        if (queueOpt.isPresent()) {
            response.setStatus("SEARCHING");
            return ResponseEntity.ok(response);
        }

        // C. Ở sảnh
        response.setStatus("LOBBY");
        return ResponseEntity.ok(response);
    }

    // --- 2. TÌM TRẬN ---
    @PostMapping("/find")
    public ResponseEntity<?> findMatch(@AuthenticationPrincipal UserDetails userDetails) {
        Character myChar = getCharacterFromUser(userDetails);
        if (myChar == null) return ResponseEntity.badRequest().body("Character not found");
        try {
            pvpService.findOrCreateMatch(myChar.getCharId());
            return ResponseEntity.ok("Started searching");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- 3. CHẤP NHẬN ---
    @PostMapping("/accept")
    public ResponseEntity<?> acceptMatch(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody Map<String, Long> payload) {
        Character myChar = getCharacterFromUser(userDetails);
        Long matchId = payload.get("matchId");
        PvpMatch match = matchRepo.findById(matchId).orElse(null);

        if (match == null || !"PENDING".equals(match.getStatus()))
            return ResponseEntity.badRequest().body("Invalid match");

        if (match.getPlayer1().getCharId().equals(myChar.getCharId())) match.setP1Accepted(true);
        else if (match.getPlayer2().getCharId().equals(myChar.getCharId())) match.setP2Accepted(true);

        if (match.isP1Accepted() && match.isP2Accepted()) match.setStatus("ACTIVE");

        matchRepo.save(match);
        return ResponseEntity.ok("Accepted");
    }

    // --- 4. GỬI MOVE ---
    @PostMapping("/move")
    public ResponseEntity<?> submitMove(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestBody Map<String, Object> payload) {
        Character myChar = getCharacterFromUser(userDetails);
        Long matchId = ((Number) payload.get("matchId")).longValue();
        String move = (String) payload.get("move");
        try {
            pvpService.submitMove(matchId, myChar.getCharId(), move);
            return ResponseEntity.ok("Move submitted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ================= HELPER =================

    private Character getCharacterFromUser(UserDetails userDetails) {
        User user = userRepo.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) return null;
        return charRepo.findByUserId(user.getUserId()).orElse(null);
    }

    private void mapMatchToResponse(PvpMatch match, Integer myCharId, MatchResponse res) {
        res.setMatchId(match.getMatchId());
        res.setStatus(match.getStatus());
        res.setTurnCount(match.getTurnCount());
        res.setLastLog(match.getLastLog());
        res.setWinnerId(match.getWinnerId());

        boolean isP1 = match.getPlayer1().getCharId().equals(myCharId);

        // Map Player 1
        res.setP1Id(match.getPlayer1().getCharId());
        res.setP1Name(match.getPlayer1().getName());
        res.setP1Level(match.getPlayer1().getLevel());
        res.setP1Power(match.getPlayer1().getTotalPower());
        res.setP1Hp(match.getP1CurrentHp());
        res.setP1MaxHp(match.getPlayer1().getMaxHp());

        // Map Player 2
        res.setP2Id(match.getPlayer2().getCharId());
        res.setP2Name(match.getPlayer2().getName());
        res.setP2Level(match.getPlayer2().getLevel());
        res.setP2Power(match.getPlayer2().getTotalPower());
        res.setP2Hp(match.getP2CurrentHp());
        res.setP2MaxHp(match.getPlayer2().getMaxHp());

        // Che giấu nước đi
        String p1Move = match.getP1Move();
        String p2Move = match.getP2Move();
        boolean reveal = (p1Move != null && p2Move != null);

        if (isP1) {
            res.setP1Move(p1Move);
            res.setP2Move(reveal ? p2Move : (p2Move != null ? "HIDDEN" : null));
        } else {
            res.setP2Move(p2Move);
            res.setP1Move(reveal ? p1Move : (p1Move != null ? "HIDDEN" : null));
        }
    }

    // ================= DTO CLASS =================
    @Data
    static class MatchResponse {
        private String status;
        private Long matchId;
        private Integer myId;
        private Integer winnerId;

        // Player 1
        private Integer p1Id;
        private String p1Name;
        private Integer p1Level;
        private Integer p1Power; // [NEW] Thêm trường Power
        private Integer p1Hp;
        private Integer p1MaxHp;
        private String p1Move;

        // Player 2
        private Integer p2Id;
        private String p2Name;
        private Integer p2Level;
        private Integer p2Power; // [NEW] Thêm trường Power
        private Integer p2Hp;
        private Integer p2MaxHp;
        private String p2Move;

        private Integer turnCount;
        private String lastLog;
    }
}