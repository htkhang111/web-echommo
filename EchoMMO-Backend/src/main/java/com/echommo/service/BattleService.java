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
    private final CharacterService charService;

    private final Random random = new Random();

    // --- HELPER ---
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Lỗi xác thực."));
    }

    private Character getMyCharacter() {
        User user = getCurrentUser();
        return charRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Chưa tạo nhân vật!"));
    }

    @Transactional
    public BattleResult startBattle() {
        Character character = getMyCharacter();
        List<BattleSession> sessions = sessionRepo.findByCharacter_CharId(character.getCharId());

        if (sessions.isEmpty()) {
            if (character.getStatus() == CharacterStatus.IN_COMBAT) {
                character.setStatus(CharacterStatus.IDLE);
                charRepo.save(character);
            }
            throw new RuntimeException("Chưa tìm thấy đối thủ! Hãy đi Thám Hiểm.");
        }
        BattleSession session = sessions.get(0);

        character.setStatus(CharacterStatus.IN_COMBAT);
        charRepo.save(character);

        String message = "Tiếp tục chiến đấu với " + session.getEnemyName() + " (HP: " + session.getEnemyCurrentHp() + "/" + session.getEnemyMaxHp() + ")";
        return buildResult(session, Collections.singletonList(message), "ONGOING");
    }

    /**
     * XỬ LÝ LƯỢT ĐÁNH (TURN)
     */
    @Transactional
    public BattleResult processTurn(String actionType) {
        Character c = getMyCharacter();
        List<BattleSession> sessions = sessionRepo.findByCharacter_CharId(c.getCharId());
        if (sessions.isEmpty()) throw new RuntimeException("Trận đấu không tồn tại!");
        BattleSession s = sessions.get(0);

        List<String> logs = new ArrayList<>();
        s.setCurrentTurn(s.getCurrentTurn() + 1);

        // --- 1. PLAYER ATTACK ---
        int pAtk = c.getBaseAtk();
        int pCritDmg = c.getBaseCritDmg();
        int pSpeed = c.getBaseSpeed();

        // Crit Rate: 5% + (Luck/5)
        int pLuck = c.getLuck() != null ? c.getLuck() : 5;
        int pCritRate = 5 + (pLuck / 5);

        int eDef = s.getEnemyDef();
        int eSpeed = s.getEnemySpeed() != null ? s.getEnemySpeed() : 10;

        // [FIX] Giới hạn né tối đa 60% để tránh việc đánh mãi không trúng
        int eDodgeChance = Math.min(60, Math.max(0, 5 + (eSpeed - pSpeed)));

        // A. Player đánh
        if (random.nextInt(100) < eDodgeChance) {
            logs.add("💨 BẠN ĐÁNH TRƯỢT! " + s.getEnemyName() + " né được (" + eDodgeChance + "%).");
        } else {
            // [FIX] Sát thương tối thiểu 10% ATK (Xuyên giáp)
            int minDmg = (int) Math.ceil(pAtk * 0.1);
            int rawDmg = pAtk - eDef;
            int dmg = Math.max(minDmg, rawDmg);

            // Player Crit Check
            boolean isCrit = random.nextInt(100) < pCritRate;
            if (isCrit) {
                dmg = (int) (dmg * (pCritDmg / 100.0));
                logs.add("🔥 CHÍ MẠNG! Bạn gây " + dmg + " sát thương!");
            } else {
                logs.add("⚔️ Bạn gây " + dmg + " sát thương.");
            }

            s.setEnemyCurrentHp(Math.max(0, s.getEnemyCurrentHp() - dmg));
        }

        // CHECK WIN
        if (s.getEnemyCurrentHp() <= 0) return handleWin(s, c, logs);


        // --- 2. ENEMY ATTACK ---
        int eAtk = s.getEnemyAtk();
        int pDef = c.getBaseDef();

        // [FIX] Giới hạn né của người chơi max 50%
        int pDodgeChance = Math.min(50, Math.max(0, (pSpeed - eSpeed) / 2));

        // A. Kiểm tra Né tránh
        if (random.nextInt(100) < pDodgeChance) {
            logs.add("✨ BẠN NÉ ĐƯỢC đòn tấn công!");
        } else {
            // [FIX] Quái đánh cũng có sát thương tối thiểu 10%
            int minDmg = (int) Math.ceil(eAtk * 0.1);
            int rawDmg = eAtk - pDef;
            int dmg = Math.max(minDmg, rawDmg);

            logs.add("🛡️ " + s.getEnemyName() + " đánh trả " + dmg + " sát thương.");

            s.setPlayerCurrentHp(Math.max(0, s.getPlayerCurrentHp() - dmg));
            c.setCurrentHp(s.getPlayerCurrentHp());
        }

        // CHECK LOSS
        if (s.getPlayerCurrentHp() <= 0) return handleLoss(s, c, logs);

        sessionRepo.save(s);
        charRepo.save(c);
        return buildResult(s, logs, "ONGOING");
    }

    private BattleResult handleWin(BattleSession session, Character character, List<String> logs) {
        // Tìm quái gốc để lấy reward base
        Enemy enemy = enemyRepo.findById(session.getEnemyId()).orElse(new Enemy());
        int enemyLvl = enemy.getLevel() != null ? enemy.getLevel() : 1;

        // [LOGIC MỚI] Check Tinh Anh để x3 thưởng
        boolean isElite = session.getEnemyName().contains("[Tinh Anh]");
        int rewardMult = isElite ? 3 : 1;

        int expReward = (int) ((enemy.getExpReward() != null ? enemy.getExpReward() : 10) * (1 + enemyLvl * 0.2) * rewardMult);
        int goldReward = (int) ((enemy.getGoldReward() != null ? enemy.getGoldReward() : 5) * (1 + enemyLvl * 0.1) * rewardMult);

        character.setCurrentExp(character.getCurrentExp() + expReward);
        character.setMonsterKills(character.getMonsterKills() + 1);

        checkLevelUp(character);

        Wallet wallet = character.getUser().getWallet();
        wallet.setGold(wallet.getGold().add(BigDecimal.valueOf(goldReward)));

        // Tỷ lệ rơi Echo Coin
        // Tinh Anh có 30% cơ hội rơi coin lớn, quái thường 5% rơi coin nhỏ
        if (isElite && random.nextInt(100) < 30) {
            wallet.setEchoCoin(wallet.getEchoCoin().add(new BigDecimal("0.1")));
            logs.add("💎 [TINH ANH] Rơi ra mảnh Echo Coin lớn!");
        } else if (enemyLvl >= 5 && random.nextInt(100) < 10) {
            wallet.setEchoCoin(wallet.getEchoCoin().add(new BigDecimal("0.05")));
            logs.add("💎 Nhặt được mảnh Echo Coin!");
        }

        character.setStatus(CharacterStatus.IDLE);

        // Hồi máu nhẹ sau trận (5 HP)
        int regen = 5;
        if(character.getCurrentHp() + regen < character.getMaxHp()){
            character.setCurrentHp(character.getCurrentHp() + regen);
        } else {
            character.setCurrentHp(character.getMaxHp());
        }

        walletRepo.save(wallet);
        charRepo.save(character);
        sessionRepo.delete(session);

        logs.add("🏆 CHIẾN THẮNG!");
        if (isElite) logs.add("🔥 Bạn đã hạ gục quái vật TINH ANH!");
        logs.add("Nhận: " + expReward + " EXP, " + goldReward + " Vàng.");
        return buildResult(session, logs, "VICTORY");
    }

    private BattleResult handleLoss(BattleSession session, Character character, List<String> logs) {
        logs.add("💀 BẠN ĐÃ BẠI TRẬN!");
        character.setCurrentHp(1);
        character.setStatus(CharacterStatus.IDLE);
        charRepo.save(character);
        sessionRepo.delete(session);
        return buildResult(session, logs, "DEFEAT");
    }

    private void checkLevelUp(Character c) {
        long requiredExp = c.getLevel() * 100L;
        if (c.getCurrentExp() >= requiredExp) {
            c.setLevel(c.getLevel() + 1);
            c.setCurrentExp(c.getCurrentExp() - requiredExp);
            charService.recalculateStats(c); // Update Stats

            // Lên cấp thì cho hồi full máu
            c.setCurrentHp(c.getMaxHp());
            c.setCurrentEnergy(c.getMaxEnergy());
        }
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