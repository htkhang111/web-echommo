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
    private final CharacterService charService; // Để sử dụng hàm tính lại chỉ số

    private final Random random = new Random();

    // --- HELPERS ---
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Lỗi xác thực người dùng."));
    }

    private Character getMyCharacter() {
        User user = getCurrentUser();
        Character character = charRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Chưa tạo nhân vật!"));

        // [FIX] Luôn tính toán lại chỉ số từ trang bị mỗi khi lấy nhân vật ra
        // Điều này giúp baseAtk, baseDef... luôn cập nhật theo vũ khí/giáp đang mặc.
        charService.recalculateStats(character);

        return character;
    }

    // --- CHIẾN ĐẤU ---

    @Transactional
    public BattleResult startBattle() {
        Character character = getMyCharacter();
        List<BattleSession> sessions = sessionRepo.findByCharacter_CharId(character.getCharId());

        if (sessions.isEmpty()) {
            if (character.getStatus() == CharacterStatus.IN_COMBAT) {
                character.setStatus(CharacterStatus.IDLE);
                charRepo.save(character);
            }
            throw new RuntimeException("Không tìm thấy trận đấu nào! Hãy đi thám hiểm trước.");
        }

        BattleSession session = sessions.get(0);
        character.setStatus(CharacterStatus.IN_COMBAT);
        charRepo.save(character);

        String message = "⚔️ Bạn chạm trán " + session.getEnemyName() + "!";
        return buildResult(session, Collections.singletonList(message), "ONGOING");
    }

    @Transactional
    public BattleResult processTurn(String actionType) {
        Character character = getMyCharacter(); // Đã bao gồm recalculateStats

        List<BattleSession> sessions = sessionRepo.findByCharacter_CharId(character.getCharId());
        if (sessions.isEmpty()) throw new RuntimeException("Trận đấu đã kết thúc hoặc không tồn tại.");

        BattleSession s = sessions.get(0);
        List<String> logs = new ArrayList<>();
        s.setCurrentTurn(s.getCurrentTurn() + 1);

        // --- 1. NGƯỜI CHƠI TẤN CÔNG (Player -> Enemy) ---
        int pAtk = character.getBaseAtk();
        int pCritDmg = character.getBaseCritDmg();
        int pSpeed = character.getBaseSpeed();
        int pCritRate = character.getBaseCritRate();

        int eDef = s.getEnemyDef();
        int eSpeed = s.getEnemySpeed() != null ? s.getEnemySpeed() : 10;

        // Tỷ lệ né của Quái (Max 60%)
        int eDodgeChance = Math.min(60, Math.max(0, 5 + (eSpeed - pSpeed)));

        if (random.nextInt(100) < eDodgeChance) {
            logs.add("💨 " + s.getEnemyName() + " đã né được đòn tấn công của bạn!");
        } else {
            // Sát thương = Công - Thủ (Xuyên giáp tối thiểu 10% Công)
            int minDmg = (int) Math.ceil(pAtk * 0.1);
            int damage = Math.max(minDmg, pAtk - eDef);

            // Kiểm tra bạo kích
            if (random.nextInt(100) < pCritRate) {
                damage = (int) (damage * (pCritDmg / 100.0));
                logs.add("🔥 BẠO KÍCH! Bạn gây " + damage + " sát thương lên " + s.getEnemyName() + ".");
            } else {
                logs.add("⚔️ Bạn gây " + damage + " sát thương lên đối thủ.");
            }

            s.setEnemyCurrentHp(Math.max(0, s.getEnemyCurrentHp() - damage));
        }

        if (s.getEnemyCurrentHp() <= 0) return handleWin(s, character, logs);

        // --- 2. QUÁI TẤN CÔNG (Enemy -> Player) ---
        int eAtk = s.getEnemyAtk();
        int pDef = character.getBaseDef();

        // Tỷ lệ né của Người chơi (Max 50%)
        int pDodgeChance = Math.min(50, Math.max(0, (pSpeed - eSpeed) / 2));

        if (random.nextInt(100) < pDodgeChance) {
            logs.add("✨ Bạn đã né đòn tấn công từ " + s.getEnemyName() + "!");
        } else {
            int minEDmg = (int) Math.ceil(eAtk * 0.1);
            int eDamage = Math.max(minEDmg, eAtk - pDef);

            logs.add("🛡️ " + s.getEnemyName() + " tấn công, bạn mất " + eDamage + " HP.");
            s.setPlayerCurrentHp(Math.max(0, s.getPlayerCurrentHp() - eDamage));
            character.setCurrentHp(s.getPlayerCurrentHp());
        }

        if (s.getPlayerCurrentHp() <= 0) return handleLoss(s, character, logs);

        sessionRepo.save(s);
        charRepo.save(character);
        return buildResult(s, logs, "ONGOING");
    }

    private BattleResult handleWin(BattleSession session, Character character, List<String> logs) {
        Enemy enemy = enemyRepo.findById(session.getEnemyId()).orElse(new Enemy());
        int enemyLvl = enemy.getLevel() != null ? enemy.getLevel() : 1;

        // Thưởng cơ bản
        int expReward = (int) (enemy.getExpReward() != null ? enemy.getExpReward() : 10 * enemyLvl);
        int goldReward = (int) (enemy.getGoldReward() != null ? enemy.getGoldReward() : 5 * enemyLvl);

        // Check Tinh Anh (Elite)
        boolean isElite = session.getEnemyName().contains("[Tinh Anh]");
        if (isElite) {
            expReward *= 3;
            goldReward *= 3;
        }

        character.setCurrentExp(character.getCurrentExp() + expReward);
        character.setMonsterKills(character.getMonsterKills() + 1);
        character.setStatus(CharacterStatus.IDLE);

        // Kiểm tra lên cấp
        checkLevelUp(character);

        // Cộng vàng
        Wallet wallet = character.getUser().getWallet();
        wallet.setGold(wallet.getGold().add(BigDecimal.valueOf(goldReward)));

        // Tỷ lệ rơi Echo Coin
        if (isElite && random.nextInt(100) < 25) {
            wallet.setEchoCoin(wallet.getEchoCoin().add(new BigDecimal("0.1")));
            logs.add("💎 [HIẾM] Nhận được 0.1 Echo Coin từ Tinh Anh!");
        } else if (random.nextInt(100) < 5) {
            wallet.setEchoCoin(wallet.getEchoCoin().add(new BigDecimal("0.01")));
            logs.add("💎 Nhận được 0.01 Echo Coin!");
        }

        // Hồi phục 5% HP sau trận thắng
        int heal = (int)(character.getMaxHp() * 0.05);
        character.setCurrentHp(Math.min(character.getMaxHp(), character.getCurrentHp() + heal));

        walletRepo.save(wallet);
        charRepo.save(character);
        sessionRepo.delete(session);

        logs.add("🏆 CHIẾN THẮNG! Nhận: " + expReward + " EXP và " + goldReward + " Vàng.");
        return buildResult(session, logs, "VICTORY");
    }

    private BattleResult handleLoss(BattleSession session, Character character, List<String> logs) {
        character.setStatus(CharacterStatus.IDLE);
        character.setCurrentHp(10); // Hồi lại một ít máu để không bị kẹt
        charRepo.save(character);
        sessionRepo.delete(session);

        logs.add("💀 BẠN ĐÃ BẠI TRẬN trước " + session.getEnemyName() + "!");
        return buildResult(session, logs, "DEFEAT");
    }

    private void checkLevelUp(Character c) {
        long required = c.getLevel() * 150L; // Công thức EXP level up
        if (c.getCurrentExp() >= required) {
            c.setLevel(c.getLevel() + 1);
            c.setCurrentExp(c.getCurrentExp() - required);

            // Khi lên cấp, tự động tính lại chỉ số và hồi full
            charService.recalculateStats(c);
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