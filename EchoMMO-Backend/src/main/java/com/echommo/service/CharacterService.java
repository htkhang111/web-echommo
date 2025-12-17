package com.echommo.service;

import com.echommo.dto.CharacterRequest;
import com.echommo.entity.*;
import com.echommo.entity.Character;
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

    // [NEW] Thêm Repo để tặng đồ tân thủ
    @Autowired private ItemRepository itemRepo;
    @Autowired private UserItemRepository userItemRepo;

    public Character getMyCharacter() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) return null;
        return characterRepo.findByUser_UserId(user.getUserId()).orElse(null);
    }

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

        // Stats
        initStats(c);

        Character savedChar = characterRepo.save(c);

        // [NEW] Tặng đồ tân thủ
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

        // Stats
        initStats(c);

        Character savedChar = characterRepo.save(c);

        // [NEW] Tặng đồ tân thủ
        grantStarterPack(savedChar);

        return savedChar;
    }

    // --- Helper: Khởi tạo chỉ số ---
    private void initStats(Character c) {
        c.setStr(5); c.setDex(5); c.setIntelligence(5); c.setLuck(5);
        c.setStatPoints(0);
        c.setBaseAtk(10);
        c.setBaseDef(2);
        c.setBaseSpeed(10);
        c.setBaseCritRate(5);
        c.setBaseCritDmg(150);
        c.setMaxHp(100); c.setCurrentHp(100);
        c.setMaxEnergy(50); c.setCurrentEnergy(50);
        c.setCurrentExp(0L);
    }

    // --- [NEW] Helper: Tặng đồ tân thủ ---
    private void grantStarterPack(Character character) {
        // 1. Tặng Kiếm Gỗ (Nếu có trong DB)
        Item sword = itemRepo.findByName("Kiếm Gỗ").orElse(null);
        if (sword != null) {
            createItemForChar(character, sword, 1, true); // Mặc luôn cho máu
        }

        // 2. Tặng 5 Bình Máu Nhỏ
        Item potion = itemRepo.findByName("Bình Máu Nhỏ").orElse(null);
        if (potion != null) {
            createItemForChar(character, potion, 5, false);
        }
    }

    private void createItemForChar(Character c, Item item, int qty, boolean equip) {
        UserItem ui = new UserItem();
        ui.setCharacter(c); // Quan trọng: Link với Character
        ui.setItem(item);
        ui.setQuantity(qty);
        ui.setIsEquipped(equip);
        ui.setRarity(Rarity.COMMON);
        ui.setEnhanceLevel(0);

        // Set Main Stat cơ bản nếu là trang bị
        if (item.getSlotType() == SlotType.WEAPON) {
            ui.setMainStatType("ATK_FLAT");
            ui.setMainStatValue(BigDecimal.valueOf(5)); // Kiếm gỗ cùi +5 dame
        } else if (item.getSlotType() == SlotType.CONSUMABLE) {
            ui.setMainStatType(null);
            ui.setMainStatValue(BigDecimal.ZERO);
        }

        ui.setSubStats("[]");
        ui.setAcquiredAt(LocalDateTime.now());

        userItemRepo.save(ui);
    }
}