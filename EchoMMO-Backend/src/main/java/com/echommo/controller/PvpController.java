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

        // [FIX QUAN TRỌNG] PRE-FILL: Luôn điền thông tin mới nhất của mình vào Slot P1
        // Để khi ở LOBBY, frontend vẫn hiển thị được Level/Power thật từ Database
        response.setP1Id(myChar.getCharId());
        response.setP1Name(myChar.getName());
        response.setP1Level(myChar.getLevel());
        response.setP1Power(myChar.getTotalPower()); // Lực chiến thật
        response.setP1Hp(myChar.getCurrentHp());
        response.setP1MaxHp(myChar.getMaxHp());
        // Slot P2 tạm để null hoặc mặc định
        response.setP2Hp(0);
        response.setP2MaxHp(100);

        // A. Kiểm tra xem có đang trong trận đấu không (ACTIVE hoặc PENDING)
        Optional<PvpMatch> matchOpt = matchRepo.findActiveMatchByCharId(myChar.getCharId());

        if (matchOpt.isPresent()) {
            PvpMatch match = matchOpt.get();
            // Nếu có trận, map lại dữ liệu chính xác của 2 bên
            mapMatchToResponse(match, myChar.getCharId(), response);
            return ResponseEntity.ok(response);
        }

        // B. Nếu không trong trận, kiểm tra xem có đang trong hàng chờ không
        Optional<PvpQueue> queueOpt = queueRepo.findByCharId(myChar.getCharId());
        if (queueOpt.isPresent()) {
            response.setStatus("SEARCHING");
            return ResponseEntity.ok(response);
        }

        // C. Không làm gì cả (Ở sảnh)
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

    // --- 3. CHẤP NHẬN TRẬN ĐẤU ---
    @PostMapping("/accept")
    public ResponseEntity<?> acceptMatch(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody Map<String, Long> payload) {
        Character myChar = getCharacterFromUser(userDetails);
        Long matchId = payload.get("matchId");

        PvpMatch match = matchRepo.findById(matchId).orElse(null);
        if (match == null || !"PENDING".equals(match.getStatus())) {
            return ResponseEntity.badRequest().body("Invalid match");
        }

        // Cập nhật trạng thái chấp nhận
        if (match.getPlayer1().getCharId().equals(myChar.getCharId())) {
            match.setP1Accepted(true);
        } else if (match.getPlayer2().getCharId().equals(myChar.getCharId())) {
            match.setP2Accepted(true);
        }

        // Nếu cả 2 đã chấp nhận -> Vào trận (ACTIVE)
        if (match.isP1Accepted() && match.isP2Accepted()) {
            match.setStatus("ACTIVE");
        }

        matchRepo.save(match);
        return ResponseEntity.ok("Accepted");
    }

    // --- 4. GỬI NƯỚC ĐI (KÉO BÚA BAO) ---
    @PostMapping("/move")
    public ResponseEntity<?> submitMove(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestBody Map<String, Object> payload) {
        Character myChar = getCharacterFromUser(userDetails);

        // Xử lý an toàn khi JSON gửi số (Integer/Long)
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

    // ================= HELPER METHODS =================

    private Character getCharacterFromUser(UserDetails userDetails) {
        User user = userRepo.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) return null;
        return charRepo.findByUserId(user.getUserId()).orElse(null);
    }

    // Map Entity sang JSON Response (DTO)
    private void mapMatchToResponse(PvpMatch match, Integer myCharId, MatchResponse res) {
        res.setMatchId(match.getMatchId());
        res.setStatus(match.getStatus());
        res.setTurnCount(match.getTurnCount());
        res.setLastLog(match.getLastLog());
        res.setWinnerId(match.getWinnerId());

        boolean isP1 = match.getPlayer1().getCharId().equals(myCharId);

        // Map Player 1 Info
        res.setP1Id(match.getPlayer1().getCharId());
        res.setP1Name(match.getPlayer1().getName());
        res.setP1Level(match.getPlayer1().getLevel());
        res.setP1Power(match.getPlayer1().getTotalPower()); // Lực chiến
        res.setP1Hp(match.getP1CurrentHp());
        res.setP1MaxHp(match.getPlayer1().getMaxHp());

        // Map Player 2 Info
        res.setP2Id(match.getPlayer2().getCharId());
        res.setP2Name(match.getPlayer2().getName());
        res.setP2Level(match.getPlayer2().getLevel());
        res.setP2Power(match.getPlayer2().getTotalPower()); // Lực chiến
        res.setP2Hp(match.getP2CurrentHp());
        res.setP2MaxHp(match.getPlayer2().getMaxHp());

        // --- LOGIC CHE GIẤU MOVE ---
        String p1Move = match.getP1Move();
        String p2Move = match.getP2Move();

        // Chỉ hiện move của đối thủ khi cả 2 đã cùng chọn xong
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

        // Player 1 Info
        private Integer p1Id;
        private String p1Name;
        private Integer p1Level;
        private Integer p1Power; // [NEW] Thêm trường Power
        private Integer p1Hp;
        private Integer p1MaxHp;
        private String p1Move;

        // Player 2 Info
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
    // --- 5. ĐẦU HÀNG ---
    @PostMapping("/surrender")
    public ResponseEntity<?> surrender(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestBody Map<String, Long> payload) {
        Character myChar = getCharacterFromUser(userDetails);
        Long matchId = payload.get("matchId");

        PvpMatch match = matchRepo.findById(matchId).orElse(null);
        if (match == null || !"ACTIVE".equals(match.getStatus())) {
            return ResponseEntity.badRequest().body("Trận đấu không còn khả dụng");
        }

        // Xác định người thắng là đối thủ của người bấm nút
        Integer winnerId = match.getPlayer1().getCharId().equals(myChar.getCharId())
                ? match.getPlayer2().getCharId()
                : match.getPlayer1().getCharId();

        match.setStatus("FINISHED");
        match.setWinnerId(winnerId);
        match.setLastLog(myChar.getName() + " đã xin hàng! Trận đấu kết thúc.");

        matchRepo.save(match);
        return ResponseEntity.ok("Bạn đã đầu hàng.");
    }
}