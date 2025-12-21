package com.echommo.service;

import com.echommo.config.GameConstants;
import com.echommo.dto.ExplorationResponse;
import com.echommo.entity.*;
import com.echommo.entity.Character;
import com.echommo.enums.CharacterStatus;
import com.echommo.enums.Rarity;
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

    private final CharacterService characterService;
    private final CharacterRepository characterRepository;
    private final WalletRepository walletRepository;
    private final CaptchaService captchaService;
    private final ItemRepository itemRepo;
    private final UserItemRepository userItemRepo;
    private final FlavorTextRepository flavorTextRepo;
    private final BattleSessionRepository battleSessionRepo;
    private final ItemGenerationService itemGenService; // Inject service mới

    // --- CẤU HÌNH MAP (ID MỚI) ---
    public enum GameMap {
        MAP_01("MAP_01", "Đồng Bằng", 1, 19,
                createWeightedList(Map.of(1, 40, 5, 30, 6, 20, 9, 10)), // Gỗ, Đá, Đồng, Cá
                List.of("Slime", "Sói")),

        MAP_02("MAP_02", "Rừng Rậm", 20, 30,
                createWeightedList(Map.of(1, 30, 6, 20, 7, 20, 9, 30)), // Gỗ, Đồng, Sắt, Cá
                List.of("Gấu", "Nhện")),

        MAP_03("MAP_03", "Sa Mạc", 30, 40,
                createWeightedList(Map.of(2, 30, 999, 40, 7, 30)), // 999 là Code Quặng Vàng
                List.of("Bọ Cạp", "Rắn")),

        MAP_04("MAP_04", "Núi Cao", 40, 50,
                createWeightedList(Map.of(5, 30, 7, 30, 8, 20, 3, 20)), // Đá, Sắt, Bạch Kim, Gỗ Lạnh
                List.of("Golem", "Rồng Đá")),

        MAP_05("MAP_05", "Băng Đảo", 50, 60,
                createWeightedList(Map.of(3, 40, 8, 40, 13, 20)), // Gỗ Lạnh, Bạch Kim, Cá Mập Trắng
                List.of("Yeti", "Sói Tuyết")),

        MAP_06("MAP_06", "Vùng Đất Chết", 60, 70,
                createWeightedList(Map.of(4, 30, 12, 30, 14, 20, 11, 20)), // Gỗ Lạ, Quặng Lạ, Megalodon, EchoItem
                List.of("Bóng Ma", "Lich King"));

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
            weights.forEach((id, count) -> { for(int i=0; i<count; i++) list.add(id); });
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
        c = characterRepository.findByUser_UserIdWithUserAndWallet(c.getUser().getUserId()).orElseThrow();
        captchaService.checkLockStatus(c.getUser());
        if (c.getCurrentEnergy() < 1) throw new RuntimeException("Hết năng lượng!");

        GameMap map = GameMap.findById(mapId);
        if (c.getLevel() < map.minLv) throw new RuntimeException("Cấp độ không đủ!");

        Random r = new Random();
        Wallet w = c.getUser().getWallet();

        c.setCurrentEnergy(c.getCurrentEnergy() - 1);
        c.setCurrentExp(c.getCurrentExp() + 10L + c.getLevel());

        int roll = r.nextInt(100);
        String type; String msg;
        String rewardName = null; Integer rewardAmount = 0; Integer rewardItemId = null;

        // Logic Drop
        if (roll < 60) {
            type = "TEXT"; msg = flavorTextRepo.findRandomContent().orElse("Không có gì.");
            clearGatheringState(c);
        } else if (roll < 85) { // RESOURCE
            type = "GATHERING";
            Integer resId = map.resourceIds.get(r.nextInt(map.resourceIds.size()));

            // XỬ LÝ ĐẶC BIỆT: QUẶNG VÀNG (Code 999) - Map Sa Mạc
            if (resId == 999) {
                type = "GOLD_MINE";
                BigDecimal bonusGold = BigDecimal.valueOf(50 + r.nextInt(100)); // 50-150 Gold
                w.setGold(w.getGold().add(bonusGold));
                msg = "Trúng mánh! Tìm thấy Quặng Vàng! (+" + bonusGold + " Gold)";
                rewardName = "Quặng Vàng";
                clearGatheringState(c);
            } else {
                Item item = itemRepo.findById(resId).orElse(null);
                if (item != null) {
                    // Logic Drop Echo Coin Lẻ (Map 6)
                    // Nếu farm trúng Quặng Lạ/Gỗ Lạ -> Có cơ hội rớt Echo Coin lẻ
                    if (resId == 12 || resId == 4 || resId == 14) {
                        if (r.nextInt(100) < 10) { // 10% cơ hội
                            double echoFrac = 0.001 + (0.499 * r.nextDouble()); // 0.001 -> 0.500
                            BigDecimal echoVal = BigDecimal.valueOf(echoFrac);
                            w.setEchoCoin(w.getEchoCoin().add(echoVal));
                            msg = "Kỳ tích! Tìm thấy " + String.format("%.4f", echoFrac) + " Echo Coin trong quặng!";
                        }
                    }

                    int amount = 5 + r.nextInt(21); // 5 -> 25
                    c.setGatheringItemId(resId);
                    c.setGatheringRemainingAmount(amount);
                    c.setGatheringExpiry(LocalDateTime.now().plusMinutes(3));

                    msg = "Phát hiện " + item.getName() + " (SL: " + amount + ")";
                    rewardName = item.getName();
                    rewardAmount = amount;
                    rewardItemId = resId;
                } else {
                    type = "TEXT"; msg = "Tài nguyên ảo ảnh.";
                    clearGatheringState(c);
                }
            }
        } else if (roll < 95) { // ENEMY
            type = "ENEMY";
            String enemy = map.enemies.get(r.nextInt(map.enemies.size()));
            BattleSession s = createBattleSession(c, enemy, map);
            msg = "Gặp " + s.getEnemyName();
            c.setStatus(CharacterStatus.IN_COMBAT);
            clearGatheringState(c);
        } else { // EQUIPMENT
            type = "ITEM";
            List<Item> equips = itemRepo.findAll().stream().filter(i -> i.getItemId() >= 20).toList();
            if (!equips.isEmpty()) {
                Item eq = equips.get(r.nextInt(equips.size()));
                addItemToInventory(c, eq, 1); // Hàm này sẽ random visual/stats
                msg = "Nhặt được " + eq.getName();
                rewardName = eq.getName();
                rewardItemId = eq.getItemId();
            } else {
                type = "TEXT"; msg = "Rương rỗng.";
            }
            clearGatheringState(c);
        }

        checkLevelUp(c);
        characterRepository.save(c);
        walletRepository.save(w);

        return ExplorationResponse.builder()
                .message(msg).type(type).goldGained(BigDecimal.ZERO)
                .currentExp(c.getCurrentExp()).currentLv(c.getLevel())
                .currentEnergy(c.getCurrentEnergy()).maxEnergy(c.getMaxEnergy())
                .rewardName(rewardName).rewardAmount(rewardAmount).rewardItemId(rewardItemId)
                .build();
    }

    // --- HELPER: THÊM ITEM VÀO TÚI (CÓ RANDOM) ---
    private void addItemToInventory(Character character, Item item, int amount) {
        // Nếu là nguyên liệu -> Cộng dồn
        if (item.getType().equals("MATERIAL") || item.getType().equals("CONSUMABLE")) {
            Optional<UserItem> exist = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), item.getItemId())
                    .stream().filter(ui -> !ui.getIsEquipped()).findFirst();
            if (exist.isPresent()) {
                exist.get().setQuantity(exist.get().getQuantity() + amount);
                userItemRepo.save(exist.get());
                return;
            }
        }

        // Nếu là trang bị -> Tạo mới với Random Assets & Stats
        UserItem ui = new UserItem();
        ui.setCharacter(character);
        ui.setItem(item);
        ui.setQuantity(amount);
        ui.setIsEquipped(false);
        ui.setEnhanceLevel(0);
        ui.setAcquiredAt(LocalDateTime.now());

        // Gán chỉ số mặc định từ Item gốc trước
        ui.setMainStatValue(new BigDecimal(10)); // Giá trị tạm

        // Gọi Service Random hóa
        if (item.getType().equals("WEAPON") || item.getType().equals("ARMOR")) {
            itemGenService.randomizeNewItem(ui);
        }

        userItemRepo.save(ui);
    }

    // ... (Giữ nguyên các hàm gatherResource, addItemToWallet mapping ID mới 1->14) ...
    // Lưu ý: addItemToWallet cần switch case chuẩn theo GameConstants mới
    private void addItemToWallet(Wallet w, int itemId, int amount) {
        switch (itemId) {
            case 1: w.setWood(w.getWood() + amount); break;
            case 2: w.setDriedWood(w.getDriedWood() + amount); break;
            case 3: w.setColdWood(w.getColdWood() + amount); break;
            case 4: w.setStrangeWood(w.getStrangeWood() + amount); break;
            case 5: w.setCoal(w.getCoal() + amount); break;
            case 6: w.setCopperOre(w.getCopperOre() + amount); break;
            case 7: w.setIronOre(w.getIronOre() + amount); break;
            case 8: w.setPlatinum(w.getPlatinum() + amount); break;
            case 9: w.setFish(w.getFish() + amount); break;
            case 10: w.setShark(w.getShark() + amount); break;
            case 12: w.setUnknownMaterial(w.getUnknownMaterial() + amount); break;
            case 11: // Echo Coin Item (nếu drop dạng item thì cộng vào số lẻ wallet)
                w.setEchoCoin(w.getEchoCoin().add(BigDecimal.valueOf(amount)));
                break;
        }
    }

    // ... (Giữ nguyên checkLevelUp, createBattleSession) ...
    private void clearGatheringState(Character c) {
        c.setGatheringItemId(null); c.setGatheringRemainingAmount(0); c.setGatheringExpiry(null);
    }
    private Integer checkLevelUp(Character c) { /* Logic cũ */ return c.getLevel(); }
    private BattleSession createBattleSession(Character p, String n, GameMap m) { /* Logic cũ */ return new BattleSession(); }
}