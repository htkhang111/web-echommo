package com.echommo.service;

import com.echommo.entity.UserItem;
import com.echommo.enums.SlotType;
import com.echommo.repository.UserItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    @Autowired
    private UserItemRepository userItemRepository;

    @Transactional
    public void equipItem(Integer charId, Long userItemIdToEquip) {
        // 1. Find the specific item the user wants to equip
        UserItem newItem = userItemRepository.findByUserItemIdAndCharacter_CharId(userItemIdToEquip, charId)
                .orElseThrow(() -> new RuntimeException("Item not found or does not belong to you"));

        // 2. Get the slot type from the Item definition
        SlotType slot = newItem.getItem().getSlotType();

        // 3. Check if something is already wearing in that slot
        userItemRepository.findEquippedItemBySlot(charId, slot).ifPresent(oldItem -> {
            // Unequip the old item
            oldItem.setEquipped(false);
            userItemRepository.save(oldItem);
        });

        // 4. Equip the new item
        newItem.setEquipped(true);
        userItemRepository.save(newItem);

        // Note: You might want to trigger a "recalculate stats" method here later
    }
}