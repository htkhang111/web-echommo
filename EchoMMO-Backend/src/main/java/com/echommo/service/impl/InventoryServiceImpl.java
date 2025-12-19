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
        // Lấy danh sách vật phẩm, ưu tiên đồ mới nhặt lên đầu
        return userItemRepo.findByCharacter_CharIdOrderByAcquiredAtDesc(charId);
    }

    @Override
    public void equipItem(Long charId, Long userItemId) {
        // Logic mặc đồ (Đã xử lý ở EquipmentService/Controller, có thể để trống hoặc implement nếu cần)
    }

    @Override
    public void unequipItem(Long charId, Long userItemId) {
        // Logic tháo đồ (Đã xử lý ở EquipmentService/Controller)
    }

    @Override
    @Transactional
    public UserItem enhanceItem(Long charId, Long userItemId) {
        UserItem item = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại!"));

        // Kiểm tra chủ sở hữu (Convert charId sang int/long cho khớp type DB)
        if (item.getCharacter().getCharId().longValue() != charId) {
            throw new RuntimeException("Vật phẩm không thuộc về bạn!");
        }

        SlotType type = item.getItem().getSlotType();
        if (type == SlotType.CONSUMABLE || type == SlotType.MATERIAL || type == SlotType.NONE) {
            throw new RuntimeException("Vật phẩm này không thể cường hóa!");
        }

        // Logic cường hóa: Tỉ lệ thành công = 100 - (Level * 10), min 10%
        int currentLevel = item.getEnhanceLevel() != null ? item.getEnhanceLevel() : 0;
        int successRate = 100 - (currentLevel * 10);
        if (successRate < 10) successRate = 10;

        Random random = new Random();
        if (random.nextInt(100) < successRate) {
            item.setEnhanceLevel(currentLevel + 1);
            return userItemRepo.save(item);
        } else {
            throw new RuntimeException("Cường hóa thất bại! (Tỉ lệ: " + successRate + "%)");
        }
    }
}