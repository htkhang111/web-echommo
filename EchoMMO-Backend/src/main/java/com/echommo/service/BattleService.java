package com.echommo.service;

import com.echommo.dto.BattleResult;
import com.echommo.entity.*;
import com.echommo.entity.Character;
import com.echommo.enums.CharacterStatus;
import com.echommo.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        // 1. Player Attack
        int pDmg = Math.max(1, character.getBaseAtk() - session.getEnemyDef());
        session.setEnemyCurrentHp(Math.max(0, session.getEnemyCurrentHp() - pDmg));
        logs.add("Bạn đánh " + pDmg + " sát thương.");

        if (session.getEnemyCurrentHp() <= 0) return handleWin(session, character);

        // 2. Enemy Attack
        int eDmg = Math.max(1, session.getEnemyAtk() - character.getBaseDef());
        session.setPlayerCurrentHp(Math.max(0, session.getPlayerCurrentHp() - eDmg));
        logs.add(session.getEnemyName() + " đánh trả " + eDmg + " sát thương.");

        if (session.getPlayerCurrentHp() <= 0) return handleLoss(session, character);

        sessionRepo.save(session);
        return buildResult(session, logs, "ONGOING");
    }

    private BattleResult handleWin(BattleSession session, Character character) {
        BattleResult res = buildResult(session, "🏆 Chiến thắng!", "VICTORY");

        Enemy enemy = enemyRepo.findById(session.getEnemyId()).orElse(new Enemy());
        int expReward = enemy.getExpReward() != null ? enemy.getExpReward() : 10;
        int goldReward = enemy.getGoldReward() != null ? enemy.getGoldReward() : 5;

        character.setCurrentExp(character.getCurrentExp() + expReward);
        character.setMonsterKills(character.getMonsterKills() + 1);

        Wallet wallet = character.getUser().getWallet();
        // [FIX] Cộng Gold bằng BigDecimal
        wallet.setGold(wallet.getGold().add(BigDecimal.valueOf(goldReward)));

        if (session.getEnemyId() >= 100) {
            wallet.setEchoCoin(wallet.getEchoCoin().add(new BigDecimal("0.05")));
        }

        walletRepo.save(wallet);

        character.setStatus(CharacterStatus.IDLE);
        character.setCurrentHp(character.getMaxHp());
        charRepo.save(character);
        sessionRepo.delete(session);

        return res;
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