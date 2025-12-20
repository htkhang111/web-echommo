package com.echommo.controller;

import com.echommo.entity.Item;
import com.echommo.entity.User;
import com.echommo.enums.NotificationType;
import com.echommo.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // --- STATS ---
    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    // --- USER MANAGEMENT ---
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/user/ban/{id}")
    public ResponseEntity<?> banUser(@PathVariable Integer id, @RequestBody Map<String, String> payload) {
        adminService.banUser(id, payload.get("reason"));
        return ResponseEntity.ok("User banned successfully");
    }

    @PostMapping("/user/unban/{id}")
    public ResponseEntity<?> unbanUser(@PathVariable Integer id) {
        adminService.unbanUser(id);
        return ResponseEntity.ok("User unbanned successfully");
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // --- ITEM MANAGEMENT ---
    @GetMapping("/items")
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(adminService.getAllItems());
    }

    @PostMapping("/item/create")
    public ResponseEntity<?> createItem(@RequestBody Item item) {
        return ResponseEntity.ok(adminService.createItem(item));
    }

    @DeleteMapping("/item/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Integer id) {
        adminService.deleteItem(id);
        return ResponseEntity.ok("Item deleted successfully");
    }

    // --- REWARDS & ACTIONS ---
    @PostMapping("/grant-gold")
    public ResponseEntity<?> grantGold(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        Number amountNum = (Number) payload.get("amount");
        Long amount = amountNum.longValue();

        adminService.grantGold(username, amount);
        return ResponseEntity.ok("Gold granted successfully");
    }

    // [NEW] API Ban thưởng EchoCoin
    @PostMapping("/grant-echo")
    public ResponseEntity<?> grantEcho(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        // Chuyển đổi an toàn sang BigDecimal
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());

        adminService.grantEcho(username, amount);
        return ResponseEntity.ok("EchoCoin granted successfully");
    }

    @PostMapping("/grant-item")
    public ResponseEntity<?> grantItem(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        Integer itemId = (Integer) payload.get("itemId");
        Integer quantity = (Integer) payload.get("quantity");

        adminService.grantItem(username, itemId, quantity);
        return ResponseEntity.ok("Item granted successfully");
    }

    @PostMapping("/notification/create")
    public ResponseEntity<?> sendNotification(@RequestBody Map<String, String> payload) {
        String title = payload.get("title");
        String message = payload.get("message");
        String typeStr = payload.get("type");
        String recipient = payload.get("recipientUsername");

        NotificationType type = NotificationType.INFO;
        try {
            type = NotificationType.valueOf(typeStr);
        } catch (Exception e) {
            // default to INFO
        }

        adminService.createNotification(recipient, title, message, type);
        return ResponseEntity.ok("Notification sent");
    }

    @PostMapping("/give-reward")
    public ResponseEntity<?> giveReward(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok("Use new endpoints /grant-gold, /grant-echo or /grant-item");
    }
}