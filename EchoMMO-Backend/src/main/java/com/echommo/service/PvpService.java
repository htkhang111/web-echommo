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

    // --- 2. CHẤP NHẬN ---
    @Transactional
    public void acceptMatch(Long matchId, Integer charId) {
        PvpMatch match = matchRepo.findById(matchId).orElseThrow(() -> new RuntimeException("Match not found"));
        if (match.getPlayer1().getCharId().equals(charId)) match.setP1Accepted(true);
        else if (match.getPlayer2().getCharId().equals(charId)) match.setP2Accepted(true);

        if (Boolean.TRUE.equals(match.isP1Accepted()) && Boolean.TRUE.equals(match.isP2Accepted())) {
            match.setStatus("ACTIVE");
            match.setLastLog("Trận đấu bắt đầu! Hãy chọn nước đi.");
        }
        matchRepo.save(match);
    }

    // --- 3. SUBMIT MOVE ---
    @Transactional
    public PvpMatch submitMove(Long matchId, Integer charId, String move) {
        PvpMatch match = matchRepo.findById(matchId).orElseThrow(() -> new RuntimeException("Match not found"));

        if (!"ACTIVE".equals(match.getStatus())) {
            return match;
        }

        if (match.getPlayer1().getCharId().equals(charId)) match.setP1Move(move);
        else if (match.getPlayer2().getCharId().equals(charId)) match.setP2Move(move);

        matchRepo.save(match);

        if (match.getP1Move() != null && match.getP2Move() != null) {
            resolveTurn(match);
        }
        return match;
    }

    // --- 4. XỬ LÝ TURN (HÒA TRỪ MÁU & DOUBLE KO) ---
    private void resolveTurn(PvpMatch match) {
        String m1 = match.getP1Move();
        String m2 = match.getP2Move();

        match.setLastP1Move(m1);
        match.setLastP2Move(m2);

        Character p1 = match.getPlayer1();
        Character p2 = match.getPlayer2();
        int hp1 = match.getP1CurrentHp();
        int hp2 = match.getP2CurrentHp();
        StringBuilder log = new StringBuilder();

        if (m1.equals(m2)) {
            int drawDamage = 20;
            hp1 = Math.max(0, hp1 - drawDamage);
            hp2 = Math.max(0, hp2 - drawDamage);
            log.append("⚔️ HÒA! Cùng ra ").append(translateMove(m1))
                    .append(". Nội lực xung khắc! Cả hai mất ").append(drawDamage).append(" HP.");
        } else {
            boolean p1Wins = (m1.equals("ROCK") && m2.equals("SCISSORS")) ||
                    (m1.equals("PAPER") && m2.equals("ROCK")) ||
                    (m1.equals("SCISSORS") && m2.equals("PAPER"));

            Character atk = p1Wins ? p1 : p2;
            Character def = p1Wins ? p2 : p1;
            String wMove = p1Wins ? m1 : m2;
            int dmg = Math.max(15, atk.getBaseAtk() - def.getBaseDef());

            if (p1Wins) {
                hp2 = Math.max(0, hp2 - dmg);
                log.append("💥 ").append(p1.getName()).append(" dùng ").append(translateMove(wMove))
                        .append(" thắng! ").append(p2.getName()).append(" mất ").append(dmg).append(" HP.");
            } else {
                hp1 = Math.max(0, hp1 - dmg);
                log.append("💥 ").append(p2.getName()).append(" dùng ").append(translateMove(wMove))
                        .append(" thắng! ").append(p1.getName()).append(" mất ").append(dmg).append(" HP.");
            }
        }

        match.setP1CurrentHp(hp1);
        match.setP2CurrentHp(hp2);

        if (hp1 <= 0 && hp2 <= 0) {
            match.setStatus("FINISHED");
            match.setWinnerId(null);
            log.append("\n💀 LƯỠNG BẠI CÂU THƯƠNG! Cả hai cùng gục ngã. Hòa!");
        } else if (hp1 <= 0 || hp2 <= 0) {
            match.setStatus("FINISHED");
            Integer wId = hp1 <= 0 ? p2.getCharId() : p1.getCharId();
            Integer lId = hp1 <= 0 ? p1.getCharId() : p2.getCharId();
            match.setWinnerId(Long.valueOf(wId));
            log.append("\n🏆 ").append(hp1 <= 0 ? p2.getName() : p1.getName()).append(" ĐÃ CHIẾN THẮNG!");
            updatePvpStats(wId, lId);
        } else {
            match.setP1Move(null);
            match.setP2Move(null);
            match.setTurnCount(match.getTurnCount() + 1);
        }

        match.setLastLog(log.toString());
        matchRepo.save(match);
    }

    // --- 5. CHAT ---
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

    // --- 6. HỦY TÌM ---
    @Transactional
    public void cancelQueue(Integer charId) {
        queueRepo.findByCharId(charId).ifPresent(queueRepo::delete);
    }

    // --- 7. ĐẦU HÀNG (QUAN TRỌNG: FIX HIỂN THỊ THÔNG BÁO) ---
    @Transactional
    public void surrenderMatch(Long matchId, Integer charId) {
        PvpMatch match = matchRepo.findById(matchId).orElse(null);
        if (match != null && !"FINISHED".equals(match.getStatus())) {
            // Xác định người thắng
            Character p1 = match.getPlayer1();
            Character p2 = match.getPlayer2();

            Integer winnerId = p1.getCharId().equals(charId) ? p2.getCharId() : p1.getCharId();
            Character winner = p1.getCharId().equals(winnerId) ? p1 : p2;
            Character loser = p1.getCharId().equals(charId) ? p1 : p2;

            match.setStatus("FINISHED");
            match.setWinnerId(Long.valueOf(winnerId));

            // Log chuẩn để Frontend bắt được từ khóa "đầu hàng"
            match.setLastLog("🏳️ " + loser.getName() + " đã đầu hàng! " + winner.getName() + " giành chiến thắng.");

            updatePvpStats(winnerId, charId);
            matchRepo.save(match);
        }
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
}