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

    // 1. TÌM TRẬN
    @Transactional
    public PvpMatch findOrCreateMatch(Integer charId) {
        // Tìm trận đang Active (nếu user F5 lại trang)
        Optional<PvpMatch> existingMatch = matchRepo.findActiveMatchByCharId(charId);
        if (existingMatch.isPresent()) return existingMatch.get();

        Character myChar = charRepo.findById(charId).orElseThrow(() -> new RuntimeException("Character not found"));

        // Kiểm tra xem chính mình có đang trong hàng chờ không
        Optional<PvpQueue> myQueue = queueRepo.findByCharId(charId);
        if (myQueue.isPresent()) return null;

        // Tìm đối thủ
        Optional<PvpQueue> opponentQueue = queueRepo.findMatchCandidate(charId, myChar.getLevel() - 5, myChar.getLevel() + 5)
                .filter(q -> !q.getCharId().equals(charId));

        if (opponentQueue.isPresent()) {
            // --- TRƯỜNG HỢP 1: TÌM THẤY ĐỐI THỦ ---
            PvpQueue opponent = opponentQueue.get();
            Character enemyChar = charRepo.findById(opponent.getCharId()).orElseThrow();
            queueRepo.delete(opponent); // Xóa đối thủ khỏi hàng chờ

            PvpMatch newMatch = new PvpMatch();
            newMatch.setPlayer1(myChar);
            newMatch.setPlayer2(enemyChar);

            // [SỬA QUAN TRỌNG] Set luôn là ACTIVE để đánh được ngay
            newMatch.setStatus("ACTIVE");
            newMatch.setCreatedAt(LocalDateTime.now());
            newMatch.setTurnCount(1);
            newMatch.setP1CurrentHp(myChar.getMaxHp());
            newMatch.setP2CurrentHp(enemyChar.getMaxHp());

            // [SỬA QUAN TRỌNG] Coi như đã chấp nhận luôn
            newMatch.setP1Accepted(true);
            newMatch.setP2Accepted(true);

            return matchRepo.save(newMatch);
        } else {
            // --- TRƯỜNG HỢP 2: KHÔNG THẤY AI -> VÀO HÀNG CHỜ ---
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

    // 2. GỬI NƯỚC ĐI
    @Transactional
    public PvpMatch submitMove(Long matchId, Integer charId, String move) {
        PvpMatch match = matchRepo.findById(matchId).orElseThrow(() -> new RuntimeException("Match not found"));

        // [SỬA QUAN TRỌNG] Nếu trận đang PENDING (do code cũ tạo ra), tự động chuyển sang ACTIVE
        if ("PENDING".equals(match.getStatus())) {
            match.setStatus("ACTIVE");
            match.setP1Accepted(true);
            match.setP2Accepted(true);
        }

        // Chỉ chặn nếu status KHÁC ACTIVE (ví dụ FINISHED)
        if (!"ACTIVE".equals(match.getStatus())) {
            throw new RuntimeException("Match is not active (Status: " + match.getStatus() + ")");
        }

        // Xác định ai đang đánh
        if (match.getPlayer1().getCharId().equals(charId)) {
            match.setP1Move(move);
        } else if (match.getPlayer2().getCharId().equals(charId)) {
            match.setP2Move(move);
        }

        matchRepo.save(match);

        // Nếu cả 2 đã ra đòn -> Xử lý kết quả
        if (match.getP1Move() != null && match.getP2Move() != null) {
            resolveTurn(match);
        }
        return match;
    }

    // 3. XỬ LÝ LƯỢT ĐÁNH
    private void resolveTurn(PvpMatch match) {
        String m1 = match.getP1Move();
        String m2 = match.getP2Move();
        Character p1 = match.getPlayer1();
        Character p2 = match.getPlayer2();
        int hp1 = match.getP1CurrentHp();
        int hp2 = match.getP2CurrentHp();
        StringBuilder log = new StringBuilder();

        if (m1.equals(m2)) {
            log.append("⚔️ HÒA! Cả hai cùng ra ").append(translateMove(m1)).append(". Đấu lại!");
            match.setP1Move(null);
            match.setP2Move(null);
        } else {
            boolean p1WinsRps = (m1.equals("ROCK") && m2.equals("SCISSORS")) ||
                    (m1.equals("PAPER") && m2.equals("ROCK")) ||
                    (m1.equals("SCISSORS") && m2.equals("PAPER"));

            Character attacker = p1WinsRps ? p1 : p2;
            Character defender = p1WinsRps ? p2 : p1;
            String winMove = p1WinsRps ? m1 : m2;

            log.append("⚔️ ").append(attacker.getName()).append(" ra ").append(translateMove(winMove))
                    .append(" thắng! -> TẤN CÔNG.\n");

            // Công thức damage đơn giản: (Atk - Def), tối thiểu 10 damage
            int damage = Math.max(10, attacker.getBaseAtk() - defender.getBaseDef());

            if (p1WinsRps) {
                hp2 = Math.max(0, hp2 - damage);
                match.setP2CurrentHp(hp2);
                log.append("💥 ").append(p1.getName()).append(" gây ").append(damage).append(" sát thương!");
            } else {
                hp1 = Math.max(0, hp1 - damage);
                match.setP1CurrentHp(hp1);
                log.append("💥 ").append(p2.getName()).append(" gây ").append(damage).append(" sát thương!");
            }

            // CHECK KẾT THÚC TRẬN ĐẤU
            if (hp1 <= 0 || hp2 <= 0) {
                Integer winnerCharId = hp1 <= 0 ? p2.getCharId() : p1.getCharId();
                Integer loserCharId = hp1 <= 0 ? p1.getCharId() : p2.getCharId();

                match.setStatus("FINISHED");
                match.setWinnerId(Long.valueOf(winnerCharId));

                log.append("\n🏆 ").append(hp1 <= 0 ? p2.getName() : p1.getName()).append(" ĐÃ CHIẾN THẮNG!");

                updatePvpStats(winnerCharId, loserCharId);
            } else {
                match.setP1Move(null);
                match.setP2Move(null);
                match.setTurnCount(match.getTurnCount() + 1);
            }
        }
        match.setLastLog(log.toString());
        matchRepo.save(match);
    }

    // 4. HÀM LƯU CHAT
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

    // 5. XỬ LÝ KẾT QUẢ KHI CÓ NGƯỜI ĐẦU HÀNG
    @Transactional
    public void processMatchResult(PvpMatch match) {
        if (match.getWinnerId() != null) {
            Integer winnerId = match.getWinnerId().intValue();

            Integer loserId = match.getPlayer1().getCharId().equals(winnerId)
                    ? match.getPlayer2().getCharId()
                    : match.getPlayer1().getCharId();
            updatePvpStats(winnerId, loserId);
        }
    }

    // 6. HỦY TÌM TRẬN
    @Transactional
    public void cancelQueue(Integer charId) {
        Optional<PvpQueue> queueEntry = queueRepo.findByCharId(charId);
        if (queueEntry.isPresent()) {
            queueRepo.delete(queueEntry.get());
        }
    }

    private void updatePvpStats(Integer winnerId, Integer loserId) {
        charRepo.findById(winnerId).ifPresent(c -> {
            c.setPvpWins((c.getPvpWins() == null ? 0 : c.getPvpWins()) + 1);
            c.setPvpMatchesPlayed((c.getPvpMatchesPlayed() == null ? 0 : c.getPvpMatchesPlayed()) + 1);
            // Cộng điểm rank
            c.setPvpPoints(c.getPvpPoints() + 25);
            charRepo.save(c);
        });
        charRepo.findById(loserId).ifPresent(c -> {
            c.setPvpMatchesPlayed((c.getPvpMatchesPlayed() == null ? 0 : c.getPvpMatchesPlayed()) + 1);
            // Trừ điểm rank (không dưới 0)
            c.setPvpPoints(Math.max(0, c.getPvpPoints() - 10));
            charRepo.save(c);
        });
    }

    private String translateMove(String move) {
        if ("ROCK".equals(move)) return "BÚA ✊";
        if ("PAPER".equals(move)) return "BAO ✋";
        return "KÉO ✌️";
    }
}