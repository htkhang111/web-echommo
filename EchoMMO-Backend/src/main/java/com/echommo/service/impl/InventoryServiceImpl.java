package com.echommo.service.impl;

import com.echommo.entity.Character;
import com.echommo.entity.Item;
import com.echommo.entity.User;
import com.echommo.entity.UserItem;
import com.echommo.entity.Wallet;
import com.echommo.enums.Rarity;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.ItemRepository;
import com.echommo.repository.UserItemRepository;
import com.echommo.repository.UserRepository;
import com.echommo.repository.WalletRepository;
import com.echommo.service.EquipmentService;
import com.echommo.service.InventoryService;
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
    private final EquipmentService equipmentService;
    private final WalletRepository walletRepo;
    private final UserRepository userRepo;

    @Override
    public List<UserItem> getInventory(Integer charId) {
        return userItemRepo.findByCharacter_CharIdOrderByAcquiredAtDesc(charId);
    }

    @Override
    @Transactional
    public void equipItem(Long charId, Long userItemId) {
        // Logic mặc đồ giữ nguyên, xử lý ở Controller hoặc chuyển vào đây nếu muốn
    }

    @Override
    @Transactional
    public void unequipItem(Long charId, Long userItemId) {
        // Logic tháo đồ
    }

    @Override
    @Transactional
    public UserItem enhanceItem(Long charId, Long userItemId) {
        return equipmentService.enhanceItem(userItemId);
    }

    @Override
    @Transactional
    public User expandInventory(User user) {
        int currentSlots = user.getInventorySlots() != null ? user.getInventorySlots() : 50;
        int nextSlots = currentSlots + 5;

        // Công thức: ((current - 50) / 5) + 1
        // VD: 50 -> 1 Coin, 55 -> 2 Coin...
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
        // Giữ nguyên logic cũ
        Character character = charRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        Item item = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found: " + itemId));

        Optional<UserItem> existingItem = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), itemId)
                .stream()
                .filter(ui -> !Boolean.TRUE.equals(ui.getIsEquipped()))
                .findFirst();

        if (existingItem.isPresent()) {
            UserItem ui = existingItem.get();
            ui.setQuantity(ui.getQuantity() + quantity);
            userItemRepo.save(ui);
        } else {
            UserItem ui = UserItem.builder()
                    .character(character)
                    .item(item)
                    .quantity(quantity)
                    .isEquipped(false)
                    .enhanceLevel(0)
                    .rarity(Rarity.COMMON)
                    .acquiredAt(LocalDateTime.now())
                    .mainStatValue(BigDecimal.ZERO)
                    .build();
            userItemRepo.save(ui);
        }
    }
}