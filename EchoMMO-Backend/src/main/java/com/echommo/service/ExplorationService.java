package com.echommo.service;

import com.echommo.dto.ExplorationResponse;
import com.echommo.entity.*;
import com.echommo.entity.Character;
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
    private final EnemyRepository enemyRepository; // Inject thêm để xử lý Enemy

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
        // 1. Anti-spam check
        long now = System.currentTimeMillis();
        if (now - lastActionMap.getOrDefault(user.getUserId(), 0L) < 500) {
            throw new RuntimeException("Thao tác quá nhanh!");
        }
        lastActionMap.put(user.getUserId(), now);

        Character c = characterRepository.findByUser_UserIdWithUserAndWallet(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Chưa có nhân vật!"));

        captchaService.checkLockStatus(c.getUser());

        // Logic cũ: Tốn 1 Energy
        if (c.getCurrentEnergy() < 1) throw new RuntimeException("Hết năng lượng! Hãy vào Spa nghỉ ngơi.");
        c.setCurrentEnergy(c.getCurrentEnergy() - 1);

        GameMap map = GameMap.findById(mapId);
        if (c.getLevel() < map.minLv) throw new RuntimeException("Cấp độ không đủ! Cần level " + map.minLv);

        Random r = new Random();
        Wallet w = c.getUser().getWallet();
        c.setCurrentExp(c.getCurrentExp() + 10L + c.getLevel()); // Exp hành tẩu

        int roll = r.nextInt(100); // 0 - 99
        String type; String msg;
        String rewardName = null; Integer rewardAmount = 0; Integer rewardItemId = null;

        // --- [LOGIC TỈ LỆ MỚI: 70 / 11 / 10 / 9] ---

        if (roll < 70) {
            // 70% TEXT
            type = "TEXT";
            msg = flavorTextRepo.findRandomContent().orElse("Bạn đi dạo quanh " + map.name + ".");
            clearGatheringState(c);

        } else if (roll < 81) {
            // 11% MATERIALS (GATHERING)
            type = "GATHERING";
            String resCode = map.resourceCodes.get(r.nextInt(map.resourceCodes.size()));

            if ("GOLD_MINE_SPECIAL".equals(resCode)) {
                // Map 3 Special
                BigDecimal gold = BigDecimal.valueOf(50 + r.nextInt(150));
                w.setGold(w.getGold().add(gold));
                msg = "Tìm thấy kho báu! (+" + gold + " Vàng)";
                type = "GOLD_MINE";
                clearGatheringState(c);
            } else {
                Item item = itemRepo.findByCode(resCode).orElse(null);
                if (item != null) {
                    // Trữ lượng mỏ random 10-30
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
            // 10% ENEMY (Battle)
            type = "ENEMY";
            String enemyName = map.enemies.get(r.nextInt(map.enemies.size()));
            msg = "Đụng độ " + enemyName + "!";
            // Frontend sẽ check type ENEMY để chuyển cảnh
            clearGatheringState(c);

        } else {
            // 9% ITEM LOOT (Nhặt luôn)
            type = "ITEM";
            // Map 6 có tỉ lệ 5% (của 9%) ra Echo Coin
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

        checkLevelUp(c);
        characterRepository.save(c);
        walletRepository.save(w);

        return ExplorationResponse.builder()
                .message(msg).type(type)
                .currentLv(c.getLevel()).currentExp(c.getCurrentExp())
                .currentEnergy(c.getCurrentEnergy()).maxEnergy(c.getMaxEnergy())
                .rewardName(rewardName).rewardAmount(rewardAmount).rewardItemId(rewardItemId)
                .build();
    }

    @Transactional
    public Map<String, Object> gatherResource(User user, int itemId, int amountRequest) {
        Character c = characterRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        // 1. Validate Mỏ
        if (c.getGatheringItemId() == null || c.getGatheringItemId() != itemId)
            throw new RuntimeException("Mỏ tài nguyên không hợp lệ!");
        if (c.getGatheringExpiry() != null && LocalDateTime.now().isAfter(c.getGatheringExpiry())) {
            clearGatheringState(c); characterRepository.save(c);
            throw new RuntimeException("Mỏ đã sập!");
        }
        if (c.getGatheringRemainingAmount() < amountRequest)
            throw new RuntimeException("Trữ lượng không đủ!");

        // 2. Logic Hái Nhanh vs Hái Thường
        int energyCost;
        int expGain;
        int actualAmount = amountRequest;

        if (amountRequest >= 10) {
            // Hái Nhanh (Combo 10): Phí tiện lợi 12 Energy
            energyCost = 12;
            expGain = 100; // Exp Nghề
            actualAmount = 10;
        } else {
            // Hái Thường: 1 Energy/item
            energyCost = amountRequest;
            expGain = amountRequest * 10;
        }

        // 3. Check Energy
        if (c.getCurrentEnergy() < energyCost)
            throw new RuntimeException("Thiếu năng lượng! Cần " + energyCost + " / " + c.getCurrentEnergy());

        // 4. Check Cấp Nghề (Gating)
        Item item = itemRepo.findById(itemId).orElseThrow();
        int requiredJobLv = getRequiredJobLevel(item);
        if (c.getGatheringLevel() < requiredJobLv) {
            throw new RuntimeException("Cấp nghề thấp! Cần Lv " + requiredJobLv + " để khai thác.");
        }

        // 5. Execute
        c.setCurrentEnergy(c.getCurrentEnergy() - energyCost);
        c.setGatheringRemainingAmount(c.getGatheringRemainingAmount() - actualAmount);

        // Cộng Exp Nghề
        addGatheringExp(c, expGain);

        // Thêm đồ vào kho (có check slot)
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
        // Công thức: Lv * 150 + Lv^2 * 10
        long req = c.getGatheringLevel() * 150L + (long)Math.pow(c.getGatheringLevel(), 2) * 10;
        if (c.getGatheringExp() >= req) {
            c.setGatheringLevel(c.getGatheringLevel() + 1);
            c.setGatheringExp(c.getGatheringExp() - req);
            // Có thể thêm thông báo level up nghề ở đây nếu cần
        }
    }

    private int getRequiredJobLevel(Item item) {
        // Gating đơn giản dựa trên Rarity
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

    private void checkLevelUp(Character c) {
        long required = c.getLevel() < 60
                ? c.getLevel() * 100L
                : c.getLevel() * 100L + (long)Math.pow(c.getLevel() - 60, 2) * 200;

        if (c.getCurrentExp() >= required) {
            c.setLevel(c.getLevel() + 1);
            c.setCurrentExp(c.getCurrentExp() - required);
            c.setStatPoints(c.getStatPoints() + 5);
            c.setMaxHp(c.getMaxHp() + 20);
            c.setCurrentHp(c.getMaxHp());
            c.setMaxEnergy(c.getMaxEnergy() + 1); // Tăng 1 Max Energy mỗi cấp
        }
    }

    private void addItemToInventory(Character character, Item item, int amount) {
        // [QUAN TRỌNG] Check Inventory Slots
        int currentSlots = userItemRepo.countByCharacter_CharId(character.getCharId());
        // Lấy giới hạn kho từ User (mặc định 50 nếu null)
        int maxSlots = character.getUser().getInventorySlots() != null ? character.getUser().getInventorySlots() : 50;

        if (List.of("MATERIAL", "CONSUMABLE").contains(item.getType())) {
            // Logic Stack: Nếu đã có thì cộng dồn -> Không tốn slot mới
            Optional<UserItem> exist = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), item.getItemId())
                    .stream().filter(ui -> !ui.getIsEquipped()).findFirst();
            if (exist.isPresent()) {
                exist.get().setQuantity(exist.get().getQuantity() + amount);
                userItemRepo.save(exist.get());
                return;
            }
        }

        // Nếu phải tạo ô mới -> Check Full
        if (currentSlots >= maxSlots) {
            throw new RuntimeException("Kho đồ đã đầy (" + currentSlots + "/" + maxSlots + ")! Hãy mở rộng kho.");
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