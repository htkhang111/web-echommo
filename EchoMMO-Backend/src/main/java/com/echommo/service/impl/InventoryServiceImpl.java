//package com.echommo.service.impl;
//
//import com.echommo.entity.UserItem;
//import com.echommo.entity.Character;
//import com.echommo.repository.UserItemRepository;
//import com.echommo.repository.CharacterRepository;
//import com.echommo.service.InventoryService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.util.List;
//import java.util.Random;
//
//@Service
//public class InventoryServiceImpl implements InventoryService {
//
//    @Autowired private UserItemRepository userItemRepository;
//    @Autowired private CharacterRepository characterRepository;
//
//    private Character getChar(Integer userId) {
//        // [FIX LỖI] Xóa .longValue() đi. Truyền thẳng Integer vào vì Repository yêu cầu Integer.
//        return characterRepository.findByUser_UserId(userId)
//                .orElseThrow(() -> new RuntimeException("Chưa tạo nhân vật"));
//    }
//
//    @Override
//    public List<UserItem> getInventory(Integer userId) {
//        Character c = getChar(userId);
//        return userItemRepository.findByCharacter_CharIdOrderByAcquiredAtDesc(c.getCharId());
//    }
//
//    @Override
//    @Transactional
//    public void equipItem(Integer userId, Long userItemId) {
//        Character c = getChar(userId);
//        UserItem item = userItemRepository.findByUserItemIdAndCharacter_CharId(userItemId, c.getCharId())
//                .orElseThrow(() -> new RuntimeException("Đồ không tồn tại"));
//
//        // Kiểm tra xem Item có slot type không (tránh lỗi NullPointerException)
//        if (item.getItem().getSlotType() != null) {
//            userItemRepository.findEquippedItemBySlot(c.getCharId(), item.getItem().getSlotType())
//                    .ifPresent(old -> {
//                        old.setIsEquipped(false);
//                        userItemRepository.save(old);
//                    });
//        }
//
//        item.setIsEquipped(true);
//        userItemRepository.save(item);
//    }
//
//    @Override
//    @Transactional
//    public void unequipItem(Integer userId, Long userItemId) {
//        Character c = getChar(userId);
//        UserItem item = userItemRepository.findByUserItemIdAndCharacter_CharId(userItemId, c.getCharId())
//                .orElseThrow(() -> new RuntimeException("Đồ không tồn tại"));
//        item.setIsEquipped(false);
//        userItemRepository.save(item);
//    }
//
//    @Override
//    @Transactional
//    public UserItem enhanceItem(Integer userId, Long userItemId) {
//        Character c = getChar(userId);
//        UserItem item = userItemRepository.findByUserItemIdAndCharacter_CharId(userItemId, c.getCharId())
//                .orElseThrow(() -> new RuntimeException("Đồ không tồn tại"));
//
//        int current = item.getEnhancementLevel();
//        // Tỉ lệ thành công giảm dần theo cấp độ: 100% - (Level * 10)%
//        // Ví dụ: Level 0 lên 1 (100%), Level 5 lên 6 (50%)
//        if (new Random().nextInt(100) < (100 - current * 10)) {
//            item.setEnhancementLevel(current + 1);
//        } else {
//            throw new RuntimeException("Cường hóa thất bại!");
//        }
//        return userItemRepository.save(item);
//    }
//}

package com.echommo.service.impl;

import com.echommo.entity.UserItem;
import com.echommo.enums.SlotType;
import com.echommo.repository.UserItemRepository;
import com.echommo.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private UserItemRepository userItemRepo;

    @Override
    public List<UserItem> getInventory(Integer charId) {
        // [FIX] Gọi hàm có sắp xếp để túi đồ hiện vật phẩm mới nhất lên đầu
        return userItemRepo.findByCharacter_CharIdOrderByAcquiredAtDesc(charId);
    }

    @Override
    public void equipItem(Long charId, Long userItemId) {
        // Logic mặc đồ (hiện đang xử lý ở Controller)
    }

    @Override
    public void unequipItem(Long charId, Long userItemId) {
        // Logic tháo đồ (hiện đang xử lý ở Controller)
    }

    @Override
    @Transactional
    public UserItem enhanceItem(Long charId, Long userItemId) {
        UserItem item = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại!"));

        // So sánh charId (Long) với item.character.charId (Integer)
        if (item.getCharacter().getCharId().longValue() != charId) {
            throw new RuntimeException("Vật phẩm không thuộc về bạn!");
        }

        SlotType type = item.getItem().getSlotType();
        if (type == SlotType.CONSUMABLE || type == SlotType.MATERIAL) {
            throw new RuntimeException("Vật phẩm này không thể cường hóa!");
        }

        // Logic cường hóa: thành công tăng cấp, thất bại báo lỗi
        int currentLevel = item.getLevel();
        int successRate = 100 - (currentLevel * 10);
        if (successRate < 10) successRate = 10;

        Random random = new Random();
        if (random.nextInt(100) < successRate) {
            item.setLevel(currentLevel + 1);
            userItemRepo.save(item);
            return item;
        } else {
            throw new RuntimeException("Cường hóa thất bại! (Tỉ lệ: " + successRate + "%)");
        }
    }
}