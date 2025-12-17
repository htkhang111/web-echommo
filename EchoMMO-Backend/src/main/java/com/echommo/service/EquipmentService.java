package com.echommo.service;

import com.echommo.entity.UserItem;
import com.echommo.repository.UserItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class EquipmentService {

    @Autowired private UserItemRepository userItemRepo;

    @Transactional
    public UserItem upgradeToMythic(Long userItemId) {
        UserItem item = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // Sửa lỗi: Sử dụng getEnhanceLevel() và setMythic()
        if (item.getEnhanceLevel() < 10) {
            throw new RuntimeException("Cần cường hóa +10 để lên Mythic");
        }

        item.setMythic(true);
        item.setMythicLevel(1);
        item.setOriginalMainStatValue(item.getMainStatValue());

        return userItemRepo.save(item);
    }

    @Transactional
    public UserItem enhanceMythic(Long userItemId) {
        UserItem item = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.isMythic()) {
            throw new RuntimeException("Vật phẩm chưa đạt cấp Mythic");
        }

        item.setMythicLevel(item.getMythicLevel() + 1);

        // Logic tăng chỉ số dựa trên giá trị gốc
        BigDecimal bonus = item.getOriginalMainStatValue().multiply(new BigDecimal("0.1"));
        item.setMainStatValue(item.getMainStatValue().add(bonus));

        return userItemRepo.save(item);
    }
}