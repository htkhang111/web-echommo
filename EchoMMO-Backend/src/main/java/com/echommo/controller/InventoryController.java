package com.echommo.controller;

import com.echommo.entity.Character;
import com.echommo.entity.User;
import com.echommo.entity.UserItem;
import com.echommo.enums.SlotType;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.UserItemRepository;
import com.echommo.repository.UserRepository;
import com.echommo.service.EquipmentService;
import com.echommo.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/equipment") // Đường dẫn chuẩn: /api/equipment
@CrossOrigin(origins = "http://localhost:5173")
public class InventoryController {

    @Autowired private UserItemRepository userItemRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private CharacterRepository charRepo;
    @Autowired private InventoryService inventoryService;
    @Autowired private EquipmentService equipmentService;

    private Character getCurrentCharacter() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy User."));
        return charRepo.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Bạn chưa tạo nhân vật!"));
    }

    // 1. Lấy túi đồ (/api/equipment/inventory)
    @GetMapping("/inventory")
    public ResponseEntity<List<UserItem>> getInventory() {
        Character character = getCurrentCharacter();
        return ResponseEntity.ok(userItemRepo.findByCharacter_CharIdOrderByAcquiredAtDesc(character.getCharId()));
    }

    // 2. Mặc trang bị
    @PostMapping("/equip/{userItemId}")
    @Transactional
    public ResponseEntity<?> equipItem(@PathVariable Long userItemId) {
        Character character = getCurrentCharacter();
        UserItem newItem = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Item không tồn tại"));

        if (!newItem.getCharacter().getCharId().equals(character.getCharId())) {
            return ResponseEntity.badRequest().body("Vật phẩm không thuộc về nhân vật này!");
        }

        SlotType slot = newItem.getItem().getSlotType();
        if (slot == null || slot == SlotType.CONSUMABLE || slot == SlotType.MATERIAL) {
            return ResponseEntity.badRequest().body("Không thể trang bị vật phẩm này!");
        }

        Optional<UserItem> currentEquip = userItemRepo.findEquippedItemBySlot(character.getCharId(), slot);
        if (currentEquip.isPresent()) {
            UserItem old = currentEquip.get();
            old.setIsEquipped(false);
            userItemRepo.save(old);
        }

        newItem.setIsEquipped(true);
        userItemRepo.save(newItem);
        return ResponseEntity.ok("Đã trang bị: " + newItem.getItem().getName());
    }

    // 3. Tháo trang bị
    @PostMapping("/unequip/{userItemId}")
    @Transactional
    public ResponseEntity<?> unequipItem(@PathVariable Long userItemId) {
        Character character = getCurrentCharacter();
        UserItem item = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Item không tồn tại"));

        if (!item.getCharacter().getCharId().equals(character.getCharId())) {
            return ResponseEntity.badRequest().body("Vật phẩm không thuộc về bạn!");
        }

        item.setIsEquipped(false);
        userItemRepo.save(item);
        return ResponseEntity.ok("Đã tháo trang bị.");
    }

    // 4. Dùng vật phẩm
    @PostMapping("/use/{userItemId}")
    @Transactional
    public ResponseEntity<?> useItem(@PathVariable Long userItemId) {
        Character character = getCurrentCharacter();
        UserItem uItem = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Item không tồn tại"));

        if (!uItem.getCharacter().getCharId().equals(character.getCharId())) {
            return ResponseEntity.badRequest().body("Lỗi sở hữu vật phẩm!");
        }

        if (uItem.getItem().getSlotType() != SlotType.CONSUMABLE) {
            return ResponseEntity.badRequest().body("Không thể sử dụng!");
        }

        int heal = 50;
        if (uItem.getItem().getName().toLowerCase().contains("cao cấp")) heal = 200;

        character.setCurrentHp(Math.min(character.getMaxHp(), character.getCurrentHp() + heal));
        charRepo.save(character);

        uItem.setQuantity(uItem.getQuantity() - 1);
        if (uItem.getQuantity() <= 0) userItemRepo.delete(uItem);
        else userItemRepo.save(uItem);

        return ResponseEntity.ok("Đã dùng vật phẩm. HP hiện tại: " + character.getCurrentHp());
    }

    // 5. Cường hóa
    @PostMapping("/enhance")
    public ResponseEntity<?> enhance(@RequestParam Long userItemId) {
        try {
            return ResponseEntity.ok(inventoryService.enhanceItem(getCurrentCharacter().getCharId(), userItemId));
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    // 6. Mythic
    @PostMapping("/mythic/evolve")
    public ResponseEntity<?> evolve(@RequestParam Long userItemId) {
        try {
            return ResponseEntity.ok(equipmentService.upgradeToMythic(userItemId));
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/mythic/upgrade")
    public ResponseEntity<?> upgradeMythic(@RequestParam Long userItemId) {
        try {
            return ResponseEntity.ok(equipmentService.enhanceMythic(userItemId));
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }
}