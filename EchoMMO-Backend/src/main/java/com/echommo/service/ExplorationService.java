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
        Character c = characterRepository.findByUser_UserIdWithUserAndWallet(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Chưa có nhân vật!"));

        captchaService.checkLockStatus(c.getUser());
        if (c.getCurrentEnergy() < 1) throw new RuntimeException("Hết năng lượng!");

        GameMap map = GameMap.findById(mapId);
        if (c.getLevel() < map.minLv) throw new RuntimeException("Cấp độ không đủ! Cần level " + map.minLv);

        Random r = new Random();
        Wallet w = c.getUser().getWallet();

        c.setCurrentEnergy(c.getCurrentEnergy() - 1);
        c.setCurrentExp(c.getCurrentExp() + 10L + c.getLevel());

        int roll = r.nextInt(100);
        String type; String msg;
        String rewardName = null; Integer rewardAmount = 0; Integer rewardItemId = null;

        if (roll < 60) {
            type = "TEXT"; msg = flavorTextRepo.findRandomContent().orElse("Bạn đi dạo quanh map.");
            clearGatheringState(c);
        } else if (roll < 85) { // RESOURCE
            type = "GATHERING";
            String resCode = map.resourceCodes.get(r.nextInt(map.resourceCodes.size()));

            if ("GOLD_MINE_SPECIAL".equals(resCode)) {
                BigDecimal gold = BigDecimal.valueOf(50 + r.nextInt(100));
                w.setGold(w.getGold().add(gold));
                msg = "Tìm thấy mỏ vàng! (+" + gold + " Vàng)";
                type = "GOLD_MINE"; clearGatheringState(c);
            } else {
                Item item = itemRepo.findByCode(resCode).orElse(null);
                if (item != null) {
                    if (map == GameMap.MAP_06 && r.nextInt(100) < 10) {
                        double echoFrac = 0.001 + (0.009 * r.nextDouble());
                        w.setEchoCoin(w.getEchoCoin().add(BigDecimal.valueOf(echoFrac)));
                    }

                    int amount = 5 + r.nextInt(16);
                    c.setGatheringItemId(item.getItemId());
                    c.setGatheringRemainingAmount(amount);
                    c.setGatheringExpiry(LocalDateTime.now().plusMinutes(3));

                    msg = "Phát hiện " + item.getName();
                    rewardName = item.getName(); rewardAmount = amount; rewardItemId = item.getItemId();
                } else {
                    type = "TEXT"; msg = "Không thấy gì.";
                    clearGatheringState(c);
                }
            }
        } else {
            type = "TEXT"; msg = "Một cơn gió thoảng qua.";
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
    public Map<String, Object> gatherResource(User user, int itemId, int amount) {
        Character c = characterRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        if (c.getGatheringItemId() == null || c.getGatheringItemId() != itemId) {
            throw new RuntimeException("Mỏ tài nguyên đã biến mất hoặc không hợp lệ!");
        }

        if (c.getGatheringExpiry() != null && LocalDateTime.now().isAfter(c.getGatheringExpiry())) {
            clearGatheringState(c);
            characterRepository.save(c);
            throw new RuntimeException("Mỏ tài nguyên đã hết hạn!");
        }

        if (c.getGatheringRemainingAmount() < amount) {
            throw new RuntimeException("Không đủ tài nguyên để thu hoạch!");
        }

        Item item = itemRepo.findById(itemId).orElseThrow(() -> new RuntimeException("Item lỗi"));
        addItemToInventory(c, item, amount);

        c.setGatheringRemainingAmount(c.getGatheringRemainingAmount() - amount);
        if (c.getGatheringRemainingAmount() <= 0) {
            clearGatheringState(c);
        }
        characterRepository.save(c);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Thu hoạch thành công " + amount + "x " + item.getName());
        resp.put("remaining", c.getGatheringRemainingAmount());
        return resp;
    }

    private void addItemToInventory(Character character, Item item, int amount) {
        if (List.of("MATERIAL", "CONSUMABLE").contains(item.getType())) {
            Optional<UserItem> exist = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), item.getItemId())
                    .stream().filter(ui -> !ui.getIsEquipped()).findFirst();
            if (exist.isPresent()) {
                exist.get().setQuantity(exist.get().getQuantity() + amount);
                userItemRepo.save(exist.get());
                return;
            }
        }

        UserItem ui = new UserItem();
        ui.setCharacter(character); ui.setItem(item);
        ui.setQuantity(amount); ui.setIsEquipped(false);
        // [FIX] Gọi setter mới
        ui.setEnhanceLevel(0);
        ui.setAcquiredAt(LocalDateTime.now());
        ui.setMainStatValue(BigDecimal.valueOf(10));

        if (List.of("WEAPON", "ARMOR").contains(item.getType())) {
            itemGenService.randomizeNewItem(ui);
        }
        userItemRepo.save(ui);
    }

    private void clearGatheringState(Character c) {
        c.setGatheringItemId(null); c.setGatheringRemainingAmount(0); c.setGatheringExpiry(null);
    }

    private void checkLevelUp(Character c) {
        long req = c.getLevel() * 100L;
        if (c.getCurrentExp() >= req) {
            c.setLevel(c.getLevel() + 1);
            c.setCurrentExp(c.getCurrentExp() - req);
            c.setStatPoints(c.getStatPoints() + 5);
            c.setMaxHp(c.getMaxHp() + 20);
            c.setCurrentHp(c.getMaxHp());
        }
    }
}