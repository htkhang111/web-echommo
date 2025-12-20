package com.echommo.service;

import com.echommo.entity.*;
import com.echommo.entity.Character;
import com.echommo.enums.NotificationType;
import com.echommo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final ItemRepository itemRepository;
    private final UserItemRepository userItemRepository;
    private final CharacterRepository characterRepository; // [NEW] Cần để tìm nhân vật
    private final NotificationService notificationService; // [NEW] Dùng service thay vì repo trực tiếp

    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalItems", itemRepository.count());

        BigDecimal totalEcho = walletRepository.findAll().stream()
                .map(w -> w.getEchoCoin() != null ? w.getEchoCoin() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalGold = walletRepository.findAll().stream()
                .mapToLong(Wallet::getGold)
                .sum();

        stats.put("totalEchoMined", totalEcho);
        stats.put("totalGold", totalGold);
        return stats;
    }

    // --- USER MANAGEMENT ---
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void banUser(Integer userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void unbanUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Integer userId) {
        userRepository.deleteById(userId);
    }

    // --- ITEM MANAGEMENT ---
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @Transactional
    public Item createItem(Item item) {
        return itemRepository.save(item);
    }

    @Transactional
    public void deleteItem(Integer itemId) {
        itemRepository.deleteById(itemId);
    }

    // --- REWARDS ---

    @Transactional
    public void grantGold(String username, Long amount) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setGold(wallet.getGold() + amount);
        walletRepository.save(wallet);

        createNotification(user.getUsername(), "Thưởng Hệ Thống",
                "Bạn nhận được " + amount + " vàng từ ban quản trị.", NotificationType.REWARD);
    }

    @Transactional
    public void grantItem(String username, Integer itemId, int quantity) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // [FIX] Item phải được add vào Character, không phải User trực tiếp
        Character character = characterRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User chưa tạo nhân vật, không thể nhận đồ!"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // [FIX] Tìm item trong túi đồ của nhân vật (dùng CharId)
        UserItem userItem = userItemRepository.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), itemId)
                .orElse(new UserItem());

        // [FIX] Sử dụng đúng tên hàm getter/setter của UserItem entity
        if (userItem.getUserItemId() == null) { // ID là userItemId, không phải Id
            userItem.setCharacter(character);   // Set Character, không phải setUser
            userItem.setItem(item);
            userItem.setQuantity(0);
            userItem.setIsEquipped(false);
        }

        userItem.setQuantity(userItem.getQuantity() + quantity);
        userItemRepository.save(userItem);

        createNotification(user.getUsername(), "Thưởng Vật Phẩm",
                "Bạn nhận được " + quantity + "x " + item.getName() + " từ ban quản trị.", NotificationType.REWARD);
    }

    public void createNotification(String recipientUsername, String title, String message, NotificationType type) {
        if (recipientUsername == null || recipientUsername.isEmpty()) {
            List<User> allUsers = userRepository.findAll();
            allUsers.forEach(u -> notificationService.sendNotification(u, title, message, type));
        } else {
            User user = userRepository.findByUsername(recipientUsername)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            notificationService.sendNotification(user, title, message, type);
        }
    }
}