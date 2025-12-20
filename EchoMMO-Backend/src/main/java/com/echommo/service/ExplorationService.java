//package com.echommo.service;
//
//import com.echommo.config.GameConstants;
//import com.echommo.dto.ExplorationResponse;
//import com.echommo.entity.*;
//import com.echommo.entity.Character;
//import com.echommo.enums.CharacterStatus;
//import com.echommo.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Random;
//
//@Service
//@RequiredArgsConstructor
//public class ExplorationService {
//
//    private final WalletRepository walletRepository;
//    private final ItemRepository itemRepository;
//    private final InventoryService inventoryService;
//    private final CharacterRepository characterRepository;
//    private final EnemyRepository enemyRepository;
//    private final BattleSessionRepository battleSessionRepository;
//    private final FlavorTextRepository flavorTextRepository;
//
//    private final Random random = new Random();
//
//    @Transactional
//    public ExplorationResponse explore(User user, int mapId) {
//        Character character = characterRepository.findByUser(user)
//                .orElseThrow(() -> new RuntimeException("Character not found"));
//
//        if (character.getCurrentEnergy() < 1) {
//            throw new RuntimeException("Hết thể lực! Hãy về Spa nghỉ ngơi.");
//        }
//        character.setCurrentEnergy(character.getCurrentEnergy() - 1);
//        character.setCurrentExp(character.getCurrentExp() + 10);
//        checkLevelUp(character);
//
//        clearGatheringState(character);
//
//        int roll = random.nextInt(100);
//        ExplorationResponse response = new ExplorationResponse();
//
//        // --- 60% TEXT ---
//        if (roll < 60) {
//            String flavorText = flavorTextRepository.findRandomContent()
//                    .orElse("Bạn đi dạo quanh map và tận hưởng không khí trong lành.");
//            response.setType("TEXT");
//            response.setMessage(flavorText);
//            response.setLog(flavorText);
//        }
//        // --- 20% GATHERING ---
//        else if (roll < 80) {
//            int resourceId = rollItemForMap(mapId);
//            // [FIX] ID là Integer, không ép kiểu sang Long
//            Item item = itemRepository.findById(resourceId).orElse(null);
//
//            if (item != null) {
//                int nodeSize = 5 + random.nextInt(6);
//                character.setGatheringItemId(item.getItemId());
//                character.setGatheringRemainingAmount(nodeSize);
//                character.setGatheringExpiry(LocalDateTime.now().plusMinutes(5));
//
//                response.setType("GATHERING");
//                response.setMessage("Phát hiện một " + item.getName() + "!");
//                response.setRewardName(item.getName());
//                response.setRewardItemId(item.getItemId());
//                response.setRewardAmount(nodeSize);
//                response.setImageUrl(item.getImageUrl());
//            } else {
//                response.setType("TEXT");
//                response.setMessage("Khu vực này có vẻ đã bị khai thác hết.");
//            }
//        }
//        // --- 11% ENEMY ---
//        else if (roll < 91) {
//            Enemy enemy = getRandomEnemy(mapId);
//            createBattleSession(character, enemy);
//            character.setStatus(CharacterStatus.IN_COMBAT);
//
//            response.setType("ENEMY");
//            response.setMessage("Nguy hiểm! Bạn đụng độ " + enemy.getName() + "!");
//            response.setRewardName(enemy.getName());
//        }
//        // --- 9% ITEM ---
//        else {
//            List<Item> items = itemRepository.findAll();
//            if (!items.isEmpty()) {
//                Item luckyItem = items.get(random.nextInt(items.size()));
//                inventoryService.addItemToInventory(user, luckyItem.getItemId(), 1);
//
//                response.setType("ITEM");
//                response.setMessage("May mắn! Bạn nhặt được " + luckyItem.getName());
//                response.setRewardName(luckyItem.getName());
//                response.setRewardItemId(luckyItem.getItemId());
//                response.setRewardAmount(1);
//                response.setImageUrl(luckyItem.getImageUrl());
//            } else {
//                response.setType("TEXT");
//                response.setMessage("Bạn tìm thấy một chiếc rương rỗng.");
//            }
//        }
//
//        characterRepository.save(character);
//
//        response.setCurrentLv(character.getLevel());
//        response.setCurrentExp(character.getCurrentExp());
//        response.setCurrentEnergy(character.getCurrentEnergy());
//        response.setMaxEnergy(character.getMaxEnergy());
//
//        return response;
//    }
//
//    @Transactional
//    public ExplorationResponse gatherResource(User user, Integer itemId, int amount) {
//        Character c = characterRepository.findByUser(user).orElseThrow();
//
//        if (c.getGatheringItemId() == null || !c.getGatheringItemId().equals(itemId)) {
//            throw new RuntimeException("Mỏ tài nguyên không hợp lệ hoặc đã biến mất!");
//        }
//        if (c.getGatheringRemainingAmount() < amount) amount = c.getGatheringRemainingAmount();
//
//        if (c.getCurrentEnergy() < 1) throw new RuntimeException("Không đủ sức để thu hoạch!");
//        c.setCurrentEnergy(c.getCurrentEnergy() - 1);
//
//        inventoryService.addItemToInventory(user, itemId, amount);
//
//        c.setGatheringRemainingAmount(c.getGatheringRemainingAmount() - amount);
//        if (c.getGatheringRemainingAmount() <= 0) {
//            clearGatheringState(c);
//        }
//
//        characterRepository.save(c);
//
//        ExplorationResponse res = new ExplorationResponse();
//        res.setMessage("Thu hoạch thành công " + amount + " cái.");
//        res.setCurrentEnergy(c.getCurrentEnergy());
//        res.setRewardAmount(c.getGatheringRemainingAmount());
//        return res;
//    }
//
//    private void createBattleSession(Character player, Enemy enemy) {
//        List<BattleSession> old = battleSessionRepository.findByCharacter_CharId(player.getCharId());
//        battleSessionRepository.deleteAll(old);
//
//        BattleSession session = new BattleSession();
//        session.setCharacter(player);
//        // [FIX] Gọi getEnemyId() - Đã đảm bảo có trong Entity Enemy
//        session.setEnemyId(enemy.getEnemyId());
//        session.setEnemyName(enemy.getName());
//        session.setEnemyMaxHp(enemy.getHp());
//        session.setEnemyCurrentHp(enemy.getHp());
//        session.setEnemyAtk(enemy.getAtk());
//        session.setEnemyDef(enemy.getDef());
//        session.setPlayerMaxHp(player.getMaxHp());
//        session.setPlayerCurrentHp(player.getCurrentHp());
//        session.setCurrentTurn(0);
//
//        battleSessionRepository.save(session);
//    }
//
//    private Enemy getRandomEnemy(int mapId) {
//        List<Enemy> enemies = enemyRepository.findAll();
//        if (enemies.isEmpty()) {
//            Enemy e = new Enemy();
//            e.setName("Quái Test"); e.setHp(100); e.setAtk(10); e.setDef(0);
//            return enemyRepository.save(e);
//        }
//        return enemies.get(random.nextInt(enemies.size()));
//    }
//
//    private void checkLevelUp(Character c) {
//        long expNeed = c.getLevel() * 100L;
//        if (c.getCurrentExp() >= expNeed) {
//            c.setLevel(c.getLevel() + 1);
//            c.setCurrentExp(c.getCurrentExp() - expNeed);
//            c.setMaxHp(c.getMaxHp() + 50);
//            c.setCurrentHp(c.getMaxHp());
//        }
//    }
//
//    private void clearGatheringState(Character c) {
//        c.setGatheringItemId(null);
//        c.setGatheringRemainingAmount(0);
//        c.setGatheringExpiry(null);
//    }
//
//    private int rollItemForMap(int mapId) {
//        int roll = random.nextInt(100);
//        if (mapId == GameConstants.MAP_FINAL) {
//            if (roll < 50) return GameConstants.MAT_ORE_PLATINUM;
//            return GameConstants.MAT_WOOD_COLD;
//        }
//        if (roll < 50) return GameConstants.MAT_ORE_COPPER;
//        return GameConstants.MAT_WOOD_OAK;
//    }
//}


package com.echommo.service;

import com.echommo.config.GameConstants;
import com.echommo.dto.ExplorationResponse;
import com.echommo.entity.*;
import com.echommo.entity.Character;
import com.echommo.enums.CharacterStatus;
import com.echommo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ExplorationService {

    private final WalletRepository walletRepository;
    private final ItemRepository itemRepository;
    private final InventoryService inventoryService;
    private final CharacterRepository characterRepository;
    private final EnemyRepository enemyRepository;
    private final BattleSessionRepository battleSessionRepository;
    private final FlavorTextRepository flavorTextRepository;

    private final Random random = new Random();

    @Transactional
    public ExplorationResponse explore(User user, int mapId) {
        Character character = characterRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        if (character.getCurrentEnergy() < 1) {
            throw new RuntimeException("Hết thể lực! Hãy về Spa nghỉ ngơi.");
        }
        character.setCurrentEnergy(character.getCurrentEnergy() - 1);
        character.setCurrentExp(character.getCurrentExp() + 10);
        checkLevelUp(character);

        clearGatheringState(character);

        int roll = random.nextInt(100);
        ExplorationResponse response = new ExplorationResponse();

        // --- 60% TEXT ---
        if (roll < 60) {
            String flavorText = flavorTextRepository.findRandomContent()
                    .orElse("Bạn đi dạo quanh map và tận hưởng không khí trong lành.");
            response.setType("TEXT");
            response.setMessage(flavorText);
            response.setLog(flavorText);
        }
        // --- 20% GATHERING ---
        else if (roll < 80) {
            int resourceId = rollItemForMap(mapId);
            Item item = itemRepository.findById(resourceId).orElse(null);

            if (item != null) {
                int nodeSize = 5 + random.nextInt(6);
                character.setGatheringItemId(item.getItemId());
                character.setGatheringRemainingAmount(nodeSize);
                character.setGatheringExpiry(LocalDateTime.now().plusMinutes(5));

                response.setType("GATHERING");
                response.setMessage("Phát hiện một " + item.getName() + "!");
                response.setRewardName(item.getName());
                response.setRewardItemId(item.getItemId());
                response.setRewardAmount(nodeSize);
                response.setImageUrl(item.getImageUrl());
            } else {
                response.setType("TEXT");
                response.setMessage("Khu vực này có vẻ đã bị khai thác hết.");
            }
        }
        // --- 11% ENEMY ---
        else if (roll < 91) {
            Enemy enemy = getRandomEnemy(mapId);
            createBattleSession(character, enemy);
            character.setStatus(CharacterStatus.IN_COMBAT);

            response.setType("ENEMY");
            response.setMessage("Nguy hiểm! Bạn đụng độ " + enemy.getName() + "!");
            response.setRewardName(enemy.getName());
        }
        // --- 9% ITEM ---
        else {
            List<Item> items = itemRepository.findAll();
            if (!items.isEmpty()) {
                Item luckyItem = items.get(random.nextInt(items.size()));
                inventoryService.addItemToInventory(user, luckyItem.getItemId(), 1);

                response.setType("ITEM");
                response.setMessage("May mắn! Bạn nhặt được " + luckyItem.getName());
                response.setRewardName(luckyItem.getName());
                response.setRewardItemId(luckyItem.getItemId());
                response.setRewardAmount(1);
                response.setImageUrl(luckyItem.getImageUrl());
            } else {
                response.setType("TEXT");
                response.setMessage("Bạn tìm thấy một chiếc rương rỗng.");
            }
        }

        characterRepository.save(character);

        response.setCurrentLv(character.getLevel());
        response.setCurrentExp(character.getCurrentExp());
        response.setCurrentEnergy(character.getCurrentEnergy());
        response.setMaxEnergy(character.getMaxEnergy());

        return response;
    }

    @Transactional
    public ExplorationResponse gatherResource(User user, Integer itemId, int amount) {
        Character c = characterRepository.findByUser(user).orElseThrow();

        if (c.getGatheringItemId() == null || !c.getGatheringItemId().equals(itemId)) {
            throw new RuntimeException("Mỏ tài nguyên không hợp lệ hoặc đã biến mất!");
        }
        if (c.getGatheringRemainingAmount() < amount) amount = c.getGatheringRemainingAmount();

        if (c.getCurrentEnergy() < 1) throw new RuntimeException("Không đủ sức để thu hoạch!");
        c.setCurrentEnergy(c.getCurrentEnergy() - 1);

        inventoryService.addItemToInventory(user, itemId, amount);

        c.setGatheringRemainingAmount(c.getGatheringRemainingAmount() - amount);
        if (c.getGatheringRemainingAmount() <= 0) {
            clearGatheringState(c);
        }

        characterRepository.save(c);

        ExplorationResponse res = new ExplorationResponse();
        res.setMessage("Thu hoạch thành công " + amount + " cái.");
        res.setCurrentEnergy(c.getCurrentEnergy());
        res.setRewardAmount(c.getGatheringRemainingAmount());
        return res;
    }

    private void createBattleSession(Character player, Enemy enemy) {
        // [FIX] Thay vì xóa rồi tạo mới (gây lỗi Duplicate Entry), ta tìm và cập nhật session cũ
        // Nếu tìm thấy list, lấy phần tử đầu tiên. Nếu không, tạo mới.
        BattleSession session = battleSessionRepository.findByCharacter_CharId(player.getCharId())
                .stream()
                .findFirst()
                .orElse(new BattleSession());

        session.setCharacter(player);
        // Cập nhật thông tin quái mới đè lên thông tin cũ
        session.setEnemyId(enemy.getEnemyId());
        session.setEnemyName(enemy.getName());
        session.setEnemyMaxHp(enemy.getHp());
        session.setEnemyCurrentHp(enemy.getHp());
        session.setEnemyAtk(enemy.getAtk());
        session.setEnemyDef(enemy.getDef());

        // Cập nhật trạng thái người chơi
        session.setPlayerMaxHp(player.getMaxHp());
        session.setPlayerCurrentHp(player.getCurrentHp());
        session.setCurrentTurn(0);

        // Lưu đè (Update) thay vì Insert mới
        battleSessionRepository.save(session);
    }

    private Enemy getRandomEnemy(int mapId) {
        List<Enemy> enemies = enemyRepository.findAll();
        if (enemies.isEmpty()) {
            Enemy e = new Enemy();
            e.setName("Quái Test"); e.setHp(100); e.setAtk(10); e.setDef(0);
            return enemyRepository.save(e);
        }
        return enemies.get(random.nextInt(enemies.size()));
    }

    private void checkLevelUp(Character c) {
        long expNeed = c.getLevel() * 100L;
        if (c.getCurrentExp() >= expNeed) {
            c.setLevel(c.getLevel() + 1);
            c.setCurrentExp(c.getCurrentExp() - expNeed);
            c.setMaxHp(c.getMaxHp() + 50);
            c.setCurrentHp(c.getMaxHp());
        }
    }

    private void clearGatheringState(Character c) {
        c.setGatheringItemId(null);
        c.setGatheringRemainingAmount(0);
        c.setGatheringExpiry(null);
    }

    private int rollItemForMap(int mapId) {
        int roll = random.nextInt(100);
        if (mapId == GameConstants.MAP_FINAL) {
            if (roll < 50) return GameConstants.MAT_ORE_PLATINUM;
            return GameConstants.MAT_WOOD_COLD;
        }
        if (roll < 50) return GameConstants.MAT_ORE_COPPER;
        return GameConstants.MAT_WOOD_OAK;
    }
}