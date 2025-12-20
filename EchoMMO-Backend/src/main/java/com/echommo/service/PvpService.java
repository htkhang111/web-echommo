package com.echommo.service;

import com.echommo.entity.Character; // Import từ entity
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
        // A. Kiểm tra xem user có đang trong trận nào (ACTIVE/PENDING) không?
        Optional<PvpMatch> existingMatch = matchRepo.findActiveMatchByCharId(charId);
        if (existingMatch.isPresent()) {
            return existingMatch.get();
        }

        // B. Lấy thông tin nhân vật
        Character myChar = charRepo.findById(charId)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        // C. Kiểm tra xem mình có đang trong hàng chờ không
        Optional<PvpQueue> myQueue = queueRepo.findByCharId(charId);
        if (myQueue.isPresent()) {
            // Đã trong hàng chờ rồi -> Trả về null (để client tiếp tục polling chờ)
            // Hoặc có thể check logic ghép ở đây nếu muốn
            return null;
        }

        // D. Tìm đối thủ trong hàng chờ (Khác mình, level chênh lệch <= 5)
        // Query này bạn cần viết trong PvpQueueRepository: findFirstByCharIdNotAndLevelBetween...
        Optional<PvpQueue> opponentQueue = queueRepo.findMatchCandidate(charId, myChar.getLevel() - 5, myChar.getLevel() + 5);

        if (opponentQueue.isPresent()) {
            // -- TÌM THẤY ĐỐI THỦ --
            PvpQueue opponent = opponentQueue.get();
            Character enemyChar = charRepo.findById(opponent.getCharId()).orElseThrow();

            // 1. Xóa cả 2 khỏi hàng chờ (nếu mình chưa vào thì thôi, xóa đối thủ)
            queueRepo.delete(opponent);

            // 2. Tạo trận đấu mới
            PvpMatch newMatch = new PvpMatch();
            newMatch.setPlayer1(myChar);
            newMatch.setPlayer2(enemyChar);
            newMatch.setStatus("PENDING"); // Chờ chấp nhận
            newMatch.setCreatedAt(LocalDateTime.now());
            newMatch.setTurnCount(1);

            // Set máu thi đấu (Snapshot máu hiện tại)
            newMatch.setP1CurrentHp(myChar.getMaxHp());
            newMatch.setP2CurrentHp(enemyChar.getMaxHp());

            return matchRepo.save(newMatch);
        } else {
            // -- KHÔNG TÌM THẤY --
            // Thêm mình vào hàng chờ
            PvpQueue newQueue = new PvpQueue();
            newQueue.setCharId(charId);
            newQueue.setLevel(myChar.getLevel());
            newQueue.setPower(myChar.getTotalPower()); // Giả sử có getPower
            newQueue.setStatus("SEARCHING");
            newQueue.setJoinedAt(LocalDateTime.now());

            queueRepo.save(newQueue);
            return null; // Trả về null báo hiệu đang chờ
        }
    }

    // 2. GỬI NƯỚC ĐI (OẲN TÙ TÌ)
    @Transactional
    public PvpMatch submitMove(Long matchId, Integer charId, String move) {
        PvpMatch match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (!"ACTIVE".equals(match.getStatus())) {
            throw new RuntimeException("Match is not active");
        }

        if (match.getPlayer1().getCharId().equals(charId)) {
            match.setP1Move(move);
        } else if (match.getPlayer2().getCharId().equals(charId)) {
            match.setP2Move(move);
        }

        // Lưu tạm move
        matchRepo.save(match);

        // Nếu cả 2 đã ra chiêu -> Tính toán kết quả ngay
        if (match.getP1Move() != null && match.getP2Move() != null) {
            resolveTurn(match);
        }

        return match;
    }

    // 3. XỬ LÝ LOGIC COMBAT
    private void resolveTurn(PvpMatch match) {
        String m1 = match.getP1Move();
        String m2 = match.getP2Move();
        Character p1 = match.getPlayer1();
        Character p2 = match.getPlayer2();

        // Lấy máu hiện tại TRONG TRẬN (từ bảng pvp_matches)
        int hp1 = match.getP1CurrentHp();
        int hp2 = match.getP2CurrentHp();

        StringBuilder log = new StringBuilder();

        // A. Xử lý RPS
        if (m1.equals(m2)) {
            log.append("HÒA OẲN TÙ TÌ! Cả hai cùng ra ").append(translateMove(m1)).append(". Làm lại!");
            match.setP1Move(null);
            match.setP2Move(null);
        } else {
            boolean p1WinsRps = (m1.equals("ROCK") && m2.equals("SCISSORS")) ||
                    (m1.equals("PAPER") && m2.equals("ROCK")) ||
                    (m1.equals("SCISSORS") && m2.equals("PAPER"));

            // Xác định người đánh, người đỡ
            Character attacker = p1WinsRps ? p1 : p2;
            Character defender = p1WinsRps ? p2 : p1;
            String winMove = p1WinsRps ? m1 : m2;

            // Log RPS
            log.append(attacker.getName()).append(" ra ").append(translateMove(winMove))
                    .append(" thắng! -> TẤN CÔNG.\n");

            // B. Xử lý Damage
            // Tỷ lệ né = (Speed thủ - Speed công) / 100. Max 50%.
            int speedDiff = defender.getBaseSpeed() - attacker.getBaseSpeed();
            double dodgeChance = Math.max(0, Math.min(0.5, speedDiff * 0.01));

            if (random.nextDouble() < dodgeChance) {
                log.append(defender.getName()).append(" ĐÃ NÉ ĐƯỢC ĐÒN TẤN CÔNG!");
            } else {
                // Damage calculation
                int damage = Math.max(10, attacker.getBaseAtk() - defender.getBaseDef());

                // Crit logic (Ví dụ: 20% cơ hội)
                if (random.nextInt(100) < attacker.getBaseCritRate()) {
                    damage = (int) (damage * 1.5);
                    log.append("BẠO KÍCH! ");
                }

                // Trừ máu (Chỉ trừ trong trận đấu, không update vào Character Entity)
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

            // C. Kiểm tra thắng thua
            if (hp1 <= 0) {
                match.setStatus("FINISHED");
                match.setWinnerId(p2.getCharId());
                log.append("\n").append(p2.getName()).append(" ĐÃ CHIẾN THẮNG!");
                // Có thể cộng điểm rank ở đây
            } else if (hp2 <= 0) {
                match.setStatus("FINISHED");
                match.setWinnerId(p1.getCharId());
                log.append("\n").append(p1.getName()).append(" ĐÃ CHIẾN THẮNG!");
            } else {
                // Chưa ai thua -> Reset move để sang hiệp sau
                match.setP1Move(null);
                match.setP2Move(null);
                match.setTurnCount(match.getTurnCount() + 1);
            }
        }

        match.setLastLog(log.toString());
        matchRepo.save(match);
    }

    // Helper dịch tiếng Việt
    private String translateMove(String move) {
        if ("ROCK".equals(move)) return "BÚA";
        if ("PAPER".equals(move)) return "BAO";
        return "KÉO";
    }
}