// File: EchoMMO-Backend/src/main/java/com/echommo/service/InventoryService.java

package com.echommo.service;

import com.echommo.entity.User;
import com.echommo.entity.UserItem;
import java.util.List;

public interface InventoryService {
    List<UserItem> getInventory(Integer charId);

    // [FIX] Đổi Long -> Integer để chiều lòng client
    void equipItem(Integer charId, Integer userItemId);
    void unequipItem(Integer charId, Integer userItemId);
    UserItem enhanceItem(Integer charId, Integer userItemId);

    User expandInventory(User user);
    void addItemToInventory(User user, Integer itemId, int quantity);

    // [FIX] Đổi Long -> Integer
    UserItem repairItem(User user, Integer userItemId);
}