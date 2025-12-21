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

    // Inject Service để xử lý Logic
    private final EquipmentService equipmentService;
    private final CharacterService characterService; // [QUAN TRỌNG] Để tính lại chỉ số
    private final ItemGenerationService itemGenService; // [QUAN TRỌNG] Để random chỉ số đồ mới

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

        // 1. Check quyền sở hữu
        if (!newItem.getCharacter().getCharId().equals(charId)) {
            throw new RuntimeException("Vật phẩm không thuộc về bạn!");
        }

        Item itemBase = newItem.getItem();
        if (!List.of("WEAPON", "ARMOR").contains(itemBase.getType())) {
            throw new RuntimeException("Chỉ có thể trang bị Vũ khí hoặc Giáp!");
        }

        // 2. Check Level (Tier * 10 - 10)
        int requiredLv = (itemBase.getTier() != null) ? Math.max(1, (itemBase.getTier() - 1) * 10) : 1;
        if (character.getLevel() < requiredLv) {
            throw new RuntimeException("Cấp độ không đủ! Cần Level " + requiredLv);
        }

        // 3. Tháo đồ cũ ở cùng vị trí (Slot)
        List<UserItem> equippedItems = userItemRepo.findByCharacter_CharIdAndIsEquippedTrue(charId);
        SlotType newSlot = itemBase.getSlotType();

        for (UserItem equipped : equippedItems) {
            if (equipped.getItem().getSlotType() == newSlot) {
                equipped.setIsEquipped(false);
                userItemRepo.save(equipped);
            }
        }

        // 4. Mặc đồ mới
        newItem.setIsEquipped(true);
        userItemRepo.save(newItem);

        // 5. Tính lại chỉ số nhân vật
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

        // Tính lại chỉ số (sức mạnh sẽ giảm)
        characterService.recalculateStats(character);
        charRepo.save(character);
    }

    @Override
    @Transactional
    public UserItem enhanceItem(Integer charId, Long userItemId) {
        // Delegate sang EquipmentService đã fix
        return equipmentService.enhanceItem(userItemId);
    }

    @Override
    @Transactional
    public User expandInventory(User user) {
        int currentSlots = user.getInventorySlots() != null ? user.getInventorySlots() : 50;

        if (currentSlots >= 200) throw new RuntimeException("Kho đồ đã đạt giới hạn tối đa!");

        int nextSlots = currentSlots + 5;

        // Công thức Echo Coin của bạn: ((current - 50) / 5) + 1
        int costInt = ((currentSlots - 50) / 5) + 1;
        BigDecimal cost = BigDecimal.valueOf(costInt);

        Wallet w = user.getWallet();
        if (w.getEchoCoin().compareTo(cost) < 0) {
            throw new RuntimeException("Thiếu Echo Coin! Cần " + cost + " để mở thêm 5 ô.");
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
                .orElseThrow(() -> new RuntimeException("Item not found: " + itemId));

        // 1. Kiểm tra sức chứa kho
        int currentCount = userItemRepo.countByCharacter_CharId(character.getCharId());
        int maxSlots = user.getInventorySlots() != null ? user.getInventorySlots() : 50;

        // Nếu là đồ mới (không stack được hoặc chưa có) thì check full kho
        boolean isStackable = List.of("MATERIAL", "CONSUMABLE").contains(item.getType());
        if (!isStackable && currentCount + quantity > maxSlots) {
            throw new RuntimeException("Kho đồ đã đầy (" + currentCount + "/" + maxSlots + ")!");
        }

        // 2. Logic Stack cho nguyên liệu
        if (isStackable) {
            Optional<UserItem> existingItem = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), itemId)
                    .stream()
                    .filter(ui -> !Boolean.TRUE.equals(ui.getIsEquipped()))
                    .findFirst();

            if (existingItem.isPresent()) {
                UserItem ui = existingItem.get();
                ui.setQuantity(ui.getQuantity() + quantity);
                userItemRepo.save(ui);
                return;
            }
        }

        // 3. Tạo vật phẩm mới (Trang bị hoặc nguyên liệu mới)
        for (int i = 0; i < quantity; i++) {
            UserItem ui = UserItem.builder()
                    .character(character)
                    .item(item)
                    .quantity(1) // Trang bị số lượng luôn là 1
                    .isEquipped(false)
                    .enhanceLevel(0)
                    .rarity(item.getRarity() != null ? item.getRarity() : Rarity.COMMON)
                    .acquiredAt(LocalDateTime.now())
                    // Fix lỗi null MainStatValue
                    .mainStatValue(BigDecimal.valueOf(item.getBaseMainStat() != null ? item.getBaseMainStat() : 0))
                    .build();

            // [FIX] Nếu là trang bị, phải Random chỉ số
            if (List.of("WEAPON", "ARMOR").contains(item.getType())) {
                itemGenService.randomizeNewItem(ui);
            } else {
                ui.setSubStats("[]");
            }
            userItemRepo.save(ui);
        }
    }
}