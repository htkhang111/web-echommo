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

    public enum GameMap {
        MAP_01("MAP_01", "Đồng Bằng", 1, 19, Map.of("Gỗ", 40, "Đá", 30), List.of("Goblin", "Skeleton")),
        MAP_02("MAP_02", "Rừng Rậm", 20, 30, Map.of("Gỗ", 30, "Đá", 20), List.of("Người Rừng", "Gấu Hoang"));

        public final String id; public final String name; public final int minLv; public final int maxLv;
        public final Map<String, Integer> resources; public final List<String> enemies;
        GameMap(String id, String name, int minLv, int maxLv, Map<String, Integer> resources, List<String> enemies) {
            this.id = id; this.name = name; this.minLv = minLv; this.maxLv = maxLv; this.resources = resources; this.enemies = enemies;
        }
        public static GameMap findById(String id) { return Arrays.stream(values()).filter(m -> m.id.equalsIgnoreCase(id)).findFirst().orElse(MAP_01); }
    }

    @Transactional
    public ExplorationResponse explore(String mapId) {
        Character c = characterService.getMyCharacter();
        if (c == null) throw new RuntimeException("Chưa có nhân vật!");

        captchaService.checkLockStatus(c.getUser());
        GameMap map = GameMap.findById(mapId);
        if (c.getLevel() < map.minLv) throw new RuntimeException("Cấp độ không đủ! Cần Lv." + map.minLv);

        Random r = new Random();
        Wallet w = c.getUser().getWallet();

        // [FIX] Cộng EXP (Dùng Long)
        long expGain = 10L + c.getLevel();
        c.setCurrentExp(c.getCurrentExp() + expGain);

        // Cộng Vàng
        BigDecimal coinGain = BigDecimal.valueOf(1 + r.nextInt(5));
        w.setGold(w.getGold().add(coinGain));

        int roll = r.nextInt(100);
        String type; String msg; String rewardName = null; Integer rewardAmount = 0;

        if (roll < 60) {
            type = "TEXT";
            msg = flavorTextRepo.findRandomContent().orElse("Bạn đang đi dạo và ngắm cảnh.");
        } else if (roll < 80) {
            type = "GATHERING";
            msg = "Phát hiện khu vực tài nguyên!";
        } else if (roll < 91) {
            type = "ENEMY";
            String enemyName = map.enemies.get(r.nextInt(map.enemies.size()));
            BattleSession session = createBattleSession(c, enemyName, map);
            msg = "Nguy hiểm! Gặp " + session.getEnemyName() + "!";
            c.setStatus(CharacterStatus.IN_COMBAT);
            rewardName = session.getEnemyName();
        } else {
            type = "ITEM";
            List<Item> items = itemRepo.findAll();
            if (!items.isEmpty()) {
                Item item = items.get(r.nextInt(items.size()));
                addItemToInventory(c, item, 1);
                msg = "May mắn! Nhặt được " + item.getName();
                rewardName = item.getName();
                rewardAmount = 1;
            } else {
                type = "TEXT"; msg = "Tìm thấy một cái rương rỗng.";
            }
        }

        // [QUAN TRỌNG] Kiểm tra lên cấp sau khi cộng EXP
        Integer newLevel = checkLevelUp(c);

        characterRepository.save(c);
        walletRepository.save(w);

        return new ExplorationResponse(
                msg,
                type,
                coinGain,
                c.getCurrentExp(), // Long (OK)
                c.getLevel(),
                c.getCurrentEnergy(),
                c.getMaxEnergy(),
                newLevel,
                rewardName,
                rewardAmount
        );
    }

    // --- LOGIC LÊN CẤP (QUAN TRỌNG) ---
    private Integer checkLevelUp(Character c) {
        // Công thức: Level * 100
        long reqExp = (long) c.getLevel() * 100L;

        if (c.getCurrentExp() >= reqExp) {
            c.setCurrentExp(c.getCurrentExp() - reqExp);
            c.setLevel(c.getLevel() + 1);
            c.setMaxHp(c.getMaxHp() + 20);
            c.setCurrentHp(c.getMaxHp());
            c.setCurrentEnergy(c.getMaxEnergy());

            // Đệ quy check tiếp (nếu cộng dồn nhiều exp)
            checkLevelUp(c);

            return c.getLevel();
        }
        return null;
    }

    // --- API THU THẬP TÀI NGUYÊN ---
    @Transactional
    public Map<String, Object> gatherResource(String resourceType, int amount) {
        Character c = characterService.getMyCharacter();
        if (c.getCurrentEnergy() < amount) throw new RuntimeException("Không đủ năng lượng!");

        c.setCurrentEnergy(c.getCurrentEnergy() - amount);
        characterRepository.save(c);

        // Map resourceType sang Item Name trong DB
        String itemName = "Gỗ";
        if ("stone".equals(resourceType)) itemName = "Đá";
        if ("mining".equals(resourceType)) itemName = "Quặng Đồng";

        Item item = itemRepo.findByName(itemName).orElse(null);
        if (item != null) {
            addItemToInventory(c, item, amount);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Thu hoạch thành công " + amount + " " + itemName);
        res.put("currentEnergy", c.getCurrentEnergy());
        return res;
    }

    private BattleSession createBattleSession(Character player, String enemyName, GameMap map) {
        BattleSession session = battleSessionRepo.findByCharacter_CharId(player.getCharId())
                .orElse(new BattleSession());
        session.setCharacter(player);
        session.setEnemyName(enemyName);
        session.setEnemyMaxHp(100 + (map.minLv * 10)); // Máu quái tăng theo map
        session.setEnemyCurrentHp(session.getEnemyMaxHp());
        session.setEnemyAtk(10 + map.minLv);
        session.setEnemyDef(map.minLv);
        session.setEnemySpeed(10);
        session.setCurrentTurn(0);
        session.setLog("Bạn đụng độ " + enemyName);
        return battleSessionRepo.save(session);
    }

    private void addItemToInventory(Character character, Item item, int amount) {
        // Tìm xem đã có item này trong túi chưa (chỉ tìm item không phải trang bị để stack)
        Optional<UserItem> existing = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), item.getItemId())
                .stream()
                .filter(ui -> !Boolean.TRUE.equals(ui.getIsEquipped())) // Không lấy đồ đang mặc
                .findFirst();

        if (existing.isPresent()) {
            UserItem ui = existing.get();
            ui.setQuantity(ui.getQuantity() + amount);
            userItemRepo.save(ui);
        } else {
            UserItem ui = new UserItem();
            ui.setCharacter(character);
            ui.setItem(item);
            ui.setQuantity(amount);
            ui.setIsEquipped(false);
            ui.setEnhanceLevel(0);
            userItemRepo.save(ui);
        }
    }
}