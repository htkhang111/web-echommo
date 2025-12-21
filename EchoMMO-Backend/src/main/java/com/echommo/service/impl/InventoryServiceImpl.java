package com.echommo.service.impl;

import com.echommo.entity.*;
import com.echommo.entity.Character;
import com.echommo.enums.Rarity;
import com.echommo.enums.SlotType;
import com.echommo.repository.*;
import com.echommo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final UserItemRepository userItemRepo;
    private final ItemRepository itemRepo;
    private final CharacterRepository charRepo;
    private final WalletRepository walletRepo;
    private final UserRepository userRepo;
    private final EquipmentService equipmentService;
    private final CharacterService characterService;
    private final ItemGenerationService itemGenService;

    @Override
    public List<UserItem> getInventory(Integer charId) {
        return userItemRepo.findByCharacter_CharIdOrderByAcquiredAtDesc(charId);
    }

    @Override
    @Transactional
    public void equipItem(Integer charId, Long userItemId) {
        Character character = charRepo.findById(charId)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        UserItem newItem = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!newItem.getCharacter().getCharId().equals(charId)) {
            throw new RuntimeException("Vật phẩm không thuộc về bạn!");
        }

        Item itemBase = newItem.getItem();

        // [FIX] Cho phép trang bị TOOL (Cúp, Rìu, Xẻng, Cần Câu)
        boolean isGear = List.of("WEAPON", "ARMOR").contains(itemBase.getType());
        boolean isTool = "TOOL".equals(itemBase.getType());

        if (!isGear && !isTool) {
            throw new RuntimeException("Vật phẩm này không thể trang bị!");
        }

        // Check Level
        int requiredLv = (itemBase.getTier() != null) ? Math.max(1, (itemBase.getTier() - 1) * 10) : 1;
        if (character.getLevel() < requiredLv) {
            throw new RuntimeException("Cấp độ không đủ! Cần Level " + requiredLv);
        }

        // Tháo đồ cũ cùng slot
        SlotType newSlot = itemBase.getSlotType();
        List<UserItem> equippedItems = userItemRepo.findByCharacter_CharIdAndIsEquippedTrue(charId);

        for (UserItem equipped : equippedItems) {
            if (equipped.getItem().getSlotType() == newSlot) {
                equipped.setIsEquipped(false);
                userItemRepo.save(equipped);
            }
        }

        newItem.setIsEquipped(true);
        userItemRepo.save(newItem);

        characterService.recalculateStats(character);
        charRepo.save(character);
    }

    @Override
    @Transactional
    public void unequipItem(Integer charId, Long userItemId) {
        Character character = charRepo.findById(charId).orElseThrow();
        UserItem item = userItemRepo.findById(userItemId).orElseThrow();

        if (!item.getCharacter().getCharId().equals(charId)) {
            throw new RuntimeException("Vật phẩm không thuộc về bạn!");
        }
        if (!Boolean.TRUE.equals(item.getIsEquipped())) {
            throw new RuntimeException("Vật phẩm này chưa được trang bị!");
        }

        item.setIsEquipped(false);
        userItemRepo.save(item);

        characterService.recalculateStats(character);
        charRepo.save(character);
    }

    @Override
    @Transactional
    public UserItem enhanceItem(Integer charId, Long userItemId) {
        return equipmentService.enhanceItem(userItemId);
    }

    // [NEW] LOGIC SỬA ĐỒ (GIÁ RẺ 0.1/Point)
    @Override
    @Transactional
    public UserItem repairItem(User user, Long userItemId) {
        UserItem userItem = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm!"));

        if (!userItem.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Vật phẩm không thuộc về bạn!");
        }

        Integer current = userItem.getCurrentDurability();
        Integer max = userItem.getMaxDurability();

        if (current == null) current = 0;
        if (max == null || max <= 0) max = 100;

        if (current >= max) {
            throw new RuntimeException("Độ bền đã đầy, không cần sửa!");
        }

        int missingDurability = max - current;

        // CÔNG THỨC: 1 Echo Coin sửa 10 Độ bền
        double costValue = Math.ceil(missingDurability / 10.0);
        BigDecimal cost = BigDecimal.valueOf(costValue);

        if (cost.compareTo(BigDecimal.ONE) < 0) cost = BigDecimal.ONE;

        Wallet wallet = user.getWallet();
        if (wallet.getEchoCoin().compareTo(cost) < 0) {
            throw new RuntimeException("Không đủ Echo Coin! Cần " + cost + " để sửa.");
        }

        wallet.setEchoCoin(wallet.getEchoCoin().subtract(cost));
        walletRepo.save(wallet);

        userItem.setCurrentDurability(max);
        return userItemRepo.save(userItem);
    }

    @Override
    @Transactional
    public User expandInventory(User user) {
        int currentSlots = user.getInventorySlots() != null ? user.getInventorySlots() : 50;
        if (currentSlots >= 200) throw new RuntimeException("Kho đồ đã đạt giới hạn!");

        int nextSlots = currentSlots + 5;
        int costInt = ((currentSlots - 50) / 5) + 1;
        BigDecimal cost = BigDecimal.valueOf(costInt);

        Wallet w = user.getWallet();
        if (w.getEchoCoin().compareTo(cost) < 0) {
            throw new RuntimeException("Thiếu Echo Coin! Cần " + cost);
        }

        w.setEchoCoin(w.getEchoCoin().subtract(cost));
        walletRepo.save(w);

        user.setInventorySlots(nextSlots);
        return userRepo.save(user);
    }

    @Override
    @Transactional
    public void addItemToInventory(User user, Integer itemId, int quantity) {
        Character character = charRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        Item item = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        int currentCount = userItemRepo.countByCharacter_CharId(character.getCharId());
        int maxSlots = user.getInventorySlots() != null ? user.getInventorySlots() : 50;

        boolean isStackable = List.of("MATERIAL", "CONSUMABLE").contains(item.getType());
        if (!isStackable && currentCount + quantity > maxSlots) {
            throw new RuntimeException("Kho đồ đã đầy!");
        }

        if (isStackable) {
            Optional<UserItem> existingItem = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), itemId)
                    .stream().filter(ui -> !Boolean.TRUE.equals(ui.getIsEquipped())).findFirst();

            if (existingItem.isPresent()) {
                UserItem ui = existingItem.get();
                ui.setQuantity(ui.getQuantity() + quantity);
                userItemRepo.save(ui);
                return;
            }
        }

        for (int i = 0; i < quantity; i++) {
            UserItem ui = UserItem.builder()
                    .character(character)
                    .item(item)
                    .quantity(1)
                    .isEquipped(false)
                    .enhanceLevel(0)
                    .rarity(item.getRarity() != null ? item.getRarity() : Rarity.COMMON)
                    .acquiredAt(LocalDateTime.now())
                    .mainStatValue(BigDecimal.valueOf(item.getBaseMainStat() != null ? item.getBaseMainStat() : 0))
                    // [NEW] SET ĐỘ BỀN
                    .maxDurability(item.getMaxDurability() != null ? item.getMaxDurability() : 100)
                    .currentDurability(item.getMaxDurability() != null ? item.getMaxDurability() : 100)
                    .build();

            if (List.of("WEAPON", "ARMOR", "TOOL").contains(item.getType())) {
                itemGenService.randomizeNewItem(ui);
            } else {
                ui.setSubStats("[]");
            }
            userItemRepo.save(ui);
        }
    }
}