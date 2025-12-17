package com.echommo.service;

import com.echommo.entity.UserItem;
import java.util.List;

public interface InventoryService {
    List<UserItem> getInventory(Integer userId);
    void equipItem(Integer userId, Long userItemId);
    void unequipItem(Integer userId, Long userItemId);
    UserItem enhanceItem(Integer userId, Long userItemId);
}