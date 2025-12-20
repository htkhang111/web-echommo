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

    @GetMapping("/status")
    public ResponseEntity<?> getMatchStatus(@AuthenticationPrincipal UserDetails userDetails) {
        Character myChar = getCharacterFromUser(userDetails);
        if (myChar == null) return ResponseEntity.badRequest().body("Character not found");

        MatchResponse response = new MatchResponse();
        response.setMyId(myChar.getCharId());

        // Pre-fill thông tin bản thân
        response.setP1Id(myChar.getCharId());
        response.setP1Name(myChar.getName());
        response.setP1Level(myChar.getLevel());
        response.setP1Power(myChar.getTotalPower());
        response.setP1Hp(myChar.getCurrentHp());
        response.setP1MaxHp(myChar.getMaxHp());

        // Mặc định P2
        response.setP2Hp(0);
        response.setP2MaxHp(100);

        Optional<PvpMatch> matchOpt = matchRepo.findActiveMatchByCharId(myChar.getCharId());
        if (matchOpt.isPresent()) {
            PvpMatch match = matchOpt.get();
            mapMatchToResponse(match, myChar.getCharId(), response);
            return ResponseEntity.ok(response);
        }

        Optional<PvpQueue> queueOpt = queueRepo.findByCharId(myChar.getCharId());
        if (queueOpt.isPresent()) {
            response.setStatus("SEARCHING");
            return ResponseEntity.ok(response);
        }

        response.setStatus("LOBBY");
        return ResponseEntity.ok(response);
    }

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

    @PostMapping("/accept")
    public ResponseEntity<?> acceptMatch(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody Map<String, Long> payload) {
        Character myChar = getCharacterFromUser(userDetails);
        Long matchId = payload.get("matchId");
        PvpMatch match = matchRepo.findById(matchId).orElse(null);
        if (match == null || !"PENDING".equals(match.getStatus())) {
            return ResponseEntity.badRequest().body("Invalid match");
        }
        if (match.getPlayer1().getCharId().equals(myChar.getCharId())) {
            match.setP1Accepted(true);
        } else if (match.getPlayer2().getCharId().equals(myChar.getCharId())) {
            match.setP2Accepted(true);
        }
        if (match.isP1Accepted() && match.isP2Accepted()) {
            match.setStatus("ACTIVE");
        }
        matchRepo.save(match);
        return ResponseEntity.ok("Accepted");
    }

    @PostMapping("/move")
    public ResponseEntity<?> submitMove(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestBody Map<String, Object> payload) {
        Character myChar = getCharacterFromUser(userDetails);
        Long matchId;
        if (payload.get("matchId") instanceof Integer) {
            matchId = ((Integer) payload.get("matchId")).longValue();
        } else {
            matchId = ((Number) payload.get("matchId")).longValue();
        }
        String move = (String) payload.get("move");
        try {
            pvpService.submitMove(matchId, myChar.getCharId(), move);
            return ResponseEntity.ok("Move submitted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/surrender")
    public ResponseEntity<?> surrender(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestBody Map<String, Long> payload) {
        Character myChar = getCharacterFromUser(userDetails);
        Long matchId = payload.get("matchId");

        PvpMatch match = matchRepo.findById(matchId).orElse(null);
        if (match == null || !"ACTIVE".equals(match.getStatus())) {
            return ResponseEntity.badRequest().body("Trận đấu không còn khả dụng");
        }

        Integer winnerId = match.getPlayer1().getCharId().equals(myChar.getCharId())
                ? match.getPlayer2().getCharId()
                : match.getPlayer1().getCharId();

        match.setStatus("FINISHED");
        match.setWinnerId(winnerId);
        match.setLastLog(myChar.getName() + " đã xin hàng! Trận đấu kết thúc.");

        matchRepo.save(match);
        return ResponseEntity.ok("Bạn đã đầu hàng.");
    }

    // ================= HELPER METHODS =================
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

        // Player 1
        res.setP1Id(match.getPlayer1().getCharId());
        res.setP1Name(match.getPlayer1().getName());
        res.setP1Level(match.getPlayer1().getLevel());
        res.setP1Power(match.getPlayer1().getTotalPower());
        res.setP1Hp(match.getP1CurrentHp());
        res.setP1MaxHp(match.getPlayer1().getMaxHp());

        // Player 2
        res.setP2Id(match.getPlayer2().getCharId());
        res.setP2Name(match.getPlayer2().getName());
        res.setP2Level(match.getPlayer2().getLevel());
        res.setP2Power(match.getPlayer2().getTotalPower());
        res.setP2Hp(match.getP2CurrentHp());
        res.setP2MaxHp(match.getPlayer2().getMaxHp());

        // Moves
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

    // DTO Class với Getter/Setter thủ công
    public static class MatchResponse {
        private String status;
        private Long matchId;
        private Integer myId;
        private Integer winnerId;

        private Integer p1Id;
        private String p1Name;
        private Integer p1Level;
        private Integer p1Power;
        private Integer p1Hp;
        private Integer p1MaxHp;
        private String p1Move;

        private Integer p2Id;
        private String p2Name;
        private Integer p2Level;
        private Integer p2Power;
        private Integer p2Hp;
        private Integer p2MaxHp;
        private String p2Move;

        private Integer turnCount;
        private String lastLog;

        // Setters
        public void setStatus(String status) { this.status = status; }
        public void setMatchId(Long matchId) { this.matchId = matchId; }
        public void setMyId(Integer myId) { this.myId = myId; }
        public void setWinnerId(Integer winnerId) { this.winnerId = winnerId; }

        public void setP1Id(Integer id) { this.p1Id = id; }
        public void setP1Name(String name) { this.p1Name = name; }
        public void setP1Level(Integer level) { this.p1Level = level; }
        public void setP1Power(Integer power) { this.p1Power = power; }
        public void setP1Hp(Integer hp) { this.p1Hp = hp; }
        public void setP1MaxHp(Integer maxHp) { this.p1MaxHp = maxHp; }
        public void setP1Move(String move) { this.p1Move = move; }

        public void setP2Id(Integer id) { this.p2Id = id; }
        public void setP2Name(String name) { this.p2Name = name; }
        public void setP2Level(Integer level) { this.p2Level = level; }
        public void setP2Power(Integer power) { this.p2Power = power; }
        public void setP2Hp(Integer hp) { this.p2Hp = hp; }
        public void setP2MaxHp(Integer maxHp) { this.p2MaxHp = maxHp; }
        public void setP2Move(String move) { this.p2Move = move; }

        public void setTurnCount(Integer turnCount) { this.turnCount = turnCount; }
        public void setLastLog(String lastLog) { this.lastLog = lastLog; }

        // Getters (cho Jackson serialize)
        public String getStatus() { return status; }
        public Long getMatchId() { return matchId; }
        public Integer getMyId() { return myId; }
        public Integer getWinnerId() { return winnerId; }
        public Integer getP1Id() { return p1Id; }
        public String getP1Name() { return p1Name; }
        public Integer getP1Level() { return p1Level; }
        public Integer getP1Power() { return p1Power; }
        public Integer getP1Hp() { return p1Hp; }
        public Integer getP1MaxHp() { return p1MaxHp; }
        public String getP1Move() { return p1Move; }
        public Integer getP2Id() { return p2Id; }
        public String getP2Name() { return p2Name; }
        public Integer getP2Level() { return p2Level; }
        public Integer getP2Power() { return p2Power; }
        public Integer getP2Hp() { return p2Hp; }
        public Integer getP2MaxHp() { return p2MaxHp; }
        public String getP2Move() { return p2Move; }
        public Integer getTurnCount() { return turnCount; }
        public String getLastLog() { return lastLog; }
    }
}