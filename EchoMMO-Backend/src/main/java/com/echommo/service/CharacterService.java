package com.echommo.service;

import com.echommo.config.GameConstants;
import com.echommo.dto.CharacterRequest;
import com.echommo.entity.Character;
import com.echommo.entity.Item;
import com.echommo.entity.User;
import com.echommo.entity.UserItem;
import com.echommo.enums.CharacterStatus;
import com.echommo.enums.Rarity;
import com.echommo.enums.SlotType;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.ItemRepository;
import com.echommo.repository.UserItemRepository;
import com.echommo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CharacterService {

    @Autowired private CharacterRepository characterRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ItemRepository itemRepo;
    @Autowired private UserItemRepository userItemRepo;

    public Character getMyCharacter() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) return null;

        Character c = characterRepo.findByUser_UserId(user.getUserId()).orElse(null);
        if (c != null) {
            int expectedPoints = (c.getLevel() - 1) * GameConstants.STAT_POINTS_PER_LEVEL;
            int usedPoints = (c.getStr() - 5) + (c.getDex() - 5) + (c.getIntelligence() - 5) + (c.getLuck() - 5);
            c.setStatPoints(Math.max(0, expectedPoints - usedPoints));
        }
        return c;
    }

    // [QUAN TRỌNG] Hàm này được GameService gọi
    @Transactional
    public Character createDefaultCharacter(User user) {
        if (characterRepo.findByUser_UserId(user.getUserId()).isPresent()) {
            return characterRepo.findByUser_UserId(user.getUserId()).get();
        }

        Character c = new Character();
        c.setUser(user);
        c.setName(user.getUsername());
        c.setLevel(1);
        c.setStatus(CharacterStatus.IDLE);

        Character savedChar = characterRepo.save(c);
        grantStarterPack(savedChar);
        return savedChar;
    }

    @Transactional
    public Character createCharacter(CharacterRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        if (characterRepo.findByUser_UserId(user.getUserId()).isPresent()) {
            throw new RuntimeException("Bạn đã có nhân vật rồi!");
        }

        Character c = new Character();
        c.setUser(user);
        c.setName(req.getName());
        c.setLevel(1);
        c.setStatus(CharacterStatus.IDLE);

        Character savedChar = characterRepo.save(c);
        grantStarterPack(savedChar);
        return savedChar;
    }

    private void grantStarterPack(Character character) {
        Item sword = itemRepo.findByName("Kiếm Gỗ").orElse(null);
        if (sword != null) createItemForChar(character, sword, 1, true);

        Item potion = itemRepo.findByName("Bình Máu Nhỏ").orElse(null);
        if (potion != null) createItemForChar(character, potion, 5, false);
    }

    private void createItemForChar(Character c, Item item, int qty, boolean equip) {
        UserItem ui = new UserItem();
        ui.setCharacter(c);
        ui.setItem(item);
        ui.setQuantity(qty);
        ui.setIsEquipped(equip);
        ui.setRarity(Rarity.COMMON);
        ui.setEnhanceLevel(0);

        if (item.getSlotType() == SlotType.WEAPON) {
            ui.setMainStatType("ATK_FLAT");
            ui.setMainStatValue(BigDecimal.valueOf(5));
        } else {
            ui.setMainStatType(null);
            ui.setMainStatValue(BigDecimal.ZERO);
        }
        ui.setSubStats("[]");
        ui.setAcquiredAt(LocalDateTime.now());
        userItemRepo.save(ui);
    }
}