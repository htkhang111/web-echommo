package com.echommo.service;

import com.echommo.entity.Character;
import com.echommo.entity.PvpMatch;
import com.echommo.entity.PvpQueue;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.PvpMatchRepository;
import com.echommo.repository.PvpQueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PvpService {
    @Autowired private PvpMatchRepository matchRepo;
    @Autowired private CharacterRepository charRepo;
    @Autowired private PvpQueueRepository queueRepo;

    private final Random random = new Random();

    // 1. TÌM TRẬN HOẶC VÀO HÀNG CHỜ
    @Transactional
    public PvpMatch findOrCreateMatch(Integer charId) {
        Optional<PvpMatch> existingMatch = matchRepo.findActiveMatchByCharId(charId);
        if (existingMatch.isPresent()) return existingMatch.get();

        Character myChar = charRepo.findById(charId).orElseThrow(() -> new RuntimeException("Character not found"));
        Optional<PvpQueue> myQueue = queueRepo.findByCharId(charId);
        if (myQueue.isPresent()) return null;

        Optional<PvpQueue> opponentQueue = queueRepo.findMatchCandidate(charId, myChar.getLevel() - 5, myChar.getLevel() + 5);

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

    // 2. GỬI NƯỚC ĐI
    @Transactional
    public PvpMatch submitMove(Long matchId, Integer charId, String move) {
        PvpMatch match = matchRepo.findById(matchId).orElseThrow(() -> new RuntimeException("Match not found"));
        if (!"ACTIVE".equals(match.getStatus())) throw new RuntimeException("Match is not active");

        if (match.getPlayer1().getCharId().equals(charId)) match.setP1Move(move);
        else if (match.getPlayer2().getCharId().equals(charId)) match.setP2Move(move);

        matchRepo.save(match);
        if (match.getP1Move() != null && match.getP2Move() != null) resolveTurn(match);
        return match;
    }

    // 3. XỬ LÝ KẾT QUẢ LƯỢT ĐÁNH & GHI NHẬN CHIẾN THẮNG
    private void resolveTurn(PvpMatch match) {
        String m1 = match.getP1Move();
        String m2 = match.getP2Move();
        Character p1 = match.getPlayer1();
        Character p2 = match.getPlayer2();
        int hp1 = match.getP1CurrentHp();
        int hp2 = match.getP2CurrentHp();
        StringBuilder log = new StringBuilder();

        if (m1.equals(m2)) {
            log.append("HÒA OẲN TÙ TÌ! Cả hai cùng ra ").append(translateMove(m1)).append(". Làm lại!");
            match.setP1Move(null);
            match.setP2Move(null);
        } else {
            boolean p1WinsRps = (m1.equals("ROCK") && m2.equals("SCISSORS")) ||
                    (m1.equals("PAPER") && m2.equals("ROCK")) ||
                    (m1.equals("SCISSORS") && m2.equals("PAPER"));

            Character attacker = p1WinsRps ? p1 : p2;
            Character defender = p1WinsRps ? p2 : p1;
            String winMove = p1WinsRps ? m1 : m2;

            log.append(attacker.getName()).append(" ra ").append(translateMove(winMove)).append(" thắng! -> TẤN CÔNG.\n");

            int speedDiff = defender.getBaseSpeed() - attacker.getBaseSpeed();
            double dodgeChance = Math.max(0, Math.min(0.5, speedDiff * 0.01));

            if (random.nextDouble() < dodgeChance) {
                log.append(defender.getName()).append(" ĐÃ NÉ ĐƯỢC ĐÒN TẤN CÔNG!");
            } else {
                int damage = Math.max(10, attacker.getBaseAtk() - defender.getBaseDef());
                if (random.nextInt(100) < attacker.getBaseCritRate()) {
                    damage = (int) (damage * 1.5);
                    log.append("BẠO KÍCH! ");
                }

                if (p1WinsRps) {
                    hp2 = Math.max(0, hp2 - damage);
                    match.setP2CurrentHp(hp2);
                    log.append(p1.getName()).append(" gây ").append(damage).append(" sát thương!");
                } else {
                    hp1 = Math.max(0, hp1 - damage);
                    match.setP1CurrentHp(hp1);
                    log.append(p2.getName()).append(" gây ").append(damage).append(" sát thương!");
                }
            }

            // KIỂM TRA KẾT THÚC TRẬN ĐẤU
            if (hp1 <= 0 || hp2 <= 0) {
                Integer winnerId = hp1 <= 0 ? p2.getCharId() : p1.getCharId();
                match.setStatus("FINISHED");
                match.setWinnerId(winnerId);
                log.append("\n").append(hp1 <= 0 ? p2.getName() : p1.getName()).append(" ĐÃ CHIẾN THẮNG!");

                // [QUAN TRỌNG] GỌI HÀM CẬP NHẬT TRẬN THẮNG VÀO DATABASE
                updatePvpStats(winnerId, (hp1 <= 0 ? p1.getCharId() : p2.getCharId()));
            } else {
                match.setP1Move(null);
                match.setP2Move(null);
                match.setTurnCount(match.getTurnCount() + 1);
            }
        }
        match.setLastLog(log.toString());
        matchRepo.save(match);
    }

    // [NEW] HÀM GHI NHỚ TRẬN THẮNG VÀO DB
    private void updatePvpStats(Integer winnerId, Integer loserId) {
        // Cập nhật người thắng
        charRepo.findById(winnerId).ifPresent(c -> {
            int currentWins = c.getPvpWins() != null ? c.getPvpWins() : 0;
            int totalPlayed = c.getPvpMatchesPlayed() != null ? c.getPvpMatchesPlayed() : 0;
            c.setPvpWins(currentWins + 1); // Cộng 1 trận thắng
            c.setPvpMatchesPlayed(totalPlayed + 1);
            charRepo.save(c); // Lưu vĩnh viễn vào DB
        });

        // Cập nhật người thua
        charRepo.findById(loserId).ifPresent(c -> {
            int totalPlayed = c.getPvpMatchesPlayed() != null ? c.getPvpMatchesPlayed() : 0;
            c.setPvpMatchesPlayed(totalPlayed + 1);
            charRepo.save(c);
        });
    }

    private String translateMove(String move) {
        if ("ROCK".equals(move)) return "BÚA";
        if ("PAPER".equals(move)) return "BAO";
        return "KÉO";
    }
}