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

    // [TẠM KHÓA] Để code chạy được ngay cả khi chưa có file này.
    // Khi nào làm chức năng rèn đồ thì bỏ comment dòng dưới và Import vào.
    // private final ItemGenerationService itemGenService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    private static final double DROP_RATE = 0.5; // Tỷ lệ rơi đồ 50%

    // --- HELPER: Lấy User & Character ---
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

    // --- 1. START BATTLE (ĐÃ SỬA LỖI) ---
    // Logic đúng: Tìm session cũ từ ExplorationService gửi sang. KHÔNG tạo mới.
    @Transactional
    public BattleResult startBattle() {
        Character character = getMyCharacter();

        BattleSession session = sessionRepo.findByCharacter_CharId(character.getCharId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đối thủ! Hãy vào Hành Trang đi dạo trước."));

        // Cập nhật trạng thái nhân vật
        character.setStatus(CharacterStatus.IN_COMBAT);
        charRepo.save(character);

        return buildResult(session, "Tiếp tục chiến đấu với " + session.getEnemyName() + "!", "ONGOING");
    }

    // --- 2. PROCESS TURN (XỬ LÝ LƯỢT ĐÁNH) ---
    @Transactional
    public BattleResult processTurn(String actionType) {
        Character character = getMyCharacter();
        BattleSession session = sessionRepo.findByCharacter_CharId(character.getCharId())
                .orElseThrow(() -> new RuntimeException("Trận đấu đã kết thúc!"));

        List<String> logs = new ArrayList<>();

        // -- A. Xử lý QTE (Quick Time Event) --
        if (session.isQteActive()) {
            if (session.getQteExpiryTime() != null && LocalDateTime.now().isAfter(session.getQteExpiryTime())) {
                actionType = "MISS";
            }
            if (!"BLOCK".equalsIgnoreCase(actionType)) {
                int dmg = (int) (session.getEnemyAtk() * 1.5);
                session.setPlayerCurrentHp(Math.max(0, session.getPlayerCurrentHp() - dmg));
                logs.add("❌ QTE Thất bại! Bạn chịu " + dmg + " sát thương.");
                if (session.getPlayerCurrentHp() <= 0) return handleLoss(session, character);
            } else {
                logs.add("🛡️ Đỡ đòn thành công!");
            }
            session.setQteActive(false);
            sessionRepo.save(session);
            return buildResult(session, logs, "ONGOING");
        }

        // -- B. Tính toán Damage --
        session.setCurrentTurn(session.getCurrentTurn() + 1);
        Map<String, Double> pStats = calculateTotalStats(character);

        // --- PHASE 1: PLAYER ĐÁNH QUÁI ---
        double pAtk = pStats.getOrDefault("ATK", (double)character.getBaseAtk());

        // Tính né tránh/chính xác (Cơ bản)
        boolean isMiss = random.nextInt(100) < 5; // 5% trượt
        if (isMiss) {
            logs.add("💨 Bạn đánh trượt!");
        } else {
            // Tính Damage: (Atk - Def) * Random(0.9 ~ 1.1)
            int pDmg = Math.max(1, (int)pAtk - session.getEnemyDef());

            // Chí mạng
            double critRate = pStats.getOrDefault("CRIT_RATE", (double)character.getBaseCritRate());
            if (random.nextDouble() * 100 < critRate) {
                pDmg = (int)(pDmg * 1.5);
                logs.add("🔥 BẠO KÍCH! Gây " + pDmg + " sát thương!");
            } else {
                logs.add("⚔️ Bạn gây " + pDmg + " sát thương.");
            }

            session.setEnemyCurrentHp(Math.max(0, session.getEnemyCurrentHp() - pDmg));
        }

        // CHECK WIN NGAY LẬP TỨC
        if (session.getEnemyCurrentHp() <= 0) return handleWin(session, character);

        // --- PHASE 2: QUÁI ĐÁNH TRẢ ---
        double pDef = pStats.getOrDefault("DEF", (double)character.getBaseDef());
        int eDmg = Math.max(1, session.getEnemyAtk() - (int)pDef);

        // Player né (Giả sử 5% né mặc định)
        if (random.nextInt(100) < 5) {
            logs.add("✨ Bạn đã NÉ được đòn tấn công!");
        } else {
            session.setPlayerCurrentHp(Math.max(0, session.getPlayerCurrentHp() - eDmg));
            logs.add("👾 " + session.getEnemyName() + " đánh trả " + eDmg + " máu.");
        }

        // CHECK THUA
        if (session.getPlayerCurrentHp() <= 0) return handleLoss(session, character);

        sessionRepo.save(session);
        return buildResult(session, logs, "ONGOING");
    }

    // --- 3. LOGIC THẮNG (NHẬN THƯỞNG) ---
    private BattleResult handleWin(BattleSession session, Character character) {
        BattleResult res = buildResult(session, "🏆 Chiến thắng!", "VICTORY");

        // [QUAN TRỌNG] Lấy ID quái thật từ session để tra cứu phần thưởng
        Enemy enemyRef = enemyRepo.findById(session.getEnemyId()).orElse(createDummyEnemy());

        int expReward = enemyRef.getExpReward();
        int goldReward = enemyRef.getGoldReward();

        // 1. Cộng EXP & Level Up
        character.setCurrentExp(character.getCurrentExp() + expReward);
        boolean leveledUp = checkLevelUp(character);
        res.setExpEarned(expReward);
        res.setLevelUp(leveledUp);

        // 2. Cộng Vàng
        Wallet wallet = character.getUser().getWallet();
        if (wallet == null) {
            wallet = new Wallet(); wallet.setUser(character.getUser()); wallet.setGold(BigDecimal.ZERO);
        }
        wallet.setGold(wallet.getGold().add(BigDecimal.valueOf(goldReward)));
        walletRepo.save(wallet);
        res.setGoldEarned(goldReward);

        // 3. Reset trạng thái & Hồi máu nhẹ
        character.setMonsterKills(character.getMonsterKills() + 1);
        character.setStatus(CharacterStatus.IDLE);

        int healAmount = (int)(character.getMaxHp() * 0.1); // Hồi 10% máu
        character.setCurrentHp(Math.min(character.getMaxHp(), Math.max(1, session.getPlayerCurrentHp()) + healAmount));
        charRepo.save(character);

        // 4. Rơi đồ
        handleNewItemDrop(character, res);

        // 5. Xóa trận đấu
        sessionRepo.delete(session);
        res.setEnemyHp(0); // Để hiển thị thanh máu về 0

        return res;
    }

    // --- 4. LOGIC THUA ---
    private BattleResult handleLoss(BattleSession session, Character character) {
        character.setCurrentHp(1); // Về làng dưỡng thương (còn 1 máu)
        character.setStatus(CharacterStatus.IDLE);
        charRepo.save(character);
        sessionRepo.delete(session); // Xóa trận đấu
        return buildResult(session, "💀 Thất bại! Bạn đã kiệt sức.", "DEFEAT");
    }

    // --- 5. LOGIC RƠI ĐỒ (ITEM DROP) ---
    private void handleNewItemDrop(Character character, BattleResult result) {
        if (random.nextDouble() > DROP_RATE) return; // 50% tỉ lệ rơi

        List<Item> allItems = itemRepo.findAll();
        // Lọc item là trang bị (ID >= 13)
        List<Item> equipItems = allItems.stream().filter(i -> i.getItemId() >= 13).toList();

        if (equipItems.isEmpty()) return;

        Item baseItem = equipItems.get(random.nextInt(equipItems.size()));

        UserItem newItem = new UserItem();
        newItem.setCharacter(character);
        newItem.setItem(baseItem);
        newItem.setQuantity(1);
        newItem.setIsEquipped(false);
        newItem.setEnhanceLevel(0);
        newItem.setAcquiredAt(LocalDateTime.now());
        newItem.setRarity(Rarity.COMMON);

        // Random chỉ số cơ bản
        newItem.setMainStatType("ATK_FLAT");
        newItem.setMainStatValue(BigDecimal.valueOf(10 + random.nextInt(10)));
        newItem.setSubStats("[]"); // Mặc định rỗng để tránh lỗi JSON

        // [LOGIC SINH DÒNG PHỤ - ĐANG TẠM KHÓA ĐỂ TRÁNH LỖI NẾU THIẾU SERVICE]
        /*
        if (itemGenService != null && baseItem.getSlotType() != SlotType.NONE) {
             // ... Code sinh dòng phụ của bạn ...
        }
        */

        userItemRepo.save(newItem);

        // Trả về thông tin để hiển thị popup
        result.setDroppedItemName(baseItem.getName());
        result.setDroppedItemImage(baseItem.getImageUrl());
        result.getCombatLog().add("🎁 Nhặt được: " + baseItem.getName());
    }

    // --- CÁC HÀM PHỤ TRỢ KHÁC ---
    private Map<String, Double> calculateTotalStats(Character c) {
        Map<String, Double> totals = new HashMap<>();
        totals.put("HP", (double) c.getMaxHp());
        totals.put("ATK", (double) c.getBaseAtk());
        totals.put("DEF", (double) c.getBaseDef());
        // Có thể mở rộng cộng chỉ số từ trang bị ở đây
        return totals;
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
        e.setEnemyId(0); e.setName("Bù Nhìn"); e.setHp(100); e.setAtk(5); e.setDef(0); e.setSpeed(10);
        e.setExpReward(10); e.setGoldReward(10);
        return e;
    }

    private BattleResult buildResult(BattleSession s, List<String> logs, String status) {
        BattleResult res = new BattleResult();
        res.setEnemyId(s.getEnemyId());
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
        List<String> logs = new ArrayList<>(); logs.add(msg); return buildResult(s, logs, status);
    }
}