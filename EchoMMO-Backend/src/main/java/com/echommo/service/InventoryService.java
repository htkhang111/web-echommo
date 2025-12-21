package com.echommo.service;

import com.echommo.entity.User;
import com.echommo.entity.UserItem;
import java.util.List;

public interface InventoryService {
    List<UserItem> getInventory(Integer charId);

    void equipItem(Long charId, Long userItemId);

    void unequipItem(Long charId, Long userItemId);

    UserItem enhanceItem(Long charId, Long userItemId);

    void addItemToInventory(User user, Integer itemId, int quantity);

    // [NEW] Mở rộng kho đồ
    User expandInventory(User user);
}