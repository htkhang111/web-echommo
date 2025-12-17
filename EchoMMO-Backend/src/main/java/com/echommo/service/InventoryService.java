//package com.echommo.service;
//
//import com.echommo.entity.UserItem;
//import java.util.List;
//
//public interface InventoryService {
//    List<UserItem> getInventory(Integer userId);
//    void equipItem(Integer userId, Long userItemId);
//    void unequipItem(Integer userId, Long userItemId);
//    UserItem enhanceItem(Integer userId, Long userItemId);
//}

package com.echommo.service;

import com.echommo.entity.UserItem;
import java.util.List;

public interface InventoryService {
    // Thống nhất dùng Integer charId cho túi đồ và Long cho cường hóa để khớp Controller
    List<UserItem> getInventory(Integer charId);

    void equipItem(Long charId, Long userItemId);

    void unequipItem(Long charId, Long userItemId);

    UserItem enhanceItem(Long charId, Long userItemId);
}