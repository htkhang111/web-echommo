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

    public enum GameMap {
        MAP_01("MAP_01", "Đồng Bằng", 1, 19, List.of(1, 2), List.of("Goblin", "Skeleton")),
        MAP_02("MAP_02", "Rừng Rậm", 20, 30, List.of(1, 3), List.of("Người Rừng", "Gấu Hoang"));

        public final String id; public final String name;
        public final int minLv; public final int maxLv;
        public final List<Integer> resourceIds;
        public final List<String> enemies;

        GameMap(String id, String name, int minLv, int maxLv, List<Integer> resourceIds, List<String> enemies) {
            this.id = id; this.name = name; this.minLv = minLv; this.maxLv = maxLv;
            this.resourceIds = resourceIds; this.enemies = enemies;
        }
        public static GameMap findById(String id) {
            return Arrays.stream(values()).filter(m -> m.id.equalsIgnoreCase(id)).findFirst().orElse(MAP_01);
        }
    }

    @Transactional
    public ExplorationResponse explore(String mapId) {
        // [BẢO MẬT] Load đầy đủ quan hệ User/Wallet để tránh lỗi Lazy Loading 500
        Character c = characterService.getMyCharacter();
        if (c == null) throw new RuntimeException("Chưa có nhân vật!");

        c = characterRepository.findByUser_UserIdWithUserAndWallet(c.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("Lỗi đồng bộ dữ liệu ví tiền!"));

        captchaService.checkLockStatus(c.getUser());

        // Kiểm tra năng lượng
        if (c.getCurrentEnergy() < 1) throw new RuntimeException("Bạn đã kiệt sức! Hãy nghỉ ngơi.");

        GameMap map = GameMap.findById(mapId);
        if (c.getLevel() < map.minLv) throw new RuntimeException("Cấp độ không đủ! Cần Lv." + map.minLv);

        Random r = new Random();
        Wallet w = c.getUser().getWallet();

        // 1. Trừ năng lượng & Cộng EXP/Vàng
        c.setCurrentEnergy(c.getCurrentEnergy() - 1);
        long expGain = 10L + c.getLevel();
        c.setCurrentExp(c.getCurrentExp() + expGain);
        BigDecimal coinGain = BigDecimal.valueOf(1 + r.nextInt(5));
        w.setGold(w.getGold().add(coinGain));

        // 2. Xử lý RNG nội dung hành tẩu
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
            }
        } else if (roll < 91) { // 11% ENEMY
            type = "ENEMY";
            String enemyName = map.enemies.get(r.nextInt(map.enemies.size()));
            BattleSession session = createBattleSession(c, enemyName, map);
            msg = "Nguy hiểm! Gặp " + session.getEnemyName() + "!";
            c.setStatus(CharacterStatus.IN_COMBAT);
            rewardName = session.getEnemyName();
            clearGatheringState(c);
        } else { // 9% ITEM
            type = "ITEM";
            List<Item> items = itemRepo.findAll();
            if (!items.isEmpty()) {
                Item item = items.get(r.nextInt(items.size()));
                addItemToInventory(c, item, 1);
                msg = "May mắn! Nhặt được " + item.getName();
                rewardName = item.getName();
                rewardAmount = 1;
                rewardItemId = item.getItemId();
            } else {
                type = "TEXT"; msg = "Bạn tìm thấy một chiếc rương cũ nhưng bên trong trống rỗng.";
            }
            clearGatheringState(c);
        }

        // 3. Kiểm tra lên cấp
        Integer newLevel = checkLevelUp(c);

        characterRepository.save(c);
        walletRepository.save(w);

        // [QUAN TRỌNG] Trả về Response dùng BUILDER để tránh lệch kiểu dữ liệu gây lỗi 500
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

    @Transactional
    public Map<String, Object> gatherResource(Integer itemId, int amount) {
        Character c = characterService.getMyCharacter();

        if (c.getGatheringItemId() == null || !c.getGatheringItemId().equals(itemId)) {
            throw new RuntimeException("Tài nguyên không còn ở đây!");
        }
        if (LocalDateTime.now().isAfter(c.getGatheringExpiry())) {
            clearGatheringState(c);
            characterRepository.save(c);
            throw new RuntimeException("Mỏ tài nguyên đã cạn kiệt!");
        }

        if (c.getGatheringRemainingAmount() < amount) amount = c.getGatheringRemainingAmount();
        if (c.getCurrentEnergy() < amount) throw new RuntimeException("Không đủ năng lượng!");

        c.setCurrentEnergy(c.getCurrentEnergy() - amount);
        c.setGatheringRemainingAmount(c.getGatheringRemainingAmount() - amount);

        Item item = itemRepo.findById(itemId).orElseThrow(() -> new RuntimeException("Vật phẩm lỗi"));
        addItemToInventory(c, item, amount);

        if (c.getGatheringRemainingAmount() <= 0) clearGatheringState(c);

        characterRepository.save(c);

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Thu hoạch thành công " + amount + " " + item.getName());
        res.put("currentEnergy", c.getCurrentEnergy());
        res.put("remainingAmount", c.getGatheringRemainingAmount());
        return res;
    }

    private void clearGatheringState(Character c) {
        c.setGatheringItemId(null);
        c.setGatheringRemainingAmount(0);
        c.setGatheringExpiry(null);
    }

    private Integer checkLevelUp(Character c) {
        Integer leveledUpTo = null;
        while (c.getCurrentExp() >= (long) c.getLevel() * 100L) {
            c.setCurrentExp(c.getCurrentExp() - (long) c.getLevel() * 100L);
            c.setLevel(c.getLevel() + 1);
            c.setMaxHp(c.getMaxHp() + 20);
            c.setCurrentHp(c.getMaxHp());
            c.setCurrentEnergy(c.getMaxEnergy());
            leveledUpTo = c.getLevel();
        }
        return leveledUpTo;
    }

    private BattleSession createBattleSession(Character player, String enemyName, GameMap map) {
        BattleSession session = battleSessionRepo.findByCharacter_CharId(player.getCharId())
                .orElse(new BattleSession());

        session.setCharacter(player);
        session.setEnemyName(enemyName);
        session.setEnemyMaxHp(100 + (map.minLv * 10));
        session.setEnemyCurrentHp(session.getEnemyMaxHp());
        session.setEnemyAtk(10 + map.minLv);
        session.setEnemyDef(map.minLv);
        session.setEnemySpeed(10);
        session.setCurrentTurn(0);
        session.setLog("Bạn đụng độ " + enemyName);
        return battleSessionRepo.save(session);
    }

    private void addItemToInventory(Character character, Item item, int amount) {
        Optional<UserItem> existing = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), item.getItemId())
                .stream()
                .filter(ui -> !Boolean.TRUE.equals(ui.getIsEquipped()))
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