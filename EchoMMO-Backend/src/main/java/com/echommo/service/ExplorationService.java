package com.echommo.service;

import com.echommo.dto.ExplorationResponse;
import com.echommo.entity.*;
import com.echommo.entity.Character;
import com.echommo.enums.CharacterStatus;
import com.echommo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class ExplorationService {

    @Autowired private CharacterService characterService;
    @Autowired private CharacterRepository characterRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CaptchaService captchaService;
    @Autowired private ItemRepository itemRepo;
    @Autowired private UserItemRepository userItemRepo;
    @Autowired private FlavorTextRepository flavorTextRepo;
    @Autowired private WeatherTextRepository weatherTextRepo;
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
        if (c.getLevel() < map.minLv) throw new RuntimeException("Cấp độ không đủ!");

        Random r = new Random();
        Wallet w = c.getUser().getWallet();
        c.setCurrentExp(c.getCurrentExp() + 10);
        w.setGold(w.getGold().add(BigDecimal.ONE));

        int roll = r.nextInt(100);
        String type; String msg; String rewardName = null; Integer rewardAmount = 0;

        if (roll < 60) {
            type = "TEXT";
            msg = flavorTextRepo.findRandomContent().orElse("Bạn đang đi dạo.");
        } else if (roll < 80) {
            type = "GATHERING";
            msg = "Tìm thấy tài nguyên!";
        } else if (roll < 91) {
            type = "ENEMY";
            BattleSession session = createBattleSession(c, map.enemies.get(r.nextInt(map.enemies.size())), map);
            msg = "Gặp " + session.getEnemyName() + "!";
            c.setStatus(CharacterStatus.IN_COMBAT);
            rewardName = session.getEnemyName();
        } else {
            type = "ITEM";
            List<Item> items = itemRepo.findAll();
            if (!items.isEmpty()) {
                Item item = items.get(r.nextInt(items.size()));
                addItemToInventory(c, item, 1);
                msg = "Nhặt được " + item.getName();
                rewardName = item.getName();
                rewardAmount = 1;
            } else {
                type = "TEXT"; msg = "Rương rỗng.";
            }
        }

        characterRepository.save(c);
        return new ExplorationResponse(msg, type, BigDecimal.ONE, c.getCurrentExp(), c.getLevel(), c.getCurrentEnergy(), c.getMaxEnergy(), null, rewardName, rewardAmount);
    }

    // --- PHƯƠNG THỨC MÀ CONTROLLER ĐANG THIẾU ---
    @Transactional
    public Map<String, Object> gatherResource(String resourceType, int amount) {
        Character c = characterService.getMyCharacter();
        if (c.getCurrentEnergy() < amount) throw new RuntimeException("Thiếu năng lượng");

        c.setCurrentEnergy(c.getCurrentEnergy() - amount);
        characterRepository.save(c);

        // Logic dummy trả về item
        String itemName = "Gỗ";
        if ("stone".equals(resourceType)) itemName = "Đá";

        Item item = itemRepo.findByName(itemName).orElse(null);
        if (item != null) addItemToInventory(c, item, amount);

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Thu hoạch thành công " + amount + " " + itemName);
        res.put("currentEnergy", c.getCurrentEnergy());
        return res;
    }

    private BattleSession createBattleSession(Character player, String enemyName, GameMap map) {
        BattleSession session = battleSessionRepo.findByCharacter_CharId(player.getCharId()).orElse(new BattleSession());
        session.setCharacter(player);
        session.setEnemyName(enemyName);
        session.setEnemyMaxHp(100);
        session.setEnemyCurrentHp(100);
        session.setEnemyAtk(10);
        session.setEnemyDef(0);
        session.setEnemySpeed(10);
        session.setCurrentTurn(0);
        session.setLog("Gặp " + enemyName);
        return battleSessionRepo.save(session);
    }

    private void addItemToInventory(Character character, Item item, int amount) {
        Optional<UserItem> existing = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), item.getItemId())
                .stream().filter(ui -> !Boolean.TRUE.equals(ui.getIsEquipped())).findFirst();

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