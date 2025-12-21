package com.echommo.service;

import com.echommo.entity.User;
import com.echommo.entity.UserItem;
import java.util.List;

public interface InventoryService {
    // Lấy danh sách đồ trong kho
    List<UserItem> getInventory(Integer charId);

    // Mặc đồ
    void equipItem(Integer charId, Long userItemId);

    // Tháo đồ
    void unequipItem(Integer charId, Long userItemId);

    // Cường hóa
    UserItem enhanceItem(Integer charId, Long userItemId);

    // Thêm đồ (dùng cho các service khác)
    void addItemToInventory(User user, Integer itemId, int quantity);

    // Mở rộng kho đồ
    User expandInventory(User user);
}