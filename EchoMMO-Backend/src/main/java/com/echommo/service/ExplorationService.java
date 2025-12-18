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

    // [CẤU HÌNH DROP MAP] Sử dụng ID mới (1-12)
    public enum GameMap {
        // Map 1: Drop Gỗ (1), Đá (5), Đồng (6), Cá (9)
        MAP_01("MAP_01", "Đồng Bằng", 1, 19, List.of(1, 5, 6, 9), List.of("Yêu Tinh", "Bộ Xương")),

        // Map 2: Drop Gỗ Khô (2), Sắt (7), Cá (9), Gỗ Lạnh (3)
        MAP_02("MAP_02", "Rừng Rậm", 20, 30, List.of(2, 7, 9, 3), List.of("Nấm Độc"));

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
        Character c = characterService.getMyCharacter();
        if (c == null) throw new RuntimeException("Chưa có nhân vật!");

        c = characterRepository.findByUser_UserIdWithUserAndWallet(c.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("Lỗi đồng bộ dữ liệu ví tiền!"));

        captchaService.checkLockStatus(c.getUser());

        if (c.getCurrentEnergy() < 1) throw new RuntimeException("Bạn đã kiệt sức! Hãy nghỉ ngơi.");

        GameMap map = GameMap.findById(mapId);
        if (c.getLevel() < map.minLv) throw new RuntimeException("Cấp độ không đủ! Cần Lv." + map.minLv);

        Random r = new Random();
        Wallet w = c.getUser().getWallet();

        // Trừ năng lượng & Cộng EXP/Gold
        c.setCurrentEnergy(c.getCurrentEnergy() - 1);
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
            String enemyName = map.enemies.get(r.nextInt(map.enemies.size()));
            BattleSession session = createBattleSession(c, enemyName, map);
            msg = "Nguy hiểm! Gặp " + session.getEnemyName() + "!";
            c.setStatus(CharacterStatus.IN_COMBAT);
            rewardName = session.getEnemyName();
            clearGatheringState(c);
        } else { // 9% ITEM (Trang bị ID > 12)
            type = "ITEM";
            // Chỉ rơi Item trang bị (ID >= 13)
            List<Item> items = itemRepo.findAll().stream().filter(i -> i.getItemId() >= 13).toList();
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

    @Transactional
    public Map<String, Object> gatherResource(Integer itemId, int amount) {
        Character c = characterService.getMyCharacter();

        if (c.getGatheringItemId() == null || !c.getGatheringItemId().equals(itemId)) {
            throw new RuntimeException("Tài nguyên không còn ở đây hoặc ID không khớp!");
        }
        if (c.getGatheringExpiry() != null && LocalDateTime.now().isAfter(c.getGatheringExpiry())) {
            clearGatheringState(c);
            characterRepository.save(c);
            throw new RuntimeException("Mỏ tài nguyên đã cạn kiệt (hết thời gian)!");
        }

        if (c.getGatheringRemainingAmount() < amount) amount = c.getGatheringRemainingAmount();
        if (c.getCurrentEnergy() < amount) throw new RuntimeException("Không đủ năng lượng!");

        c.setCurrentEnergy(c.getCurrentEnergy() - amount);
        c.setGatheringRemainingAmount(c.getGatheringRemainingAmount() - amount);

        Wallet wallet = c.getUser().getWallet();
        addItemToWallet(wallet, itemId, amount);
        walletRepository.save(wallet);

        if (c.getGatheringRemainingAmount() <= 0) clearGatheringState(c);
        characterRepository.save(c);

        Item item = itemRepo.findById(itemId).orElse(null);
        String itemName = (item != null) ? item.getName() : "Vật phẩm";

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Thu hoạch thành công " + amount + " " + itemName);
        res.put("currentEnergy", c.getCurrentEnergy());
        res.put("remainingAmount", c.getGatheringRemainingAmount());
        return res;
    }

    private int safeGet(Integer value) {
        return value == null ? 0 : value;
    }

    // [QUAN TRỌNG] MAP ID 1-12 VÀO ĐÚNG CỘT WALLET
    private void addItemToWallet(Wallet w, int itemId, int amount) {
        switch (itemId) {
            // GỖ
            case 1: w.setWood(safeGet(w.getWood()) + amount); break;
            case 2: w.setDriedWood(safeGet(w.getDriedWood()) + amount); break;
            case 3: w.setColdWood(safeGet(w.getColdWood()) + amount); break;
            case 4: w.setStrangeWood(safeGet(w.getStrangeWood()) + amount); break;

            // KHOÁNG SẢN
            case 5: w.setStone(safeGet(w.getStone()) + amount); break;
            case 6: w.setCopperOre(safeGet(w.getCopperOre()) + amount); break;
            case 7: w.setIronOre(safeGet(w.getIronOre()) + amount); break;
            case 8: w.setPlatinum(safeGet(w.getPlatinum()) + amount); break;

            // THỰC PHẨM
            case 9: w.setFish(safeGet(w.getFish()) + amount); break;
            case 10: w.setShark(safeGet(w.getShark()) + amount); break;

            // ĐẶC BIỆT
            case 11: w.setEchoCoin(safeGet(w.getEchoCoin()) + amount); break;
            case 12: w.setUnknownMaterial(safeGet(w.getUnknownMaterial()) + amount); break;

            default:
                System.out.println("Warning: Item ID " + itemId + " không được hỗ trợ trong Wallet (Có thể là Trang bị).");
        }
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