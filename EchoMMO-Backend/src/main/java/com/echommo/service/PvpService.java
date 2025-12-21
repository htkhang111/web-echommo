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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class PvpService {
    @Autowired private PvpMatchRepository matchRepo;
    @Autowired private CharacterRepository charRepo;
    @Autowired private PvpQueueRepository queueRepo;
    @Autowired private PvpChatRepository chatRepo;
    @Autowired private CharacterService charService; // Tiêm vào để cập nhật chỉ số đồ

    private final Random random = new Random();

    // --- 1. TÌM TRẬN ---
    @Transactional
    public PvpMatch findOrCreateMatch(Integer charId) {
        Optional<PvpMatch> existingMatch = matchRepo.findActiveMatchByCharId(charId);
        if (existingMatch.isPresent()) return existingMatch.get();

        Character myChar = charRepo.findById(charId).orElseThrow(() -> new RuntimeException("Character not found"));
        Optional<PvpQueue> myQueue = queueRepo.findByCharId(charId);
        if (myQueue.isPresent()) return null;

        Optional<PvpQueue> opponentQueue = queueRepo.findMatchCandidate(charId, myChar.getLevel() - 5, myChar.getLevel() + 5)
                .filter(q -> !q.getCharId().equals(charId));

        if (opponentQueue.isPresent()) {
            PvpQueue opponent = opponentQueue.get();
            Character enemyChar = charRepo.findById(opponent.getCharId()).orElseThrow();
            queueRepo.delete(opponent);

            PvpMatch newMatch = new PvpMatch();
            newMatch.setPlayer1(myChar);
            newMatch.setPlayer2(enemyChar);
            newMatch.setStatus("PENDING");
            newMatch.setCreatedAt(LocalDateTime.now());
            newMatch.setUpdatedAt(LocalDateTime.now()); // Mốc thời gian đếm ngược 30s
            newMatch.setTurnCount(1);
            newMatch.setP1CurrentHp(myChar.getMaxHp());
            newMatch.setP2CurrentHp(enemyChar.getMaxHp());
            newMatch.setP1Accepted(false);
            newMatch.setP2Accepted(false);
            newMatch.setLastLog("Đang chờ xác nhận từ hai phía...");
            return matchRepo.save(newMatch);
        } else {
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
        PvpMatch match = matchRepo.findById(matchId).orElseThrow(() -> new RuntimeException("Match not found"));
        if (match.getPlayer1().getCharId().equals(charId)) match.setP1Accepted(true);
        else if (match.getPlayer2().getCharId().equals(charId)) match.setP2Accepted(true);

        if (Boolean.TRUE.equals(match.isP1Accepted()) && Boolean.TRUE.equals(match.isP2Accepted())) {
            match.setStatus("ACTIVE");
            match.setUpdatedAt(LocalDateTime.now()); // Reset mốc 30s bắt đầu hiệp 1
            match.setLastLog("Trận đấu bắt đầu! Hãy chọn nước đi.");
        }
        matchRepo.save(match);
    }

    // --- 3. RA CHIÊU ---
    @Transactional
    public PvpMatch submitMove(Long matchId, Integer charId, String move) {
        PvpMatch match = matchRepo.findById(matchId).orElseThrow(() -> new RuntimeException("Match not found"));
        if (!"ACTIVE".equals(match.getStatus())) return match;

        if (match.getPlayer1().getCharId().equals(charId)) match.setP1Move(move);
        else if (match.getPlayer2().getCharId().equals(charId)) match.setP2Move(move);

        matchRepo.save(match);

        if (match.getP1Move() != null && match.getP2Move() != null) {
            resolveTurn(match);
        }
        return match;
    }

    // --- 4. XỬ LÝ TURN (RPG + TRANG BỊ) ---
    private void resolveTurn(PvpMatch match) {
        String m1 = match.getP1Move();
        String m2 = match.getP2Move();
        match.setLastP1Move(m1);
        match.setLastP2Move(m2);

        Character p1 = match.getPlayer1();
        Character p2 = match.getPlayer2();

        // [FIX] Cập nhật lại chỉ số chuẩn từ trang bị trước khi tính toán
        charService.recalculateStats(p1);
        charService.recalculateStats(p2);

        int hp1 = match.getP1CurrentHp();
        int hp2 = match.getP2CurrentHp();
        StringBuilder log = new StringBuilder();

        if (m1.equals(m2)) {
            int drawDamage = 20;
            hp1 = Math.max(0, hp1 - drawDamage);
            hp2 = Math.max(0, hp2 - drawDamage);
            log.append("⚔️ HÒA! Cùng ra ").append(translateMove(m1)).append(". Nội lực xung khắc! Mất ").append(drawDamage).append(" HP.");
        } else {
            boolean p1Wins = (m1.equals("ROCK") && m2.equals("SCISSORS")) ||
                    (m1.equals("PAPER") && m2.equals("ROCK")) ||
                    (m1.equals("SCISSORS") && m2.equals("PAPER"));

            Character attacker = p1Wins ? p1 : p2;
            Character defender = p1Wins ? p2 : p1;
            String winningMove = p1Wins ? m1 : m2;

            // Né tránh (Dựa trên Speed)
            int dodgeChance = Math.min(60, Math.max(0, 5 + (defender.getBaseSpeed() - attacker.getBaseSpeed())));
            if (random.nextInt(100) < dodgeChance) {
                log.append("💨 ").append(defender.getName()).append(" đã NÉ ĐƯỢC đòn tấn công của ").append(attacker.getName()).append("!");
            } else {
                // Sát thương (Atk - Def)
                int damage = Math.max((int)(attacker.getBaseAtk() * 0.1), attacker.getBaseAtk() - defender.getBaseDef());

                // Chí mạng (Luck/CritRate)
                if (random.nextInt(100) < attacker.getBaseCritRate()) {
                    damage = (int) (damage * (attacker.getBaseCritDmg() / 100.0));
                    log.append("🔥 CHÍ MẠNG! ");
                }

                if (p1Wins) hp2 = Math.max(0, hp2 - damage);
                else hp1 = Math.max(0, hp1 - damage);

                log.append("💥 ").append(attacker.getName()).append(" dùng ").append(translateMove(winningMove))
                        .append(" gây ").append(damage).append(" sát thương.");
            }
        }

        match.setP1CurrentHp(hp1);
        match.setP2CurrentHp(hp2);
        checkAndFinalize(match, hp1, hp2, log);
    }

    // --- 5. XỬ LÝ HẾT GIỜ (TIMEOUT - DAME THEO ĐỐI THỦ) ---
    @Transactional
    public void checkTimeouts() {
        List<PvpMatch> activeMatches = matchRepo.findAllByStatus("ACTIVE");
        LocalDateTime now = LocalDateTime.now();

        for (PvpMatch match : activeMatches) {
            // Quá 35 giây (30s quy định + 5s bù lag)
            if (match.getUpdatedAt() != null && match.getUpdatedAt().plusSeconds(35).isBefore(now)) {
                handleTimeout(match);
            }
        }
    }

    private void handleTimeout(PvpMatch match) {
        StringBuilder log = new StringBuilder("⏰ HẾT GIỜ! ");
        Character p1 = match.getPlayer1();
        Character p2 = match.getPlayer2();
        charService.recalculateStats(p1);
        charService.recalculateStats(p2);

        int hp1 = match.getP1CurrentHp();
        int hp2 = match.getP2CurrentHp();

        if (match.getP1Move() == null && match.getP2Move() == null) {
            // Cả hai cùng treo -> Nhận dame phạt từ Atk đối thủ
            int d1 = Math.max(25, p2.getBaseAtk() - p1.getBaseDef());
            int d2 = Math.max(25, p1.getBaseAtk() - p2.getBaseDef());
            hp1 = Math.max(0, hp1 - d1);
            hp2 = Math.max(0, hp2 - d2);
            log.append("Cả hai bất động, nhận sát thương phạt từ đối phương!");
        } else if (match.getP1Move() == null) {
            // P1 treo -> Nhận sát thương từ P2
            int damage = Math.max(30, p2.getBaseAtk() - p1.getBaseDef());
            hp1 = Math.max(0, hp1 - damage);
            log.append(p1.getName()).append(" mất lượt, bị ").append(p2.getName()).append(" đánh mất ").append(damage).append(" HP.");
        } else {
            // P2 treo
            int damage = Math.max(30, p1.getBaseAtk() - p2.getBaseDef());
            hp2 = Math.max(0, hp2 - damage);
            log.append(p2.getName()).append(" mất lượt, bị ").append(p1.getName()).append(" đánh mất ").append(damage).append(" HP.");
        }

        match.setP1CurrentHp(hp1);
        match.setP2CurrentHp(hp2);
        checkAndFinalize(match, hp1, hp2, log);
    }

    private void checkAndFinalize(PvpMatch match, int hp1, int hp2, StringBuilder log) {
        if (hp1 <= 0 || hp2 <= 0) {
            match.setStatus("FINISHED");
            if (hp1 <= 0 && hp2 <= 0) {
                match.setWinnerId(null);
                log.append("\n💀 HÒA!");
            } else {
                Integer winnerId = (hp1 <= 0) ? match.getPlayer2().getCharId() : match.getPlayer1().getCharId();
                Integer loserId = (hp1 <= 0) ? match.getPlayer1().getCharId() : match.getPlayer2().getCharId();
                match.setWinnerId(Long.valueOf(winnerId));
                log.append("\n🏆 ").append(hp1 <= 0 ? match.getPlayer2().getName() : match.getPlayer1().getName()).append(" CHIẾN THẮNG!");
                updatePvpStats(winnerId, loserId);
            }
        } else {
            // Tiếp tục hiệp sau
            match.setP1Move(null);
            match.setP2Move(null);
            match.setTurnCount(match.getTurnCount() + 1);
            match.setUpdatedAt(LocalDateTime.now()); // Reset mốc 30s
        }
        match.setLastLog(log.toString());
        matchRepo.save(match);
    }

    // --- CÁC HÀM HELPER KHÁC ---
    @Transactional
    public void surrenderMatch(Long matchId, Integer charId) {
        PvpMatch match = matchRepo.findById(matchId).orElse(null);
        if (match != null && !"FINISHED".equals(match.getStatus())) {
            Integer winnerId = match.getPlayer1().getCharId().equals(charId) ? match.getPlayer2().getCharId() : match.getPlayer1().getCharId();
            match.setStatus("FINISHED");
            match.setWinnerId(Long.valueOf(winnerId));
            match.setLastLog("🏳️ Đối thủ đã đầu hàng!");
            updatePvpStats(winnerId, charId);
            matchRepo.save(match);
        }
    }

    public PvpMatch getLatestMatchForUser(Integer charId) {
        Optional<PvpMatch> active = matchRepo.findActiveMatchByCharId(charId);
        if (active.isPresent()) return active.get();
        return matchRepo.findAll().stream()
                .filter(m -> m.getPlayer1().getCharId().equals(charId) || m.getPlayer2().getCharId().equals(charId))
                .max(Comparator.comparing(PvpMatch::getCreatedAt)).orElse(null);
    }

    private void updatePvpStats(Integer wId, Integer lId) {
        charRepo.findById(wId).ifPresent(c -> {
            c.setPvpWins(Optional.ofNullable(c.getPvpWins()).orElse(0) + 1);
            c.setPvpMatchesPlayed(Optional.ofNullable(c.getPvpMatchesPlayed()).orElse(0) + 1);
            c.setPvpPoints(Optional.ofNullable(c.getPvpPoints()).orElse(0) + 25);
            charRepo.save(c);
        });
        charRepo.findById(lId).ifPresent(c -> {
            c.setPvpMatchesPlayed(Optional.ofNullable(c.getPvpMatchesPlayed()).orElse(0) + 1);
            c.setPvpPoints(Math.max(0, Optional.ofNullable(c.getPvpPoints()).orElse(0) - 10));
            charRepo.save(c);
        });
    }

    private String translateMove(String m) {
        if ("ROCK".equals(m)) return "BÚA ✊";
        if ("PAPER".equals(m)) return "BAO ✋";
        return "KÉO ✌️";
    }

    @Transactional
    public void saveChatMessage(Long matchId, Integer senderId, String message) {
        PvpMatch match = matchRepo.findById(matchId).orElseThrow();
        Character sender = charRepo.findById(senderId).orElseThrow();
        PvpChat chat = new PvpChat();
        chat.setMatch(match);
        chat.setSender(sender);
        chat.setMessage(message);
        chat.setTimestamp(new Date());
        chatRepo.save(chat);
    }

    @Transactional
    public void cancelQueue(Integer charId) {
        queueRepo.findByCharId(charId).ifPresent(queueRepo::delete);
    }
}