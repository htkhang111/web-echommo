package com.echommo.service;

import com.echommo.entity.Character;
import com.echommo.entity.PvpChat;
import com.echommo.entity.PvpMatch;
import com.echommo.entity.PvpQueue;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.PvpChatRepository;
import com.echommo.repository.PvpMatchRepository;
import com.echommo.repository.PvpQueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class PvpService {
    @Autowired private PvpMatchRepository matchRepo;
    @Autowired private CharacterRepository charRepo;
    @Autowired private PvpQueueRepository queueRepo;
    @Autowired private PvpChatRepository chatRepo;

    // --- 1. TÌM TRẬN HOẶC TẠO HÀNG CHỜ ---
    @Transactional
    public PvpMatch findOrCreateMatch(Integer charId) {
        // Kiểm tra xem nhân vật có đang trong trận nào không (ACTIVE hoặc PENDING)
        Optional<PvpMatch> existingMatch = matchRepo.findActiveMatchByCharId(charId);
        if (existingMatch.isPresent()) return existingMatch.get();

        Character myChar = charRepo.findById(charId)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        // Kiểm tra xem đã trong hàng chờ chưa
        Optional<PvpQueue> myQueue = queueRepo.findByCharId(charId);
        if (myQueue.isPresent()) return null; // Đang tìm rồi, không làm gì cả

        // Tìm đối thủ trong hàng chờ (Chênh lệch level +/- 5)
        Optional<PvpQueue> opponentQueue = queueRepo.findMatchCandidate(charId, myChar.getLevel() - 5, myChar.getLevel() + 5)
                .filter(q -> !q.getCharId().equals(charId));

        if (opponentQueue.isPresent()) {
            // ==> TÌM THẤY ĐỐI THỦ
            PvpQueue opponent = opponentQueue.get();
            Character enemyChar = charRepo.findById(opponent.getCharId()).orElseThrow();

            // Xóa cả 2 khỏi hàng chờ (nếu có)
            queueRepo.delete(opponent);

            // Tạo trận đấu mới
            PvpMatch newMatch = new PvpMatch();
            newMatch.setPlayer1(myChar);
            newMatch.setPlayer2(enemyChar);
            newMatch.setStatus("PENDING"); // Chờ 2 bên bấm "Chấp nhận"
            newMatch.setCreatedAt(LocalDateTime.now());
            newMatch.setTurnCount(1);
            newMatch.setP1CurrentHp(myChar.getMaxHp());
            newMatch.setP2CurrentHp(enemyChar.getMaxHp());
            newMatch.setP1Accepted(false);
            newMatch.setP2Accepted(false);
            newMatch.setLastLog("Đang chờ xác nhận từ hai phía...");

            return matchRepo.save(newMatch);
        } else {
            // ==> KHÔNG THẤY AI, TỰ VÀO HÀNG CHỜ
            PvpQueue newQueue = new PvpQueue();
            newQueue.setCharId(charId);
            newQueue.setLevel(myChar.getLevel());
            newQueue.setPower(myChar.getTotalPower());
            newQueue.setStatus("SEARCHING");
            newQueue.setJoinedAt(LocalDateTime.now());
            queueRepo.save(newQueue);
            return null;
        }
    }

    // --- 2. CHẤP NHẬN TRẬN ĐẤU ---
    @Transactional
    public void acceptMatch(Long matchId, Integer charId) {
        PvpMatch match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (match.getPlayer1().getCharId().equals(charId)) {
            match.setP1Accepted(true);
        } else if (match.getPlayer2().getCharId().equals(charId)) {
            match.setP2Accepted(true);
        }

        // Nếu cả 2 đã chấp nhận -> Vào trận (ACTIVE)
        if (Boolean.TRUE.equals(match.isP1Accepted()) && Boolean.TRUE.equals(match.isP2Accepted())) {
            match.setStatus("ACTIVE");
            match.setLastLog("Trận đấu bắt đầu! Hãy chọn nước đi.");
        }
        matchRepo.save(match);
    }

    // --- 3. GỬI NƯỚC ĐI (KÉO BÚA BAO) ---
    @Transactional
    public PvpMatch submitMove(Long matchId, Integer charId, String move) {
        PvpMatch match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (!"ACTIVE".equals(match.getStatus())) {
            throw new RuntimeException("Match is not active (Status: " + match.getStatus() + ")");
        }

        // Lưu nước đi tạm thời
        if (match.getPlayer1().getCharId().equals(charId)) {
            match.setP1Move(move);
        } else if (match.getPlayer2().getCharId().equals(charId)) {
            match.setP2Move(move);
        }

        matchRepo.save(match);

        // Nếu cả 2 đã đánh -> Tính toán kết quả ngay
        if (match.getP1Move() != null && match.getP2Move() != null) {
            resolveTurn(match);
        }
        return match;
    }

    // --- 4. XỬ LÝ LƯỢT ĐÁNH (LOGIC MỚI: HÒA TRỪ MÁU) ---
    private void resolveTurn(PvpMatch match) {
        String m1 = match.getP1Move();
        String m2 = match.getP2Move();

        // [QUAN TRỌNG] Lưu lại lịch sử nước đi để Frontend hiển thị Animation
        match.setLastP1Move(m1);
        match.setLastP2Move(m2);

        Character p1 = match.getPlayer1();
        Character p2 = match.getPlayer2();
        int hp1 = match.getP1CurrentHp();
        int hp2 = match.getP2CurrentHp();

        StringBuilder log = new StringBuilder();

        // --- A. TÍNH TOÁN SÁT THƯƠNG ---
        if (m1.equals(m2)) {
            // === LOGIC MỚI: HÒA LÀ CẢ 2 CÙNG MẤT MÁU ===
            int drawDamage = 15; // Lượng máu mất khi hòa (Ông có thể chỉnh số này)

            hp1 = Math.max(0, hp1 - drawDamage);
            hp2 = Math.max(0, hp2 - drawDamage);

            log.append("⚔️ HÒA! Cùng ra ").append(translateMove(m1))
                    .append(". Nội lực xung khắc! Cả hai mất ").append(drawDamage).append(" HP.");
        } else {
            // === LOGIC CŨ: PHÂN ĐỊNH THẮNG THUA ===
            boolean p1WinsRps = (m1.equals("ROCK") && m2.equals("SCISSORS")) ||
                    (m1.equals("PAPER") && m2.equals("ROCK")) ||
                    (m1.equals("SCISSORS") && m2.equals("PAPER"));

            Character attacker = p1WinsRps ? p1 : p2;
            Character defender = p1WinsRps ? p2 : p1;
            String winMove = p1WinsRps ? m1 : m2;

            // Tính damage (Damage = Công - Thủ, tối thiểu 10)
            int damage = Math.max(10, attacker.getBaseAtk() - defender.getBaseDef());

            if (p1WinsRps) {
                hp2 = Math.max(0, hp2 - damage);
                log.append("💥 ").append(p1.getName()).append(" dùng ").append(translateMove(winMove))
                        .append(" đánh trúng! ").append(p2.getName()).append(" mất ").append(damage).append(" HP.");
            } else {
                hp1 = Math.max(0, hp1 - damage);
                log.append("💥 ").append(p2.getName()).append(" dùng ").append(translateMove(winMove))
                        .append(" đánh trúng! ").append(p1.getName()).append(" mất ").append(damage).append(" HP.");
            }
        }

        // Cập nhật HP mới vào Match
        match.setP1CurrentHp(hp1);
        match.setP2CurrentHp(hp2);

        // --- B. KIỂM TRA KẾT THÚC TRẬN ĐẤU ---

        // Trường hợp 1: Cả 2 cùng hết máu (Double KO do Hòa hoặc Phản đòn)
        if (hp1 <= 0 && hp2 <= 0) {
            match.setStatus("FINISHED");
            match.setWinnerId(null); // Không ai thắng
            log.append("\n💀 LƯỠNG BẠI CÂU THƯƠNG! Cả hai đều gục ngã. Trận đấu kết thúc không phân thắng bại.");
            // Logic tùy chọn: Có thể trừ điểm cả 2 hoặc không ai bị trừ
        }
        // Trường hợp 2: Có 1 người thua
        else if (hp1 <= 0 || hp2 <= 0) {
            Integer winnerCharId = hp1 <= 0 ? p2.getCharId() : p1.getCharId();
            Integer loserCharId = hp1 <= 0 ? p1.getCharId() : p2.getCharId();

            match.setStatus("FINISHED");
            match.setWinnerId(Long.valueOf(winnerCharId));
            log.append("\n🏆 ").append(hp1 <= 0 ? p2.getName() : p1.getName()).append(" ĐÃ CHIẾN THẮNG!");

            updatePvpStats(winnerCharId, loserCharId);
        }
        // Trường hợp 3: Chưa ai thua -> Reset nước đi hiện tại để đánh turn tiếp theo
        else {
            match.setP1Move(null);
            match.setP2Move(null);
            match.setTurnCount(match.getTurnCount() + 1);
        }

        match.setLastLog(log.toString());
        matchRepo.save(match);
    }

    // --- 5. LƯU TIN NHẮN CHAT ---
    @Transactional
    public void saveChatMessage(Long matchId, Integer senderId, String message) {
        PvpMatch match = matchRepo.findById(matchId).orElseThrow(() -> new RuntimeException("Match not found"));
        Character sender = charRepo.findById(senderId).orElseThrow(() -> new RuntimeException("Character not found"));

        PvpChat chat = new PvpChat();
        chat.setMatch(match);
        chat.setSender(sender);
        chat.setMessage(message);
        chat.setTimestamp(new Date());

        chatRepo.save(chat);
    }

    // --- 6. CẬP NHẬT ĐIỂM SỐ SAU TRẬN ---
    private void updatePvpStats(Integer winnerId, Integer loserId) {
        // Cộng điểm người thắng
        charRepo.findById(winnerId).ifPresent(c -> {
            c.setPvpWins((c.getPvpWins() == null ? 0 : c.getPvpWins()) + 1);
            c.setPvpMatchesPlayed((c.getPvpMatchesPlayed() == null ? 0 : c.getPvpMatchesPlayed()) + 1);
            c.setPvpPoints(c.getPvpPoints() + 25); // Thắng +25 điểm
            charRepo.save(c);
        });

        // Trừ điểm người thua
        charRepo.findById(loserId).ifPresent(c -> {
            c.setPvpMatchesPlayed((c.getPvpMatchesPlayed() == null ? 0 : c.getPvpMatchesPlayed()) + 1);
            c.setPvpPoints(Math.max(0, c.getPvpPoints() - 10)); // Thua -10 điểm
            charRepo.save(c);
        });
    }

    // --- 7. HỦY TÌM TRẬN ---
    @Transactional
    public void cancelQueue(Integer charId) {
        Optional<PvpQueue> queueEntry = queueRepo.findByCharId(charId);
        if (queueEntry.isPresent()) {
            queueRepo.delete(queueEntry.get());
        }
    }

    // --- 8. ĐẦU HÀNG ---
    @Transactional
    public void surrenderMatch(Long matchId, Integer charId) {
        // Logic xử lý đầu hàng (người kia thắng)
        PvpMatch match = matchRepo.findById(matchId).orElse(null);
        if (match != null && "ACTIVE".equals(match.getStatus())) {
            Integer winnerId = match.getPlayer1().getCharId().equals(charId)
                    ? match.getPlayer2().getCharId()
                    : match.getPlayer1().getCharId();

            match.setStatus("FINISHED");
            match.setWinnerId(Long.valueOf(winnerId));
            match.setLastLog("🏳️ Đối thủ đã đầu hàng! Chiến thắng thuộc về " + (match.getPlayer1().getCharId().equals(winnerId) ? match.getPlayer1().getName() : match.getPlayer2().getName()));

            updatePvpStats(winnerId, charId);
            matchRepo.save(match);
        }
    }

    // --- UTILS ---
    private String translateMove(String move) {
        if ("ROCK".equals(move)) return "BÚA ✊";
        if ("PAPER".equals(move)) return "BAO ✋";
        return "KÉO ✌️";
    }
}