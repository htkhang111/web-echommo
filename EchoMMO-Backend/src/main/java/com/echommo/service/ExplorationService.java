package com.echommo.service;

import com.echommo.dto.ExplorationResponse;
import com.echommo.entity.*;
import com.echommo.entity.Character;
import com.echommo.enums.CharacterStatus;
import com.echommo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExplorationService {

    @Autowired private CharacterService characterService;
    @Autowired private CharacterRepository characterRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private CaptchaService captchaService;
    @Autowired private ItemRepository itemRepo;
    @Autowired private UserItemRepository userItemRepo;
    @Autowired private FlavorTextRepository flavorTextRepo;
    @Autowired private BattleSessionRepository battleSessionRepo;
    @Autowired private EnemyRepository enemyRepo;

    // Cấu hình Map
    public enum GameMap {
        MAP_01("MAP_01", "Đồng Bằng", 1, 19,
                List.of(1, 1, 1, 1, 5, 5, 5, 6, 6, 9),
                List.of("Slime Đồng Cỏ", "Sói Non", "Chuột Đồng")),

        MAP_02("MAP_02", "Rừng Rậm", 20, 30,
                createWeightedList(Map.of(1, 6, 5, 4, 6, 4, 7, 3, 9, 3)),
                List.of("Người Rừng", "Gấu Hoang", "Nhện Rừng")),

        MAP_03("MAP_03", "Sa Mạc", 30, 40,
                createWeightedList(Map.of(2, 4, 5, 6, 6, 5, 7, 5)),
                List.of("Bọ Cát", "Xác Ướp Lang Thang", "Bọ Hung Khổng Lồ")),

        MAP_04("MAP_04", "Núi Cao", 40, 50,
                createWeightedList(Map.of(1, 3, 5, 5, 6, 4, 7, 5, 8, 3)),
                List.of("Golem Đá", "Đại Bàng Núi", "Người Lùn Đào Mỏ")),

        MAP_05("MAP_05", "Băng Đảo", 50, 60,
                createWeightedList(Map.of(3, 2, 7, 7, 8, 7, 12, 4)),
                List.of("Gấu Băng", "Người Tuyết", "Tinh Linh Băng")),

        MAP_06("MAP_06", "Đầm Lầy", 60, 70,
                createWeightedList(Map.of(4, 3, 10, 2, 11, 1, 12, 4)),
                List.of("Quái Đầm Lầy", "Rắn Độc Khổng Lồ", "Linh Hồn Sa Lầy"));

        public final String id; public final String name;
        public final int minLv; public final int maxLv;
        public final List<Integer> resourceIds;
        public final List<String> enemies;

        GameMap(String id, String name, int minLv, int maxLv, List<Integer> resourceIds, List<String> enemies) {
            this.id = id; this.name = name; this.minLv = minLv; this.maxLv = maxLv;
            this.resourceIds = resourceIds; this.enemies = enemies;
        }

        private static List<Integer> createWeightedList(Map<Integer, Integer> weights) {
            List<Integer> list = new ArrayList<>();
            weights.forEach((id, count) -> {
                for (int i = 0; i < count; i++) list.add(id);
            });
            return list;
        }

        public static GameMap findById(String id) {
            return Arrays.stream(values()).filter(m -> m.id.equalsIgnoreCase(id)).findFirst().orElse(MAP_01);
        }
    }

    @Transactional
    public ExplorationResponse explore(String mapId) {
        Character c = characterService.getMyCharacter();
        if (c == null) throw new RuntimeException("Chưa có nhân vật!");

        c = characterRepository.findByUser_UserIdWithUserAndWallet(c.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("Lỗi đồng bộ dữ liệu ví tiền!"));

        captchaService.checkLockStatus(c.getUser());

        // --- [MIỄN PHÍ HÀNH TẨU - ĐỂ TEST] ---
        // if (c.getCurrentEnergy() < 1) throw new RuntimeException("Bạn đã kiệt sức! Hãy nghỉ ngơi.");

        GameMap map = GameMap.findById(mapId);
        if (c.getLevel() < map.minLv) throw new RuntimeException("Cấp độ không đủ! Cần Lv." + map.minLv);

        c.setCurrentMapId(map.id);
        c.setCurrentLocation(map.name);

        Random r = new Random();
        Wallet w = c.getUser().getWallet();

        // --- [MIỄN PHÍ HÀNH TẨU - ĐỂ TEST] ---
        // c.setCurrentEnergy(c.getCurrentEnergy() - 1);

        long expGain = 10L + c.getLevel();
        c.setCurrentExp(c.getCurrentExp() + expGain);
        BigDecimal coinGain = BigDecimal.valueOf(1 + r.nextInt(5));
        w.setGold(w.getGold().add(coinGain));

        int roll = r.nextInt(100);
        String type; String msg;
        String rewardName = null; Integer rewardAmount = 0; Integer rewardItemId = null;

        if (roll < 60) { // 60% TEXT
            type = "TEXT";
            msg = flavorTextRepo.findRandomContent().orElse("Bạn đang đi dạo và ngắm cảnh.");
            clearGatheringState(c);
        } else if (roll < 80) { // 20% GATHERING
            type = "GATHERING";
            Integer resourceId = map.resourceIds.get(r.nextInt(map.resourceIds.size()));
            Item item = itemRepo.findById(resourceId).orElse(null);

            if (item != null) {
                int nodeSize = 10 + r.nextInt(11);
                c.setGatheringItemId(item.getItemId());
                c.setGatheringRemainingAmount(nodeSize);
                c.setGatheringExpiry(LocalDateTime.now().plusMinutes(5));

                msg = "Phát hiện mỏ " + item.getName() + "!";
                rewardName = item.getName();
                rewardAmount = nodeSize;
                rewardItemId = item.getItemId();
            } else {
                type = "TEXT"; msg = "Khu vực này có vẻ đã bị khai thác hết.";
                clearGatheringState(c);
            }
        } else if (roll < 91) { // 11% ENEMY
            type = "ENEMY";

            // [LOGIC] Lấy quái theo Map
            String targetEnemyName = map.enemies.get(r.nextInt(map.enemies.size()));

            Enemy enemy = enemyRepo.findAll().stream()
                    .filter(e -> e.getName().equalsIgnoreCase(targetEnemyName))
                    .findFirst()
                    .orElse(null);

            if (enemy == null) {
                // Tạo quái ảo nếu chưa có trong DB
                enemy = new Enemy();
                enemy.setEnemyId(0);
                enemy.setName(targetEnemyName);
                enemy.setHp(100 + map.minLv * 10);
                enemy.setAtk(10 + map.minLv);
                enemy.setDef(map.minLv);
                enemy.setSpeed(10);
                enemy.setExpReward(10);
                enemy.setGoldReward(5);
            }

            BattleSession session = createBattleSession(c, enemy);

            msg = "Nguy hiểm! Gặp " + session.getEnemyName() + "!";
            c.setStatus(CharacterStatus.IN_COMBAT);
            rewardName = session.getEnemyName();
            clearGatheringState(c);

        } else { // 9% ITEM
            type = "ITEM";
            List<Item> items = itemRepo.findAll().stream().filter(i -> i.getItemId() >= 13).toList();
            if (!items.isEmpty()) {
                Item item = items.get(r.nextInt(items.size()));
                addItemToInventory(c, item, 1);
                msg = "May mắn! Nhặt được " + item.getName();
                rewardName = item.getName(); rewardAmount = 1; rewardItemId = item.getItemId();
            } else {
                type = "TEXT"; msg = "Bạn tìm thấy một chiếc rương cũ nhưng bên trong trống rỗng.";
            }
            clearGatheringState(c);
        }

        Integer newLevel = checkLevelUp(c);
        characterRepository.save(c);
        walletRepository.save(w);

        return ExplorationResponse.builder()
                .message(msg)
                .type(type)
                .goldGained(coinGain)
                .currentExp(c.getCurrentExp())
                .currentLv(c.getLevel())
                .currentEnergy(c.getCurrentEnergy())
                .maxEnergy(c.getMaxEnergy())
                .newLevel(newLevel)
                .rewardName(rewardName)
                .rewardAmount(rewardAmount)
                .rewardItemId(rewardItemId)
                .build();
    }

    private BattleSession createBattleSession(Character player, Enemy enemy) {
        // [FIX LỖI 3 RESULTS RETURNED]
        // Vì DB đang bị lỗi lưu trùng 3 dòng, ta dùng List để lấy hết chúng lên
        List<BattleSession> oldSessions = battleSessionRepo.findByCharacter_CharId(player.getCharId());

        // Sau đó xóa sạch sẽ
        if (!oldSessions.isEmpty()) {
            battleSessionRepo.deleteAll(oldSessions);
            battleSessionRepo.flush(); // Bắt buộc xóa ngay lập tức
        }

        // Tạo Session mới tinh
        BattleSession session = new BattleSession();
        session.setCharacter(player);

        session.setEnemyId(enemy.getEnemyId());
        session.setEnemyName(enemy.getName());
        session.setEnemyMaxHp(enemy.getHp());
        session.setEnemyCurrentHp(enemy.getHp());
        session.setEnemyAtk(enemy.getAtk());
        session.setEnemyDef(enemy.getDef());
        session.setEnemySpeed(enemy.getSpeed());

        session.setPlayerMaxHp(player.getMaxHp());
        session.setPlayerCurrentHp(player.getCurrentHp());
        session.setPlayerCurrentEnergy(player.getCurrentEnergy());

        session.setCurrentTurn(0);
        session.setLog("Bạn đụng độ " + enemy.getName());
        return battleSessionRepo.save(session);
    }

    // --- Helpers ---
    @Transactional
    public Map<String, Object> gatherResource(Integer itemId, int amount) {
        Character c = characterService.getMyCharacter();
        if (c.getGatheringItemId() == null || !c.getGatheringItemId().equals(itemId)) throw new RuntimeException("Lỗi tài nguyên!");
        if (c.getGatheringExpiry() != null && LocalDateTime.now().isAfter(c.getGatheringExpiry())) {
            clearGatheringState(c); characterRepository.save(c); throw new RuntimeException("Mỏ tài nguyên đã cạn kiệt!");
        }
        if (c.getGatheringRemainingAmount() < amount) amount = c.getGatheringRemainingAmount();

        // [ĐÃ SỬA: KHAI THÁC PHẢI TỐN THỂ LỰC]
        // Đã bỏ comment 2 dòng dưới đây để tính năng trừ thể lực hoạt động lại
        if (c.getCurrentEnergy() < amount) throw new RuntimeException("Không đủ năng lượng!");
        c.setCurrentEnergy(c.getCurrentEnergy() - amount);

        c.setGatheringRemainingAmount(c.getGatheringRemainingAmount() - amount);
        Wallet wallet = c.getUser().getWallet();
        addItemToWallet(wallet, itemId, amount);
        walletRepository.save(wallet);
        if (c.getGatheringRemainingAmount() <= 0) clearGatheringState(c);
        characterRepository.save(c);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Thu hoạch thành công");
        res.put("currentEnergy", c.getCurrentEnergy());
        res.put("remainingAmount", c.getGatheringRemainingAmount());
        return res;
    }

    private int safeGet(Integer value) { return value == null ? 0 : value; }
    private void addItemToWallet(Wallet w, int itemId, int amount) {
        switch (itemId) {
            case 1: w.setWood(safeGet(w.getWood()) + amount); break;
            case 2: w.setDriedWood(safeGet(w.getDriedWood()) + amount); break;
            case 3: w.setColdWood(safeGet(w.getColdWood()) + amount); break;
            case 4: w.setStrangeWood(safeGet(w.getStrangeWood()) + amount); break;
            case 5: w.setStone(safeGet(w.getStone()) + amount); break;
            case 6: w.setCopperOre(safeGet(w.getCopperOre()) + amount); break;
            case 7: w.setIronOre(safeGet(w.getIronOre()) + amount); break;
            case 8: w.setPlatinum(safeGet(w.getPlatinum()) + amount); break;
            case 9: w.setFish(safeGet(w.getFish()) + amount); break;
            case 10: w.setShark(safeGet(w.getShark()) + amount); break;
            case 11: w.setEchoCoin(safeGet(w.getEchoCoin()) + amount); break;
            case 12: w.setUnknownMaterial(safeGet(w.getUnknownMaterial()) + amount); break;
        }
    }
    private void clearGatheringState(Character c) { c.setGatheringItemId(null); c.setGatheringRemainingAmount(0); c.setGatheringExpiry(null); }
    private Integer checkLevelUp(Character c) {
        Integer leveledUpTo = null;
        while (c.getCurrentExp() >= (long) c.getLevel() * 100L) {
            c.setCurrentExp(c.getCurrentExp() - (long) c.getLevel() * 100L);
            c.setLevel(c.getLevel() + 1);
            c.setMaxHp(c.getMaxHp() + 20); c.setCurrentHp(c.getMaxHp());
            c.setCurrentEnergy(c.getMaxEnergy());
            leveledUpTo = c.getLevel();
        }
        return leveledUpTo;
    }
    private void addItemToInventory(Character character, Item item, int amount) {
        Optional<UserItem> existing = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), item.getItemId())
                .stream().filter(ui -> !Boolean.TRUE.equals(ui.getIsEquipped())).findFirst();
        if (existing.isPresent()) {
            UserItem ui = existing.get(); ui.setQuantity(ui.getQuantity() + amount); userItemRepo.save(ui);
        } else {
            UserItem ui = new UserItem(); ui.setCharacter(character); ui.setItem(item); ui.setQuantity(amount);
            ui.setIsEquipped(false); ui.setEnhanceLevel(0); userItemRepo.save(ui);
        }
    }
}