package com.echommo.controller;

import com.echommo.dto.PvpMoveRequest;
import com.echommo.entity.Character;
import com.echommo.entity.PvpMatch;
import com.echommo.entity.PvpQueue;
import com.echommo.entity.User;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.PvpMatchRepository;
import com.echommo.repository.PvpQueueRepository;
import com.echommo.repository.UserRepository;
import com.echommo.service.PvpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pvp")
public class PvpController {

    @Autowired private PvpService pvpService;
    @Autowired private UserRepository userRepo;
    @Autowired private CharacterRepository charRepo;
    @Autowired private PvpMatchRepository matchRepo;
    @Autowired private PvpQueueRepository queueRepo;

    // --- 1. LẤY TRẠNG THÁI ---
    @GetMapping("/status")
    public ResponseEntity<?> getMatchStatus(@AuthenticationPrincipal UserDetails userDetails) {
        Character myChar = getCharacterFromUser(userDetails);
        if (myChar == null) return ResponseEntity.badRequest().body("Character not found");

        MatchResponse response = new MatchResponse();
        response.setMyId(myChar.getCharId());

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

    // --- 2. TÌM TRẬN ---
    @PostMapping("/find")
    public ResponseEntity<?> findMatch(@AuthenticationPrincipal UserDetails userDetails) {
        Character myChar = getCharacterFromUser(userDetails);
        try {
            pvpService.findOrCreateMatch(myChar.getCharId());
            return ResponseEntity.ok("Started searching");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Trong PvpController.java

    // --- 3. CHẤP NHẬN TRẬN ---
    @PostMapping("/accept")
    public ResponseEntity<?> acceptMatch(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody Map<String, Long> payload) {
        Character myChar = getCharacterFromUser(userDetails);
        Long matchId = payload.get("matchId");

        try {
            // Gọi hàm accept trong Service
            pvpService.acceptMatch(matchId, myChar.getCharId());
            return ResponseEntity.ok("Accepted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- [QUAN TRỌNG] 4. RA CHIÊU (ĐÃ SỬA DÙNG DTO) ---
    @PostMapping("/move")
    public ResponseEntity<?> submitMove(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestBody PvpMoveRequest request) { // <--- Dùng Class DTO ở đây
        Character myChar = getCharacterFromUser(userDetails);

        System.out.println("🔥 API Move nhận: ID=" + request.getMatchId() + " Move=" + request.getMove());

        try {
            pvpService.submitMove(request.getMatchId(), myChar.getCharId(), request.getMove());
            return ResponseEntity.ok("Move submitted");
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console server để dễ debug
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- 5. CHAT ---
    @PostMapping("/chat")
    public ResponseEntity<?> sendChat(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestBody Map<String, Object> payload) {
        Character myChar = getCharacterFromUser(userDetails);
        Long matchId = ((Number) payload.get("matchId")).longValue();
        String message = (String) payload.get("message");
        try {
            pvpService.saveChatMessage(matchId, myChar.getCharId(), message);
            return ResponseEntity.ok("Chat sent");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- 6. ĐẦU HÀNG ---
    @PostMapping("/surrender")
    public ResponseEntity<?> surrender(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestBody Map<String, Long> payload) {
        Character myChar = getCharacterFromUser(userDetails);
        Long matchId = payload.get("matchId");
        PvpMatch match = matchRepo.findById(matchId).orElse(null);

        if (match != null && "ACTIVE".equals(match.getStatus())) {
            Integer winnerId = match.getPlayer1().getCharId().equals(myChar.getCharId())
                    ? match.getPlayer2().getCharId()
                    : match.getPlayer1().getCharId();

            match.setStatus("FINISHED");
            match.setWinnerId(Long.valueOf(winnerId));
            match.setLastLog("⚡ " + myChar.getName() + " đã đầu hàng! Đối thủ chiến thắng.");
            pvpService.processMatchResult(match);
            matchRepo.save(match);
        }
        return ResponseEntity.ok("Surrendered");
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelSearch(@AuthenticationPrincipal UserDetails userDetails) {
        Character myChar = getCharacterFromUser(userDetails);
        if (myChar == null) return ResponseEntity.badRequest().body("Character not found");
        try {
            pvpService.cancelQueue(myChar.getCharId());
            return ResponseEntity.ok("Search canceled");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
        res.setWinnerId(match.getWinnerId() != null ? match.getWinnerId().intValue() : null);

        // Map Chat (Giữ nguyên)
        if (match.getChats() != null) {
            List<ChatMessageDTO> chatDtos = match.getChats().stream()
                    .map(chat -> new ChatMessageDTO(chat.getSender().getName(), chat.getSender().getCharId(), chat.getMessage()))
                    .collect(Collectors.toList());
            res.setMessages(chatDtos);
        } else {
            res.setMessages(new ArrayList<>());
        }

        boolean isP1 = match.getPlayer1().getCharId().equals(myCharId);

        // Map Player 1
        res.setP1Id(match.getPlayer1().getCharId());
        res.setP1Name(match.getPlayer1().getName());
        res.setP1Level(match.getPlayer1().getLevel());
        res.setP1Hp(match.getP1CurrentHp());
        res.setP1MaxHp(match.getPlayer1().getMaxHp());
        // [MỚI] Map Avatar Url
        res.setP1AvatarUrl(match.getPlayer1().getAvatarUrl());

        // Map Player 2
        res.setP2Id(match.getPlayer2().getCharId());
        res.setP2Name(match.getPlayer2().getName());
        res.setP2Level(match.getPlayer2().getLevel());
        res.setP2Hp(match.getP2CurrentHp());
        res.setP2MaxHp(match.getPlayer2().getMaxHp());
        // [MỚI] Map Avatar Url
        res.setP2AvatarUrl(match.getPlayer2().getAvatarUrl());

        // Map Moves (Ẩn move của đối thủ nếu chưa kết thúc lượt)
        String p1Move = match.getP1Move();
        String p2Move = match.getP2Move();
        boolean bothMoved = (p1Move != null && p2Move != null);
        if ("FINISHED".equals(match.getStatus())) bothMoved = true;

        if (isP1) {
            res.setP1Move(p1Move);
            res.setP2Move(bothMoved ? p2Move : (p2Move != null ? "HIDDEN" : null));
        } else {
            res.setP2Move(p2Move);
            res.setP1Move(bothMoved ? p1Move : (p1Move != null ? "HIDDEN" : null));
        }
    }

    // --- DTO CLASSES ---
    public static class ChatMessageDTO {
        public String senderName; public Integer senderId; public String content;
        public ChatMessageDTO(String s, Integer id, String c) { this.senderName = s; this.senderId = id; this.content = c; }
    }

    public static class MatchResponse {
        private String status;
        private Long matchId;
        private Integer myId;
        private Integer winnerId;
        private Integer turnCount;
        private String lastLog;

        private Integer p1Id; private String p1Name; private Integer p1Level;
        private Integer p1Hp; private Integer p1MaxHp; private String p1Move;
        private String p1AvatarUrl; // [MỚI]

        private Integer p2Id; private String p2Name; private Integer p2Level;
        private Integer p2Hp; private Integer p2MaxHp; private String p2Move;
        private String p2AvatarUrl; // [MỚI]

        private List<ChatMessageDTO> messages;

        // --- GETTERS & SETTERS ---
        // (Copy các getter setter cũ và thêm 2 cái mới này)
        public String getP1AvatarUrl() { return p1AvatarUrl; }
        public void setP1AvatarUrl(String p1AvatarUrl) { this.p1AvatarUrl = p1AvatarUrl; }

        public String getP2AvatarUrl() { return p2AvatarUrl; }
        public void setP2AvatarUrl(String p2AvatarUrl) { this.p2AvatarUrl = p2AvatarUrl; }

        // ... Các Getter/Setter khác giữ nguyên như cũ ...
        public void setMessages(List<ChatMessageDTO> messages) { this.messages = messages; }
        public List<ChatMessageDTO> getMessages() { return messages; }
        public void setStatus(String s) { this.status = s; } public String getStatus() { return status; }
        public void setMatchId(Long id) { this.matchId = id; } public Long getMatchId() { return matchId; }
        public void setMyId(Integer id) { this.myId = id; } public Integer getMyId() { return myId; }
        public void setWinnerId(Integer id) { this.winnerId = id; } public Integer getWinnerId() { return winnerId; }
        public void setTurnCount(Integer t) { this.turnCount = t; } public Integer getTurnCount() { return turnCount; }
        public void setLastLog(String l) { this.lastLog = l; } public String getLastLog() { return lastLog; }

        public void setP1Id(Integer id) { this.p1Id = id; } public Integer getP1Id() { return p1Id; }
        public void setP1Name(String n) { this.p1Name = n; } public String getP1Name() { return p1Name; }
        public void setP1Level(Integer l) { this.p1Level = l; } public Integer getP1Level() { return p1Level; }
        public void setP1Hp(Integer h) { this.p1Hp = h; } public Integer getP1Hp() { return p1Hp; }
        public void setP1MaxHp(Integer m) { this.p1MaxHp = m; } public Integer getP1MaxHp() { return p1MaxHp; }
        public void setP1Move(String m) { this.p1Move = m; } public String getP1Move() { return p1Move; }

        public void setP2Id(Integer id) { this.p2Id = id; } public Integer getP2Id() { return p2Id; }
        public void setP2Name(String n) { this.p2Name = n; } public String getP2Name() { return p2Name; }
        public void setP2Level(Integer l) { this.p2Level = l; } public Integer getP2Level() { return p2Level; }
        public void setP2Hp(Integer h) { this.p2Hp = h; } public Integer getP2Hp() { return p2Hp; }
        public void setP2MaxHp(Integer m) { this.p2MaxHp = m; } public Integer getP2MaxHp() { return p2MaxHp; }
        public void setP2Move(String m) { this.p2Move = m; } public String getP2Move() { return p2Move; }
    }
}