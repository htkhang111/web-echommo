package com.echommo.service;

import com.echommo.config.GameConstants;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BattleService {

    private final CharacterRepository charRepo;
    private final EnemyRepository enemyRepo;
    private final WalletRepository walletRepo;
    private final ItemRepository itemRepo;
    private final UserItemRepository userItemRepo;
    private final UserRepository userRepo;
    private final BattleSessionRepository sessionRepo;
    private final ItemGenerationService itemGenService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    private static final double DROP_RATE = 0.5; // 50% drop rate example

    // --- HELPER: Get User & Character ---
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Lỗi xác thực: Người dùng chưa đăng nhập."));
    }

    private Character getMyCharacter() {
        User user = getCurrentUser();
        return charRepo.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Bạn chưa tạo nhân vật!"));
    }

    // --- 1. START BATTLE ---
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

        Map<String, Double> pStats = calculateTotalStats(character);
        int totalHp = (int) (double) pStats.getOrDefault("HP", 100.0);

        session.setPlayerMaxHp(totalHp);
        session.setPlayerCurrentHp(totalHp);
        session.setPlayerCurrentEnergy(character.getCurrentEnergy());
        session.setCurrentTurn(0);
        session.setQteActive(false);

        character.setStatus(CharacterStatus.IN_COMBAT);
        charRepo.save(character);

        return buildResult(sessionRepo.save(session), "Gặp " + enemy.getName() + "! (HP: " + enemy.getHp() + ")", "ONGOING");
    }

    // --- 2. PROCESS TURN ---
    @Transactional
    public BattleResult processTurn(String actionType) {
        Character character = getMyCharacter();
        BattleSession session = sessionRepo.findByCharacter_CharId(character.getCharId())
                .orElseThrow(() -> new RuntimeException("Trận đấu đã kết thúc!"));

        List<String> logs = new ArrayList<>();

        // -- A. QTE Handling --
        if (session.isQteActive()) {
            if (session.getQteExpiryTime() != null && LocalDateTime.now().isAfter(session.getQteExpiryTime())) {
                actionType = "MISS";
            }
            if (!"BLOCK".equalsIgnoreCase(actionType)) {
                int dmg = (int) (session.getEnemyAtk() * 1.5);
                session.setPlayerCurrentHp(session.getPlayerCurrentHp() - dmg);
                logs.add("❌ QTE Thất bại! Bạn chịu " + dmg + " sát thương.");
                if (session.getPlayerCurrentHp() <= 0) return handleLoss(session, character);
            } else {
                logs.add("🛡️ Đỡ đòn thành công!");
            }
            session.setQteActive(false);
            sessionRepo.save(session);
            return buildResult(session, logs, "ONGOING");
        }

        // -- B. Real-time stats calculation --
        session.setCurrentTurn(session.getCurrentTurn() + 1);
        Map<String, Double> pStats = calculateTotalStats(character);

        // --- PHASE 1: PLAYER ATTACK ---
        double pAcc = Math.min(pStats.getOrDefault("ACCURACY", 0.0), GameConstants.STAT_CAPS.get("ACCURACY"));
        double eEva = 10.0;
        double hitChance = 90.0 + (pAcc - eEva) / GameConstants.CONVERSION_RATE;
        hitChance = Math.max(20.0, Math.min(100.0, hitChance));

        if (random.nextDouble() * 100 < hitChance) {
            double pCrit = Math.min(pStats.getOrDefault("CRIT_RATE", 0.0), GameConstants.STAT_CAPS.get("CRIT_RATE"));
            double eLuck = 5.0;
            double critChance = (pCrit / GameConstants.CONVERSION_RATE) - (eLuck * 0.1);
            boolean isCrit = random.nextDouble() * 100 < critChance;

            double pAtk = pStats.getOrDefault("ATK", (double)character.getBaseAtk());
            double dmgMultiplier = 1.0;
            if (isCrit) {
                double pCritDmg = Math.min(pStats.getOrDefault("CRIT_DMG", 1500.0), GameConstants.STAT_CAPS.get("CRIT_DMG"));
                dmgMultiplier = pCritDmg / (GameConstants.CONVERSION_RATE * 10.0);
            }

            int finalDmg = (int) (pAtk * dmgMultiplier) - session.getEnemyDef();
            finalDmg = Math.max(1, finalDmg);

            session.setEnemyCurrentHp(session.getEnemyCurrentHp() - finalDmg);
            logs.add(isCrit ? "🔥 BẠO KÍCH! Gây " + finalDmg + " sát thương!" : "⚔️ Bạn gây " + finalDmg + " sát thương.");
        } else {
            logs.add("💨 Đánh trượt! (Quái né quá ghê)");
        }

        // Check Win
        if (session.getEnemyCurrentHp() <= 0) return handleWin(session, character);

        // --- PHASE 2: ENEMY ATTACK ---
        double pDef = pStats.getOrDefault("DEF", (double)character.getBaseDef());
        int dmgToPlayer = Math.max(1, (int)(session.getEnemyAtk() - pDef));

        double pEva = Math.min(pStats.getOrDefault("EVASION", 0.0), GameConstants.STAT_CAPS.get("EVASION"));
        double eAcc = 800.0;
        double enemyHitChance = 90.0 + (eAcc - pEva) / GameConstants.CONVERSION_RATE;

        if (random.nextDouble() * 100 < enemyHitChance) {
            session.setPlayerCurrentHp(session.getPlayerCurrentHp() - dmgToPlayer);
            logs.add("👾 " + session.getEnemyName() + " đánh trả " + dmgToPlayer + " máu.");
        } else {
            logs.add("✨ Bạn đã NÉ được đòn tấn công!");
        }

        // Check Loss
        if (session.getPlayerCurrentHp() <= 0) return handleLoss(session, character);

        sessionRepo.save(session);
        return buildResult(session, logs, "ONGOING");
    }

    // --- 3. STAT CALCULATION ---
    private Map<String, Double> calculateTotalStats(Character c) {
        Map<String, Double> totals = new HashMap<>();
        totals.put("HP", (double) c.getMaxHp());
        totals.put("ATK", (double) c.getBaseAtk());
        totals.put("DEF", (double) c.getBaseDef());
        totals.put("SPEED", (double) c.getBaseSpeed());
        totals.put("CRIT_RATE", (double) c.getBaseCritRate());
        totals.put("CRIT_DMG", (double) c.getBaseCritDmg());
        totals.put("ACCURACY", 0.0);
        totals.put("EVASION", 0.0);

        List<UserItem> equippedItems = userItemRepo.findByCharacter_CharIdAndIsEquippedTrue(c.getCharId());
        double[] flatBonus = new double[8];
        double[] percentBonus = new double[8];

        for (UserItem item : equippedItems) {
            if (item.getMainStatType() != null) {
                addToArrays(item.getMainStatType(), item.getMainStatValue().doubleValue(), flatBonus, percentBonus);
            }
            if (item.getSubStats() != null && !item.getSubStats().equals("[]")) {
                try {
                    List<SubStatDTO> subs = objectMapper.readValue(item.getSubStats(), new TypeReference<List<SubStatDTO>>() {});
                    for (SubStatDTO s : subs) {
                        addToArrays(s.getCode(), s.getValue(), flatBonus, percentBonus);
                    }
                } catch (Exception e) {}
            }
        }

        totals.put("HP", (totals.get("HP") + flatBonus[0]) * (1 + percentBonus[0]/100.0));
        totals.put("ATK", (totals.get("ATK") + flatBonus[1]) * (1 + percentBonus[1]/100.0));
        totals.put("DEF", (totals.get("DEF") + flatBonus[2]) * (1 + percentBonus[2]/100.0));
        totals.put("SPEED", totals.get("SPEED") + flatBonus[3]);
        totals.put("CRIT_RATE", totals.get("CRIT_RATE") + flatBonus[4]);
        totals.put("CRIT_DMG", totals.get("CRIT_DMG") + flatBonus[5]);
        totals.put("ACCURACY", totals.get("ACCURACY") + flatBonus[6]);
        totals.put("EVASION", totals.get("EVASION") + flatBonus[7]);

        return totals;
    }

    private void addToArrays(String type, double val, double[] flats, double[] percents) {
        switch (type) {
            case "HP_FLAT" -> flats[0] += val;
            case "HP_PERCENT" -> percents[0] += val;
            case "ATK_FLAT" -> flats[1] += val;
            case "ATK_PERCENT" -> percents[1] += val;
            case "DEF_FLAT" -> flats[2] += val;
            case "DEF_PERCENT" -> percents[2] += val;
            case "SPEED" -> flats[3] += val;
            case "CRIT_RATE" -> flats[4] += val;
            case "CRIT_DMG" -> flats[5] += val;
            case "ACCURACY" -> flats[6] += val;
            case "EVASION" -> flats[7] += val;
        }
    }

    // --- 4. HANDLE WIN / LOSS ---
    private BattleResult handleWin(BattleSession session, Character character) {
        BattleResult res = buildResult(session, "🏆 Chiến thắng!", "VICTORY");

        // [LOGIC MỚI] Lấy thông tin quái gốc để biết Gold/Exp Reward
        Enemy enemyRef = enemyRepo.findById(session.getEnemyId()).orElse(createDummyEnemy());
        int expReward = enemyRef.getExpReward();
        int goldReward = enemyRef.getGoldReward(); // Giả sử Enemy entity có field này

        // 1. Cộng EXP & Level Up
        character.setCurrentExp(character.getCurrentExp() + expReward);
        boolean leveledUp = checkLevelUp(character);
        res.setExpEarned(expReward);
        res.setLevelUp(leveledUp);

        // 2. Cộng Vàng vào Wallet
        Wallet wallet = character.getUser().getWallet();
        wallet.setGold(wallet.getGold().add(BigDecimal.valueOf(goldReward)));
        walletRepo.save(wallet);
        res.setGoldEarned(goldReward);

        // 3. Cập nhật Character
        character.setMonsterKills(character.getMonsterKills() + 1);
        character.setStatus(CharacterStatus.IDLE);
        int heal = (int)(character.getMaxHp() * 0.1);
        character.setCurrentHp(Math.min(character.getMaxHp(), Math.max(1, session.getPlayerCurrentHp()) + heal));
        charRepo.save(character);

        // 4. Rơi đồ (Item Drop)
        handleNewItemDrop(character, res);

        sessionRepo.delete(session);
        return res;
    }

    private BattleResult handleLoss(BattleSession session, Character character) {
        character.setCurrentHp(1); // Về làng dưỡng thương
        character.setStatus(CharacterStatus.IDLE);
        charRepo.save(character);
        sessionRepo.delete(session);
        return buildResult(session, "💀 Thất bại! Bạn đã kiệt sức.", "DEFEAT");
    }

    // [CẬP NHẬT] Hàm rơi đồ trả về thông tin cho BattleResult
    private void handleNewItemDrop(Character character, BattleResult result) {
        if (random.nextDouble() > DROP_RATE) return;

        List<Item> allItems = itemRepo.findAll();
        if (allItems.isEmpty()) return;

        Item baseItem = allItems.get(random.nextInt(allItems.size()));

        UserItem newItem = new UserItem();
        newItem.setCharacter(character);
        newItem.setItem(baseItem);
        newItem.setQuantity(1);
        newItem.setIsEquipped(false);
        newItem.setEnhancementLevel(0);
        newItem.setAcquiredAt(LocalDateTime.now());
        newItem.setRarity(Rarity.COMMON);

        newItem.setMainStatType("ATK_FLAT");
        newItem.setMainStatValue(BigDecimal.valueOf(10));

        if (baseItem.getSlotType() != SlotType.NONE && baseItem.getSlotType() != SlotType.CONSUMABLE) {
            List<SubStatDTO> stats = new ArrayList<>();
            int initialSubs = random.nextInt(2) + 1;

            if (baseItem.getSlotType() == SlotType.WEAPON) {
                newItem.setMainStatType("ATK_FLAT");
                newItem.setMainStatValue(BigDecimal.valueOf(baseItem.getTier() * 10));
            } else if (baseItem.getSlotType() == SlotType.ARMOR) {
                newItem.setMainStatType("DEF_FLAT");
                newItem.setMainStatValue(BigDecimal.valueOf(baseItem.getTier() * 5));
            }

            for(int i=0; i<initialSubs; i++) {
                SubStatDTO sub = itemGenService.generateRandomSubStat(newItem, stats);
                stats.add(sub);
            }

            try {
                newItem.setSubStats(objectMapper.writeValueAsString(stats));
            } catch (Exception e) {
                newItem.setSubStats("[]");
            }
        } else {
            newItem.setSubStats("[]");
            newItem.setMainStatValue(BigDecimal.ZERO);
        }

        userItemRepo.save(newItem);

        // Cập nhật thông tin drop vào Result để FE hiển thị
        result.setDroppedItemName(baseItem.getName());
        result.setDroppedItemImage(baseItem.getImageUrl()); // Giả sử Item có field này
        // result.setDroppedItemRarity(newItem.getRarity().toString()); // Nếu cần rarity
        result.getCombatLog().add("🎁 Nhặt được: " + baseItem.getName());
    }

    private boolean checkLevelUp(Character c) {
        boolean leveled = false;
        long reqExp = c.getLevel() * 100L;
        while (c.getCurrentExp() >= reqExp) {
            c.setCurrentExp(c.getCurrentExp() - reqExp);
            c.setLevel(c.getLevel() + 1);
            c.setMaxHp(c.getMaxHp() + 50);
            c.setBaseAtk(c.getBaseAtk() + 5);
            c.setBaseDef(c.getBaseDef() + 2);
            c.setCurrentHp(c.getMaxHp());
            c.setCurrentEnergy(c.getMaxEnergy());
            reqExp = c.getLevel() * 100L;
            leveled = true;
        }
        return leveled;
    }

    private Enemy createDummyEnemy() {
        Enemy e = new Enemy();
        e.setEnemyId(0);
        e.setName("Bù Nhìn");
        e.setHp(100);
        e.setAtk(5);
        e.setDef(0);
        e.setSpeed(10);
        e.setExpReward(5);
        e.setGoldReward(10); // Default gold
        return e;
    }

    // --- Result Builders ---
    private BattleResult buildResult(BattleSession s, List<String> logs, String status) {
        BattleResult res = new BattleResult();
        res.setEnemyId(s.getEnemyId()); // [NEW] Trả về ID để FE render ảnh
        res.setEnemyName(s.getEnemyName());
        res.setEnemyHp(s.getEnemyCurrentHp());
        res.setEnemyMaxHp(s.getEnemyMaxHp());
        res.setPlayerHp(s.getPlayerCurrentHp());
        res.setPlayerMaxHp(s.getPlayerMaxHp());
        res.setPlayerEnergy(s.getPlayerCurrentEnergy());
        res.setCombatLog(logs);
        res.setStatus(status);
        return res;
    }

    private BattleResult buildResult(BattleSession s, String msg, String status) {
        List<String> logs = new ArrayList<>();
        logs.add(msg);
        return buildResult(s, logs, status);
    }
}