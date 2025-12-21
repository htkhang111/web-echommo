package com.echommo.service;

import com.echommo.entity.User;
import com.echommo.entity.UserItem;
import java.util.List;

public interface InventoryService {
    List<UserItem> getInventory(Integer charId);
    void equipItem(Integer charId, Long userItemId);
    void unequipItem(Integer charId, Long userItemId);
    UserItem enhanceItem(Integer charId, Long userItemId);
    User expandInventory(User user);
    void addItemToInventory(User user, Integer itemId, int quantity);

    // [NEW] API SỬA ĐỒ
    UserItem repairItem(User user, Long userItemId);
}