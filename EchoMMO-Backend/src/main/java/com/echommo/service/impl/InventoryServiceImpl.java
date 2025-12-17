package com.echommo.service.impl;

import com.echommo.entity.UserItem;
import com.echommo.entity.Character;
import com.echommo.repository.UserItemRepository;
import com.echommo.repository.CharacterRepository;
import com.echommo.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Random;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired private UserItemRepository userItemRepository;
    @Autowired private CharacterRepository characterRepository;

    private Character getChar(Integer userId) {
        return characterRepository.findByUser_UserId(userId.longValue())
                .orElseThrow(() -> new RuntimeException("Chưa tạo nhân vật"));
    }

    @Override
    public List<UserItem> getInventory(Integer userId) {
        Character c = getChar(userId);
        return userItemRepository.findByCharacter_CharIdOrderByAcquiredAtDesc(c.getCharId());
    }

    @Override
    @Transactional
    public void equipItem(Integer userId, Long userItemId) {
        Character c = getChar(userId);
        UserItem item = userItemRepository.findByUserItemIdAndCharacter_CharId(userItemId, c.getCharId())
                .orElseThrow(() -> new RuntimeException("Đồ không tồn tại"));

        userItemRepository.findEquippedItemBySlot(c.getCharId(), item.getItem().getSlotType())
                .ifPresent(old -> {
                    old.setIsEquipped(false);
                    userItemRepository.save(old);
                });

        item.setIsEquipped(true);
        userItemRepository.save(item);
    }

    @Override
    @Transactional
    public void unequipItem(Integer userId, Long userItemId) {
        Character c = getChar(userId);
        UserItem item = userItemRepository.findByUserItemIdAndCharacter_CharId(userItemId, c.getCharId())
                .orElseThrow(() -> new RuntimeException("Đồ không tồn tại"));
        item.setIsEquipped(false);
        userItemRepository.save(item);
    }

    @Override
    @Transactional
    public UserItem enhanceItem(Integer userId, Long userItemId) {
        Character c = getChar(userId);
        UserItem item = userItemRepository.findByUserItemIdAndCharacter_CharId(userItemId, c.getCharId())
                .orElseThrow(() -> new RuntimeException("Đồ không tồn tại"));

        int current = item.getEnhancementLevel();
        if (new Random().nextInt(100) < (100 - current * 10)) {
            item.setEnhancementLevel(current + 1);
        } else {
            throw new RuntimeException("Thất bại");
        }
        return userItemRepository.save(item);
    }
}