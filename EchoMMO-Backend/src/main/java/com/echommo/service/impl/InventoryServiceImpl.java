package com.echommo.service.impl;

import com.echommo.entity.Character;
import com.echommo.entity.Item;
import com.echommo.entity.User;
import com.echommo.entity.UserItem;
import com.echommo.enums.Rarity;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.ItemRepository;
import com.echommo.repository.UserItemRepository;
import com.echommo.service.EquipmentService;
import com.echommo.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final UserItemRepository userItemRepo;
    private final ItemRepository itemRepo;
    private final CharacterRepository charRepo;
    private final EquipmentService equipmentService; // Để cường hóa (nếu cần logic phức tạp)

    @Override
    public List<UserItem> getInventory(Integer charId) {
        return userItemRepo.findByCharacter_CharIdOrderByAcquiredAtDesc(charId);
    }

    @Override
    @Transactional
    public void equipItem(Long charId, Long userItemId) {
        // Logic mặc đồ (giữ nguyên hoặc implement sau nếu chưa có)
    }

    @Override
    @Transactional
    public void unequipItem(Long charId, Long userItemId) {
        // Logic tháo đồ
    }

    @Override
    @Transactional
    public UserItem enhanceItem(Long charId, Long userItemId) {
        // Delegate sang EquipmentService cho gọn
        return equipmentService.enhanceItem(userItemId);
    }

    // [FIX] Implement hàm thêm đồ
    @Override
    @Transactional
    public void addItemToInventory(User user, Integer itemId, int quantity) {
        Character character = charRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        Item item = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found: " + itemId));

        // Kiểm tra xem đã có item này trong túi chưa (chỉ gộp nếu không phải đồ Equip hoặc logic game cho phép)
        // Ở đây giả sử nguyên liệu (Material) thì gộp, còn trang bị thì tách riêng.
        // Nhưng đơn giản nhất là tìm kiếm item cùng loại chưa equip.

        Optional<UserItem> existingItem = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), itemId)
                .stream()
                .filter(ui -> !Boolean.TRUE.equals(ui.getIsEquipped())) // Chỉ gộp vào đồ chưa mặc
                .findFirst();

        if (existingItem.isPresent()) {
            UserItem ui = existingItem.get();
            ui.setQuantity(ui.getQuantity() + quantity);
            userItemRepo.save(ui);
        } else {
            UserItem ui = UserItem.builder()
                    .character(character)
                    .item(item)
                    .quantity(quantity)
                    .isEquipped(false)
                    .enhancementLevel(0)
                    .rarity(Rarity.COMMON) // Mặc định Common, logic rơi đồ xịn xử lý chỗ khác
                    .acquiredAt(LocalDateTime.now())
                    .build();
            userItemRepo.save(ui);
        }
    }
}