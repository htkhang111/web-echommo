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
        MAP_01("MAP_01", "Đồng Bằng", 1, 19, Map.of("Gỗ", 40, "Đá", 30, "Quặng Đồng", 20, "Cá", 10), List.of("Goblin", "Skeleton", "Mushroom")),
        MAP_02("MAP_02", "Rừng Rậm", 20, 30, Map.of("Gỗ", 30, "Đá", 20, "Quặng Đồng", 20, "Sắt", 15, "Cá", 15), List.of("Người Rừng", "Gấu Hoang", "Nhện Rừng")),
        MAP_03("MAP_03", "Sa Mạc", 30, 40, Map.of("Gỗ Khô", 20, "Đá", 30, "Quặng Đồng", 25, "Sắt", 25), List.of("Bọ Cát", "Xác Ướp Lang Thang", "Bọ Hung Khổng Lồ")),
        MAP_04("MAP_04", "Núi Cao", 40, 50, Map.of("Gỗ", 15, "Đá", 25, "Quặng Đồng", 20, "Sắt", 25, "Bạch Kim", 15), List.of("Golem Đá", "Đại Bàng Núi", "Người Lùn Đào Mỏ")),
        MAP_05("MAP_05", "Băng Đảo", 50, 60, Map.of("Gỗ Lạnh", 10, "Sắt", 35, "Bạch Kim", 35, "Nguyên liệu lạ", 20), List.of("Gấu Băng", "Người Tuyết", "Tinh Linh Băng")),
        MAP_06("MAP_06", "Đầm Lầy", 60, 70, Map.of("Gỗ Hóa Thạch", 30, "Cá Độc", 20, "Kim Cương", 10, "Nguyên liệu lạ", 40), List.of("Quái Đầm Lầy", "Rắn Độc Khổng Lồ", "Linh Hồn Sa Lầy"));

        public final String id; public final String name; public final int minLv; public final int maxLv;
        public final Map<String, Integer> resources; public final List<String> enemies;
        GameMap(String id, String name, int minLv, int maxLv, Map<String, Integer> resources, List<String> enemies) {
            this.id = id; this.name = name; this.minLv = minLv; this.maxLv = maxLv; this.resources = resources; this.enemies = enemies;
        }
        public static GameMap findById(String id) { return Arrays.stream(values()).filter(m -> m.id.equalsIgnoreCase(id)).findFirst().orElse(MAP_01); }
    }

    @Transactional
    public ExplorationResponse explore(String mapId) {
        try {
            Character c = characterService.getMyCharacter();
            if (c == null) {
                User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow();
                c = characterService.createDefaultCharacter(user);
            }
            captchaService.checkLockStatus(c.getUser());
            GameMap map = GameMap.findById(mapId);
            if (c.getLevel() < map.minLv) throw new RuntimeException("Cấp độ không đủ! Cần Lv." + map.minLv);

            Random r = new Random();
            Wallet w = c.getUser().getWallet();
            int baseExp = 10 + c.getLevel();
            int baseCoin = 1 + r.nextInt(5) + (map.minLv / 5);
            c.setCurrentExp(c.getCurrentExp() + baseExp);
            w.setGold((w.getGold() != null ? w.getGold() : BigDecimal.ZERO).add(BigDecimal.valueOf(baseCoin)));

            // [FIXED] Tỷ lệ chuẩn 60/20/11/9 (Xử lý toàn bộ tại Backend)
            int roll = r.nextInt(100);
            String type; String msg; String rewardName = null; Integer rewardAmount = 0;

            if (roll < 60) { // 60% TEXT
                type = "TEXT";
                msg = r.nextBoolean() ? weatherTextRepo.findRandomContent().orElse("Thời tiết lạ.") : flavorTextRepo.findRandomContent().orElse("Cảnh đẹp.");

            } else if (roll < 80) { // 20% GATHERING (Resource)
                // Trả về type GATHERING để Frontend biết đường chuyển trang
                type = "GATHERING";
                msg = "Phát hiện khu vực tài nguyên!";
                // Không cộng item ở đây, chuyển sang Gathering.vue xử lý

            } else if (roll < 91) { // 11% ENEMY
                type = "ENEMY";
                String enemyNameBase = map.enemies.get(r.nextInt(map.enemies.size()));
                BattleSession session = createBattleSession(c, enemyNameBase, map);
                msg = "Sát khí! " + session.getEnemyName() + " xuất hiện!";
                c.setStatus(CharacterStatus.IN_COMBAT);
                rewardName = session.getEnemyName();

            } else { // 9% EQUIPMENT (ITEM)
                type = "ITEM";
                List<Item> equips = itemRepo.findByType("WEAPON");
                Item equip = equips.isEmpty() ? null : equips.get(r.nextInt(equips.size()));
                if (equip != null) { addItemToInventory(c.getUser(), equip, 1); msg = "Nhặt được: " + equip.getName(); rewardName = equip.getName(); rewardAmount = 1; }
                else { type = "TEXT"; msg = "Rương rỗng."; }
            }

            Integer newLv = checkLevelUp(c);
            characterRepository.save(c); walletRepository.save(w);
            return new ExplorationResponse(msg, type, BigDecimal.valueOf(baseCoin), c.getCurrentExp(), c.getLevel(), c.getCurrentEnergy(), c.getMaxEnergy(), newLv, rewardName, rewardAmount);
        } catch (Exception e) { throw new RuntimeException(e.getMessage()); }
    }

    private BattleSession createBattleSession(Character player, String enemyName, GameMap map) {
        Random r = new Random();
        int baseHp = 50; int baseAtk = 8; int baseDef = 2; // Giả lập base stat
        int enemyLv = r.nextInt(map.maxLv - map.minLv + 1) + map.minLv;
        double multiplier = 1.0 + (enemyLv * 0.1);
        boolean isElite = r.nextInt(100) < 5;
        if (isElite) multiplier *= 1.5;

        // Sửa getCharacterId() -> getCharId()
        BattleSession session = battleSessionRepo.findByCharacter_CharId(player.getCharId()).orElse(new BattleSession());
        session.setCharacter(player);
        session.setEnemyName((isElite ? "[Tinh Anh] " : "") + enemyName);
        session.setEnemyMaxHp((int) (baseHp * multiplier));
        session.setEnemyCurrentHp((int) (baseHp * multiplier));
        session.setTurnCount(0);
        session.setLog("Bạn gặp " + session.getEnemyName());
        return battleSessionRepo.save(session);
    }

    private Integer checkLevelUp(Character c) {
        long reqExp = (c.getLevel() < 60) ? (long) c.getLevel() * 50L : (long) c.getLevel() * 100L + (long) Math.pow(c.getLevel() - 60, 2) * 200L;
        if (c.getCurrentExp() >= reqExp) {
            c.setCurrentExp(c.getCurrentExp() - (int)reqExp);
            c.setLevel(c.getLevel() + 1); c.setMaxHp(c.getMaxHp() + 20); c.setCurrentHp(c.getMaxHp()); c.setCurrentEnergy(c.getMaxEnergy());
            return c.getLevel();
        }
        return null;
    }

    private void addItemToInventory(User user, Item item, int amount) {
        UserItem userItem = userItemRepo.findByUser_UserId(user.getUserId()).stream().filter(ui -> ui.getItem().getItemId().equals(item.getItemId())).findFirst().orElse(new UserItem());
        if (userItem.getUserItemId() == null) { userItem.setUser(user); userItem.setItem(item); userItem.setQuantity(amount); userItem.setIsEquipped(false); userItem.setEnhanceLevel(0); }
        else { userItem.setQuantity(userItem.getQuantity() + amount); }
        userItemRepo.save(userItem);
    }

    // Hàm này giữ nguyên để phục vụ trang Gathering gọi vào
    @Transactional
    public Map<String, Object> gatherResource(String resourceType, int amount) {
        Character c = characterService.getMyCharacter();
        if (c == null) throw new RuntimeException("Chưa có nhân vật");

        if (c.getCurrentEnergy() < amount) throw new RuntimeException("Không đủ nội năng! Cần " + amount);

        c.setCurrentEnergy(c.getCurrentEnergy() - amount);
        characterRepository.save(c);

        String itemName;
        switch (resourceType) {
            case "wood": itemName = "Gỗ"; break;
            case "stone": itemName = "Đá"; break;
            case "mining": itemName = "Quặng Đồng"; break;
            case "special": itemName = "Gỗ Hóa Thạch"; break;
            default: itemName = "Gỗ"; break;
        }

        Item item = itemRepo.findByName(itemName).orElse(null);
        if (item != null) { addItemToInventory(c.getUser(), item, amount); }

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Thu hoạch " + amount + " " + itemName);
        result.put("currentEnergy", c.getCurrentEnergy());
        result.put("wood", 0);
        result.put("stone", 0);
        return result;
    }
}