package com.echommo.service;

import com.echommo.dto.BattleResult;
import com.echommo.dto.SubStatDTO;
import com.echommo.entity.*;
import com.echommo.entity.Character;
import com.echommo.enums.CharacterStatus;
import com.echommo.enums.Rarity;
import com.echommo.enums.SlotType;
import com.echommo.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BattleService {

    @Autowired private CharacterRepository charRepo;
    @Autowired private EnemyRepository enemyRepo;
    @Autowired private WalletRepository walletRepo;
    @Autowired private ItemRepository itemRepo;
    @Autowired private UserItemRepository userItemRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private BattleSessionRepository sessionRepo;
    @Autowired private ItemGenerationService itemGenService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();
    private static final double DROP_RATE = 0.5;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Lỗi xác thực: Người dùng chưa đăng nhập."));
    }

    private Character getMyCharacter() {
        User user = getCurrentUser();
        return charRepo.findByUser_UserId(user.getUserId().longValue())
                .orElseThrow(() -> new RuntimeException("Bạn chưa tạo nhân vật!"));
    }

    @Transactional
    public BattleResult startBattle() {
        Character character = getMyCharacter();
        BattleSession session = sessionRepo.findByCharacter_CharId(character.getCharId())
                .orElse(new BattleSession());

        session.setCharacter(character);

        List<Enemy> enemies = enemyRepo.findAll();
        Enemy enemy = enemies.isEmpty() ? createDummyEnemy() : enemies.get(random.nextInt(enemies.size()));

        session.setEnemyId(enemy.getEnemyId());
        session.setEnemyName(enemy.getName());
        session.setEnemyMaxHp(enemy.getHp());
        session.setEnemyCurrentHp(enemy.getHp());
        session.setEnemyAtk(enemy.getAtk());
        session.setEnemyDef(enemy.getDef());
        session.setEnemySpeed(enemy.getSpeed());

        int[] bonusStats = calculatePlayerStats(character);
        session.setPlayerMaxHp(character.getMaxHp() + bonusStats[3]);
        session.setPlayerCurrentHp(session.getPlayerMaxHp());
        session.setPlayerCurrentEnergy(character.getCurrentEnergy());

        session.setCurrentTurn(0);
        session.setQteActive(false);

        character.setStatus(CharacterStatus.IN_COMBAT);
        charRepo.save(character);

        return buildResult(sessionRepo.save(session), "Gặp " + enemy.getName() + "!", "ONGOING");
    }

    @Transactional
    public BattleResult processTurn(String actionType) {
        Character character = getMyCharacter();
        BattleSession session = sessionRepo.findByCharacter_CharId(character.getCharId())
                .orElseThrow(() -> new RuntimeException("Trận đấu đã kết thúc!"));

        List<String> logs = new ArrayList<>();

        // Logic QTE
        if (session.isQteActive()) {
            if (session.getQteExpiryTime() != null && LocalDateTime.now().isAfter(session.getQteExpiryTime())) {
                actionType = "MISS";
            }
            if (!"BLOCK".equalsIgnoreCase(actionType)) {
                int dmg = (int)(session.getEnemyAtk() * 1.5);
                session.setPlayerCurrentHp(session.getPlayerCurrentHp() - dmg);
                if (session.getPlayerCurrentHp() <= 0) return handleLoss(session, character);
            }
            session.setQteActive(false);
            sessionRepo.save(session);
            return buildResult(session, logs, "ONGOING");
        }

        session.setCurrentTurn(session.getCurrentTurn() + 1);
        int[] stats = calculatePlayerStats(character);

        // Player Attack
        int pAtk = character.getBaseAtk() + stats[0];
        int rawDmg = Math.max(1, pAtk - session.getEnemyDef());
        session.setEnemyCurrentHp(session.getEnemyCurrentHp() - rawDmg);
        logs.add("⚔️ Bạn gây " + rawDmg + " sát thương.");

        if (session.getEnemyCurrentHp() <= 0) return handleWin(session, character);

        // Enemy Attack
        int pDef = character.getBaseDef() + stats[1];
        int dmgToPlayer = Math.max(1, session.getEnemyAtk() - pDef);
        session.setPlayerCurrentHp(session.getPlayerCurrentHp() - dmgToPlayer);
        logs.add("👾 " + session.getEnemyName() + " đánh trả " + dmgToPlayer + " máu.");

        if (session.getPlayerCurrentHp() <= 0) return handleLoss(session, character);

        sessionRepo.save(session);
        return buildResult(session, logs, "ONGOING");
    }

    // --- PHẦN STATS: ĐÃ FIX LỖI CANNOT FIND SYMBOL ---
    private int[] calculatePlayerStats(Character c) {
        double[] flatStats = new double[6];
        double[] percentStats = new double[6];

        List<UserItem> items = userItemRepo.findByCharacter_CharIdAndIsEquippedTrue(c.getCharId());

        for (UserItem ui : items) {
            // Sử dụng các Getter thủ công đã thêm vào Entity UserItem
            if (ui.getMainStatType() != null) {
                parseStatToArrays(ui.getMainStatType(), ui.getMainStatValue().doubleValue(), flatStats, percentStats);
            }
            if (ui.getSubStats() != null && !ui.getSubStats().equals("[]")) {
                try {
                    List<SubStatDTO> subs = objectMapper.readValue(ui.getSubStats(), new TypeReference<List<SubStatDTO>>() {});
                    for (SubStatDTO sub : subs) {
                        parseStatToArrays(sub.getCode(), sub.getValue(), flatStats, percentStats);
                    }
                } catch (Exception e) {}
            }
        }

        int[] finalBonus = new int[6];
        finalBonus[0] = (int) (flatStats[0] + ((c.getBaseAtk() + flatStats[0]) * percentStats[0] / 100.0));
        finalBonus[1] = (int) (flatStats[1] + ((c.getBaseDef() + flatStats[1]) * percentStats[1] / 100.0));
        finalBonus[2] = (int) (flatStats[2] + percentStats[2]);
        finalBonus[3] = (int) (flatStats[3] + ((c.getMaxHp() + flatStats[3]) * percentStats[3] / 100.0));
        finalBonus[4] = (int) (flatStats[4] + percentStats[4]);
        finalBonus[5] = (int) (flatStats[5] + percentStats[5]);

        return finalBonus;
    }

    private void parseStatToArrays(String type, double val, double[] flats, double[] percents) {
        switch (type) {
            case "ATK_FLAT" -> flats[0] += val;
            case "ATK_PERCENT" -> percents[0] += val;
            case "DEF_FLAT" -> flats[1] += val;
            case "DEF_PERCENT" -> percents[1] += val;
            case "CRIT_RATE" -> flats[2] += val;
            case "HP_FLAT" -> flats[3] += val;
            case "SPEED" -> flats[4] += val;
            case "CRIT_DMG" -> flats[5] += val;
        }
    }

    private BattleResult handleWin(BattleSession session, Character character) {
        BattleResult res = buildResult(session, "🏆 Chiến thắng!", "VICTORY");
        Enemy enemyRef = enemyRepo.findById(session.getEnemyId()).orElse(null);
        int exp = (enemyRef != null) ? enemyRef.getExpReward() : 10;
        int gold = (enemyRef != null) ? enemyRef.getGoldReward() : 10;

        character.setCurrentExp(character.getCurrentExp() + exp);
        // Level up logic...

        character.setCurrentHp(Math.max(1, session.getPlayerCurrentHp()));
        character.setStatus(CharacterStatus.IDLE);
        charRepo.save(character);

        handleNewItemDrop(character, res.getCombatLog(), res);
        sessionRepo.delete(session);
        return res;
    }

    private void handleNewItemDrop(Character character, List<String> logs, BattleResult result) {
        if (random.nextDouble() > DROP_RATE) return;
        List<Item> allItems = itemRepo.findAll();
        if (allItems.isEmpty()) return;

        Item baseItem = allItems.get(random.nextInt(allItems.size()));
        UserItem newItem = new UserItem();
        newItem.setCharacter(character); // Đã sửa: gán cho Character
        newItem.setItem(baseItem);
        newItem.setQuantity(1);
        newItem.setIsEquipped(false);
        newItem.setEnhancementLevel(0);
        newItem.setAcquiredAt(LocalDateTime.now());

        userItemRepo.save(newItem);
        logs.add("🎁 Nhận được: " + baseItem.getName());
    }

    private BattleResult handleLoss(BattleSession session, Character character) {
        character.setCurrentHp(1);
        character.setStatus(CharacterStatus.IDLE);
        charRepo.save(character);
        sessionRepo.delete(session);
        return buildResult(session, "💀 Thất bại!", "DEFEAT");
    }

    private Enemy createDummyEnemy() {
        Enemy e = new Enemy(); e.setEnemyId(0); e.setName("Bù Nhìn");
        e.setHp(100); e.setAtk(5); e.setDef(0); e.setSpeed(10);
        return e;
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

    private BattleResult buildResult(BattleSession s, String msg, String status) {
        List<String> logs = new ArrayList<>(); logs.add(msg); return buildResult(s, logs, status);
    }
}