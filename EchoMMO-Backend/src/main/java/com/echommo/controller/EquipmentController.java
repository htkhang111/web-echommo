package com.echommo.controller;

import com.echommo.entity.Character;
import com.echommo.entity.User;
import com.echommo.entity.UserItem;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.UserRepository;
import com.echommo.service.EquipmentService;
import com.echommo.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@CrossOrigin(origins = "http://localhost:3000")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService; // Service xử lý Mythic (nếu có)

    @Autowired
    private InventoryService inventoryService; // Service xử lý Inventory, Enhance, Equip

    @Autowired private UserRepository userRepo;
    @Autowired private CharacterRepository charRepo;

    // --- HELPER: Tự động lấy Character của người đang đăng nhập ---
    private Character getCurrentCharacter() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Mặc định lấy nhân vật đầu tiên (hoặc logic chọn char của bạn)
        return charRepo.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Bạn chưa tạo nhân vật! Hãy tạo nhân vật trước."));
    }

    // ==========================================
    // 1. CÁC API CƠ BẢN (Fix lỗi 500 Frontend)
    // ==========================================

    // API: Lấy danh sách túi đồ
    // GET /api/equipment/inventory
    @GetMapping("/inventory")
    public ResponseEntity<List<UserItem>> getInventory() {
        Character character = getCurrentCharacter();
        return ResponseEntity.ok(inventoryService.getInventory(character.getCharId()));
    }

    // API: Mặc đồ
    // POST /api/equipment/equip/123
    @PostMapping("/equip/{userItemId}")
    public ResponseEntity<String> equip(@PathVariable Long userItemId) {
        Character character = getCurrentCharacter();
        inventoryService.equipItem(character.getCharId(), userItemId);
        return ResponseEntity.ok("Mặc trang bị thành công!");
    }

    // API: Tháo đồ
    // POST /api/equipment/unequip/123
    @PostMapping("/unequip/{userItemId}")
    public ResponseEntity<String> unequip(@PathVariable Long userItemId) {
        Character character = getCurrentCharacter();
        inventoryService.unequipItem(character.getCharId(), userItemId);
        return ResponseEntity.ok("Tháo trang bị thành công!");
    }

    // ==========================================
    // 2. CÁC API NÂNG CẤP (Enhance / Mythic)
    // ==========================================

    // API: Cường hóa thường
    // POST /api/equipment/enhance?userItemId=123
    @PostMapping("/enhance")
    public ResponseEntity<UserItem> enhance(@RequestParam Long userItemId) {
        Character character = getCurrentCharacter();
        // SỬA LỖI: Truyền charId thay vì userId
        return ResponseEntity.ok(inventoryService.enhanceItem(character.getCharId(), userItemId));
    }

    // API: Tiến hóa lên Mythic (Phẩm đỏ)
    // POST /api/equipment/mythic/evolve?userItemId=123
    @PostMapping("/mythic/evolve")
    public ResponseEntity<UserItem> evolve(@RequestParam Long userItemId) {
        // Cần đảm bảo EquipmentService cũng đã update logic check CharId nếu cần
        return ResponseEntity.ok(equipmentService.upgradeToMythic(userItemId));
    }

    // API: Nâng cấp Mythic (Sao đỏ)
    // POST /api/equipment/mythic/upgrade?userItemId=123
    @PostMapping("/mythic/upgrade")
    public ResponseEntity<UserItem> upgradeMythic(@RequestParam Long userItemId) {
        return ResponseEntity.ok(equipmentService.enhanceMythic(userItemId));
    }
}