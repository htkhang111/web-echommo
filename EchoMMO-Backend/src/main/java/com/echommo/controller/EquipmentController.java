package com.echommo.controller;

import com.echommo.entity.User;
import com.echommo.entity.UserItem;
import com.echommo.repository.UserRepository;
import com.echommo.service.EquipmentService;
import com.echommo.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    @Autowired private EquipmentService equipmentService;
    @Autowired private InventoryService inventoryService;
    @Autowired private UserRepository userRepository;

    private Integer getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getUserId();
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<UserItem>> getMyInventory() {
        try {
            return ResponseEntity.ok(inventoryService.getInventory(getCurrentUserId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/equip/{userItemId}")
    public ResponseEntity<?> equipItem(@PathVariable Long userItemId) {
        try {
            inventoryService.equipItem(getCurrentUserId(), userItemId);
            return ResponseEntity.ok("Đã trang bị vật phẩm thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/unequip/{userItemId}")
    public ResponseEntity<?> unequipItem(@PathVariable Long userItemId) {
        try {
            inventoryService.unequipItem(getCurrentUserId(), userItemId);
            return ResponseEntity.ok("Đã tháo trang bị!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/enhance/{userItemId}")
    public ResponseEntity<?> enhanceItem(@PathVariable Long userItemId) {
        try {
            return ResponseEntity.ok(inventoryService.enhanceItem(getCurrentUserId(), userItemId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/upgrade/{userItemId}")
    public ResponseEntity<?> upgradeItem(@PathVariable Long userItemId) {
        try {
            UserItem updatedItem = equipmentService.upgradeItem(userItemId, getCurrentUserId());
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/evolve-mythic/{userItemId}")
    public ResponseEntity<String> evolveToMythic(@PathVariable Long userItemId) {
        try {
            String result = equipmentService.evolveToMythic(userItemId, getCurrentUserId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/upgrade-mythic/{userItemId}")
    public ResponseEntity<?> upgradeMythic(@PathVariable Long userItemId) {
        try {
            UserItem updatedItem = equipmentService.upgradeMythicLevel(userItemId);
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
