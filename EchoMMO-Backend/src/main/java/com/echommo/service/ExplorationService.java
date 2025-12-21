package com.echommo.service;

import com.echommo.dto.ExplorationResponse;
import com.echommo.entity.*;
import com.echommo.entity.Character;
import com.echommo.enums.CharacterStatus; // [FIX] Đã thêm dòng này
import com.echommo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExplorationService {

    private final CharacterRepository characterRepository;
    private final WalletRepository walletRepository;
    private final CaptchaService captchaService;
    private final ItemRepository itemRepo;
    private final UserItemRepository userItemRepo;
    private final FlavorTextRepository flavorTextRepo;
    private final ItemGenerationService itemGenService;
    private final EnemyRepository enemyRepository;
    private final BattleSessionRepository battleSessionRepo;
    private final CharacterService characterService;

    // Map chống Spam Click (500ms)
    private final Map<Integer, Long> lastActionMap = new HashMap<>();

    public enum GameMap {
        MAP_01("MAP_01", "Đồng Bằng", 1, 19,
                createWeightedList(Map.of("w_wood", 40, "o_coal", 30, "o_copper", 20, "f_fish", 10)),
                List.of("Slime", "Sói")),
        MAP_02("MAP_02", "Rừng Rậm", 20, 29,
                createWeightedList(Map.of("w_wood", 30, "o_copper", 20, "o_iron", 20, "f_fish", 30)),
                List.of("Gấu", "Nhện")),
        MAP_03("MAP_03", "Sa Mạc", 30, 39,
                createWeightedList(Map.of("w_woodRed", 30, "GOLD_MINE_SPECIAL", 40, "o_iron", 30)),
                List.of("Bọ Cạp", "Rắn")),
        MAP_04("MAP_04", "Núi Cao", 40, 49,
                createWeightedList(Map.of("o_coal", 30, "o_iron", 30, "o_platinum", 20, "w_woodWhite", 20)),
                List.of("Golem", "Rồng Đá")),
        MAP_05("MAP_05", "Băng Đảo", 50, 59,
                createWeightedList(Map.of("w_woodWhite", 40, "o_platinum", 40, "f_whiteshark", 20)),
                List.of("Yeti", "Sói Tuyết")),
        MAP_06("MAP_06", "Vùng Đất Chết", 60, 70,
                createWeightedList(Map.of("w_woodBlack", 30, "o_strange", 30, "f_megalodon", 20, "r_coinEcho", 20)),
                List.of("Bóng Ma", "Lich King"));

        public final String id; public final String name;
        public final int minLv; public final int maxLv;
        public final List<String> resourceCodes;
        public final List<String> enemies;

        GameMap(String id, String name, int minLv, int maxLv, List<String> resourceCodes, List<String> enemies) {
            this.id = id; this.name = name; this.minLv = minLv; this.maxLv = maxLv;
            this.resourceCodes = resourceCodes; this.enemies = enemies;
        }

        private static List<String> createWeightedList(Map<String, Integer> weights) {
            List<String> list = new ArrayList<>();
            weights.forEach((code, count) -> { for(int i=0; i<count; i++) list.add(code); });
            return list;
        }
        public static GameMap findById(String id) {
            return Arrays.stream(values()).filter(m -> m.id.equalsIgnoreCase(id)).findFirst().orElse(MAP_01);
        }
    }

    @Transactional
    public ExplorationResponse explore(User user, String mapId) {
        long now = System.currentTimeMillis();
        if (now - lastActionMap.getOrDefault(user.getUserId(), 0L) < 500) {
            throw new RuntimeException("Thao tác quá nhanh!");
        }
        lastActionMap.put(user.getUserId(), now);

        Character c = characterRepository.findByUser_UserIdWithUserAndWallet(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Chưa có nhân vật!"));

        captchaService.checkLockStatus(c.getUser());

        if (c.getCurrentEnergy() < 1) throw new RuntimeException("Hết năng lượng! Hãy vào Spa nghỉ ngơi.");
        c.setCurrentEnergy(c.getCurrentEnergy() - 1);

        GameMap map = GameMap.findById(mapId);
        if (c.getLevel() < map.minLv) throw new RuntimeException("Cấp độ không đủ! Cần level " + map.minLv);

        Random r = new Random();
        Wallet w = c.getUser().getWallet();

        c.setCurrentExp(c.getCurrentExp() + 10L + c.getLevel());

        int roll = r.nextInt(100);
        String type; String msg;
        String rewardName = null; Integer rewardAmount = 0; Integer rewardItemId = null;

        if (roll < 70) {
            type = "TEXT";
            msg = flavorTextRepo.findRandomContent().orElse("Bạn đi dạo quanh " + map.name + ".");
            clearGatheringState(c);
        } else if (roll < 81) {
            type = "GATHERING";
            String resCode = map.resourceCodes.get(r.nextInt(map.resourceCodes.size()));

            if ("GOLD_MINE_SPECIAL".equals(resCode)) {
                BigDecimal gold = BigDecimal.valueOf(50 + r.nextInt(150));
                w.setGold(w.getGold().add(gold));
                msg = "Tìm thấy kho báu! (+" + gold + " Vàng)";
                type = "GOLD_MINE";
                clearGatheringState(c);
            } else {
                Item item = itemRepo.findByCode(resCode).orElse(null);
                if (item != null) {
                    int amount = 10 + r.nextInt(21);
                    c.setGatheringItemId(item.getItemId());
                    c.setGatheringRemainingAmount(amount);
                    c.setGatheringExpiry(LocalDateTime.now().plusMinutes(3));

                    msg = "Phát hiện bãi " + item.getName();
                    rewardName = item.getName();
                    rewardAmount = amount;
                    rewardItemId = item.getItemId();
                } else {
                    type = "TEXT"; msg = "Chỉ là ảo ảnh."; clearGatheringState(c);
                }
            }
        } else if (roll < 91) {
            type = "ENEMY";
            String enemyName = map.enemies.get(r.nextInt(map.enemies.size()));

            Enemy baseEnemy = enemyRepository.findByName(enemyName)
                    .orElseGet(() -> enemyRepository.findAll().stream().findFirst().orElseThrow());

            createScaledBattleSession(c, baseEnemy);

            msg = "Đụng độ " + enemyName + " (Lv." + baseEnemy.getLevel() + ")!";
            clearGatheringState(c);
        } else {
            type = "ITEM";
            if (map == GameMap.MAP_06 && r.nextInt(100) < 5) {
                w.setEchoCoin(w.getEchoCoin().add(BigDecimal.ONE));
                msg = "May mắn nhặt được 1 Echo Coin!";
            } else {
                String code = map.resourceCodes.get(r.nextInt(map.resourceCodes.size()));
                if (!"GOLD_MINE_SPECIAL".equals(code)) {
                    Item it = itemRepo.findByCode(code).orElse(null);
                    if (it != null) {
                        addItemToInventory(c, it, 1);
                        msg = "Nhặt được 1 " + it.getName();
                        rewardName = it.getName(); rewardAmount = 1;
                    } else {
                        msg = "Nhặt được hòn đá cuội.";
                    }
                } else {
                    w.setGold(w.getGold().add(BigDecimal.valueOf(20)));
                    msg = "Nhặt được túi vàng nhỏ (+20 Vàng).";
                }
            }
            clearGatheringState(c);
        }

        checkExploreLevelUp(c);

        characterRepository.save(c);
        walletRepository.save(w);

        return ExplorationResponse.builder()
                .message(msg).type(type)
                .currentLv(c.getLevel()).currentExp(c.getCurrentExp())
                .currentEnergy(c.getCurrentEnergy()).maxEnergy(c.getMaxEnergy())
                .rewardName(rewardName).rewardAmount(rewardAmount).rewardItemId(rewardItemId)
                .build();
    }

    private void createScaledBattleSession(Character player, Enemy enemy) {
        List<BattleSession> oldSessions = battleSessionRepo.findByCharacter_CharId(player.getCharId());
        battleSessionRepo.deleteAll(oldSessions);

        int lvl = enemy.getLevel() != null ? enemy.getLevel() : 1;

        int scaledHp = (int) (enemy.getHp() * (1 + lvl * 0.15));
        int scaledAtk = (int) (enemy.getAtk() * (1 + lvl * 0.10));
        int scaledDef = (int) (enemy.getDef() * (1 + lvl * 0.05));

        BattleSession session = new BattleSession();
        session.setCharacter(player);
        session.setEnemyId(enemy.getEnemyId());
        session.setEnemyName(enemy.getName());
        session.setEnemyMaxHp(scaledHp);
        session.setEnemyCurrentHp(scaledHp);
        session.setEnemyAtk(scaledAtk);
        session.setEnemyDef(scaledDef);

        session.setPlayerMaxHp(player.getMaxHp());
        session.setPlayerCurrentHp(player.getCurrentHp());

        session.setCurrentTurn(0);
        session.setCreatedAt(LocalDateTime.now()); // Field này đã fix ở BattleSession

        battleSessionRepo.save(session);

        // [FIX] Enum CharacterStatus đã được import
        player.setStatus(CharacterStatus.IN_COMBAT);
    }

    @Transactional
    public Map<String, Object> gatherResource(User user, int itemId, int amountRequest) {
        Character c = characterRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        if (c.getGatheringItemId() == null || c.getGatheringItemId() != itemId)
            throw new RuntimeException("Mỏ tài nguyên không hợp lệ!");
        if (c.getGatheringExpiry() != null && LocalDateTime.now().isAfter(c.getGatheringExpiry())) {
            clearGatheringState(c); characterRepository.save(c);
            throw new RuntimeException("Mỏ đã sập!");
        }
        if (c.getGatheringRemainingAmount() < amountRequest)
            throw new RuntimeException("Trữ lượng không đủ!");

        int energyCost = (amountRequest >= 10) ? 12 : amountRequest;
        int expGain = (amountRequest >= 10) ? 100 : amountRequest * 10;
        int actualAmount = (amountRequest >= 10) ? 10 : amountRequest;

        if (c.getCurrentEnergy() < energyCost)
            throw new RuntimeException("Thiếu năng lượng! Cần " + energyCost);

        Item item = itemRepo.findById(itemId).orElseThrow();
        int requiredJobLv = getRequiredJobLevel(item);
        if (c.getGatheringLevel() < requiredJobLv) {
            throw new RuntimeException("Cấp nghề thấp! Cần Lv " + requiredJobLv);
        }

        c.setCurrentEnergy(c.getCurrentEnergy() - energyCost);
        c.setGatheringRemainingAmount(c.getGatheringRemainingAmount() - actualAmount);

        addGatheringExp(c, expGain);
        addItemToInventory(c, item, actualAmount);

        if (c.getGatheringRemainingAmount() <= 0) clearGatheringState(c);
        characterRepository.save(c);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Thu hoạch " + actualAmount + "x " + item.getName() + " (-" + energyCost + " Energy)");
        resp.put("remaining", c.getGatheringRemainingAmount());
        resp.put("jobExp", expGain);
        return resp;
    }

    private void addGatheringExp(Character c, int amount) {
        c.setGatheringExp(c.getGatheringExp() + amount);
        long req = c.getGatheringLevel() * 150L + (long)Math.pow(c.getGatheringLevel(), 2) * 10;
        if (c.getGatheringExp() >= req) {
            c.setGatheringLevel(c.getGatheringLevel() + 1);
            c.setGatheringExp(c.getGatheringExp() - req);
        }
    }

    private int getRequiredJobLevel(Item item) {
        switch (item.getRarity()) {
            case COMMON: return 1;
            case UNCOMMON: return 10;
            case RARE: return 20;
            case EPIC: return 30;
            case LEGENDARY: return 50;
            default: return 1;
        }
    }

    private void clearGatheringState(Character c) {
        c.setGatheringItemId(null);
        c.setGatheringRemainingAmount(0);
        c.setGatheringExpiry(null);
    }

    private void checkExploreLevelUp(Character c) {
        long required = c.getLevel() * 100L;
        if (c.getCurrentExp() >= required) {
            c.setLevel(c.getLevel() + 1);
            c.setCurrentExp(c.getCurrentExp() - required);
            characterService.recalculateStats(c);
            c.setCurrentHp(c.getMaxHp());
            c.setMaxEnergy(c.getMaxEnergy() + 1);
        }
    }

    private void addItemToInventory(Character character, Item item, int amount) {
        int currentSlots = userItemRepo.countByCharacter_CharId(character.getCharId());
        int maxSlots = character.getUser().getInventorySlots() != null ? character.getUser().getInventorySlots() : 50;

        if (List.of("MATERIAL", "CONSUMABLE").contains(item.getType())) {
            Optional<UserItem> exist = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), item.getItemId())
                    .stream().filter(ui -> !ui.getIsEquipped()).findFirst();
            if (exist.isPresent()) {
                exist.get().setQuantity(exist.get().getQuantity() + amount);
                userItemRepo.save(exist.get());
                return;
            }
        }

        if (currentSlots >= maxSlots) {
            throw new RuntimeException("Kho đồ đã đầy (" + currentSlots + "/" + maxSlots + ")!");
        }

        UserItem ui = new UserItem();
        ui.setCharacter(character); ui.setItem(item);
        ui.setQuantity(amount); ui.setIsEquipped(false);
        ui.setEnhanceLevel(0);
        ui.setMythicStars(0);
        ui.setAcquiredAt(LocalDateTime.now());
        ui.setMainStatValue(BigDecimal.valueOf(item.getBaseMainStat() != null ? item.getBaseMainStat() : 0));

        if (List.of("WEAPON", "ARMOR").contains(item.getType())) {
            itemGenService.randomizeNewItem(ui);
        }
        userItemRepo.save(ui);
    }
}