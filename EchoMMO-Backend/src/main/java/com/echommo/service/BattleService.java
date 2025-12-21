package com.echommo.service;

import com.echommo.dto.BattleResult;
import com.echommo.entity.*;
import com.echommo.entity.Character;
import com.echommo.enums.CharacterStatus;
import com.echommo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BattleService {

    private final CharacterRepository charRepo;
    private final EnemyRepository enemyRepo;
    private final WalletRepository walletRepo;
    private final UserRepository userRepo;
    private final BattleSessionRepository sessionRepo;
    private final CharacterService charService; // Tiêm vào để dùng recalculateStats

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Lỗi xác thực: Người dùng chưa đăng nhập."));
    }

    private Character getMyCharacter() {
        User user = getCurrentUser();
        return charRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Bạn chưa tạo nhân vật!"));
    }

    @Transactional
    public BattleResult startBattle() {
        Character character = getMyCharacter();
        List<BattleSession> sessions = sessionRepo.findByCharacter_CharId(character.getCharId());

        if (sessions.isEmpty()) {
            throw new RuntimeException("Chưa tìm thấy đối thủ! Hãy đi Thám Hiểm (Explore) để gặp quái.");
        }
        BattleSession session = sessions.get(0);

        character.setStatus(CharacterStatus.IN_COMBAT);
        charRepo.save(character);

        return buildResult(session, "Tiếp tục chiến đấu với " + session.getEnemyName() + "!", "ONGOING");
    }

    @Transactional
    public BattleResult processTurn(String actionType) {
        Character character = getMyCharacter();
        List<BattleSession> sessions = sessionRepo.findByCharacter_CharId(character.getCharId());
        if (sessions.isEmpty()) throw new RuntimeException("Trận đấu không tồn tại!");

        BattleSession session = sessions.get(0);
        List<String> logs = new ArrayList<>();

        session.setCurrentTurn(session.getCurrentTurn() + 1);

        // 1. Player Attack (Tính toán dựa trên chỉ số đã scale của nhân vật)
        int pDmg = Math.max(1, character.getBaseAtk() - session.getEnemyDef());
        session.setEnemyCurrentHp(Math.max(0, session.getEnemyCurrentHp() - pDmg));
        logs.add("Bạn đánh " + pDmg + " sát thương.");

        if (session.getEnemyCurrentHp() <= 0) return handleWin(session, character);

        // 2. Enemy Attack (Tính toán dựa trên chỉ số quái trong session)
        int eDmg = Math.max(1, session.getEnemyAtk() - character.getBaseDef());
        session.setPlayerCurrentHp(Math.max(0, session.getPlayerCurrentHp() - eDmg));
        logs.add(session.getEnemyName() + " đánh trả " + eDmg + " sát thương.");

        if (session.getPlayerCurrentHp() <= 0) return handleLoss(session, character);

        sessionRepo.save(session);
        return buildResult(session, logs, "ONGOING");
    }

    private BattleResult handleWin(BattleSession session, Character character) {
        Enemy enemy = enemyRepo.findById(session.getEnemyId()).orElse(new Enemy());

        // [FIX]: Thưởng EXP và Gold scale theo level của quái vật
        int enemyLvl = enemy.getLevel() != null ? enemy.getLevel() : 1;
        int expReward = (int) ((enemy.getExpReward() != null ? enemy.getExpReward() : 10) * (1 + enemyLvl * 0.2));
        int goldReward = (int) ((enemy.getGoldReward() != null ? enemy.getGoldReward() : 5) * (1 + enemyLvl * 0.1));

        character.setCurrentExp(character.getCurrentExp() + expReward);
        character.setMonsterKills(character.getMonsterKills() + 1);

        // [FIX]: Kiểm tra và xử lý thăng cấp
        checkLevelUp(character);

        Wallet wallet = character.getUser().getWallet();
        wallet.setGold(wallet.getGold().add(BigDecimal.valueOf(goldReward)));

        // Thưởng EchoCoin cho quái cấp cao
        if (session.getEnemyId() >= 100) {
            wallet.setEchoCoin(wallet.getEchoCoin().add(new BigDecimal("0.05")));
        }

        walletRepo.save(wallet);

        character.setStatus(CharacterStatus.IDLE);
        // Sau trận thắng, hồi phục HP dựa trên MaxHp mới nhất
        character.setCurrentHp(character.getMaxHp());
        charRepo.save(character);
        sessionRepo.delete(session);

        List<String> logs = new ArrayList<>();
        logs.add("🏆 Chiến thắng!");
        logs.add("Bạn nhận được " + expReward + " EXP và " + goldReward + " Vàng.");

        return buildResult(session, logs, "VICTORY");
    }

    private void checkLevelUp(Character c) {
        // Công thức EXP: Level hiện tại * 100
        long requiredExp = c.getLevel() * 100L;
        if (c.getCurrentExp() >= requiredExp) {
            c.setLevel(c.getLevel() + 1);
            c.setCurrentExp(c.getCurrentExp() - requiredExp);

            // [FIX]: Tính toán lại toàn bộ chỉ số khi thăng cấp để áp dụng Base Growth
            charService.recalculateStats(c);
        }
    }

    private BattleResult handleLoss(BattleSession session, Character character) {
        character.setCurrentHp(1);
        character.setStatus(CharacterStatus.IDLE);
        charRepo.save(character);
        sessionRepo.delete(session);
        return buildResult(session, "💀 Thất bại! Bạn lết về làng với 1 HP.", "DEFEAT");
    }

    private BattleResult buildResult(BattleSession s, String msg, String status) {
        List<String> l = new ArrayList<>(); l.add(msg); return buildResult(s, l, status);
    }

    private BattleResult buildResult(BattleSession s, List<String> logs, String status) {
        BattleResult res = new BattleResult();
        res.setEnemyName(s.getEnemyName());
        res.setEnemyHp(s.getEnemyCurrentHp());
        res.setEnemyMaxHp(s.getEnemyMaxHp());
        res.setPlayerHp(s.getPlayerCurrentHp());
        res.setPlayerMaxHp(s.getPlayerMaxHp());
        res.setCombatLog(logs);
        res.setStatus(status);
        return res;
    }
}