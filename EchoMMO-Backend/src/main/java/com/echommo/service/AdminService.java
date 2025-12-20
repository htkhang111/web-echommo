package com.echommo.service;

import com.echommo.entity.*;
import com.echommo.enums.NotificationType;
import com.echommo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final NotificationRepository notificationRepository;

    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalItems", itemRepository.count()); // Thống kê tổng Item

        // [FIX] Handle null EchoCoin trong reduce
        BigDecimal totalEcho = walletRepository.findAll().stream()
                .map(w -> w.getEchoCoin() != null ? w.getEchoCoin() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalGold = walletRepository.findAll().stream()
                .mapToLong(Wallet::getGold)
                .sum();

        stats.put("totalEchoMined", totalEcho);
        stats.put("totalGold", totalGold); // Rename key to match frontend expectation if needed, or keep both
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
        // Có thể lưu reason vào log hoặc note nếu User entity hỗ trợ
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

    // --- REWARDS & NOTIFICATIONS ---

    @Transactional
    public void grantGold(String username, Long amount) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setGold(wallet.getGold() + amount);
        walletRepository.save(wallet);

        // Notify user
        createNotification(user.getUsername(), "Thưởng Hệ Thống",
                "Bạn nhận được " + amount + " vàng từ ban quản trị.", NotificationType.REWARD);
    }

    @Transactional
    public void grantItem(String username, Integer itemId, int quantity) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // Add item to user inventory logic (Simple version)
        // Check existing stack or create new
        UserItem userItem = userItemRepository.findByUserAndItem(user, item)
                .orElse(new UserItem());

        if (userItem.getId() == null) {
            userItem.setUser(user);
            userItem.setItem(item);
            userItem.setQuantity(0);
            userItem.setIsEquipped(false);
        }
        userItem.setQuantity(userItem.getQuantity() + quantity);
        userItemRepository.save(userItem);

        createNotification(user.getUsername(), "Thưởng Vật Phẩm",
                "Bạn nhận được " + quantity + "x " + item.getName() + " từ ban quản trị.", NotificationType.REWARD);
    }

    @Transactional
    public void createNotification(String recipientUsername, String title, String message, NotificationType type) {
        if (recipientUsername == null || recipientUsername.isEmpty()) {
            // Global notification logic here (requires implementation in NotificationService/Repo usually)
            // For now, loop all users (inefficient but works for small scale) or handle differently
            List<User> allUsers = userRepository.findAll();
            allUsers.forEach(u -> saveNotification(u, title, message, type));
        } else {
            User user = userRepository.findByUsername(recipientUsername)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            saveNotification(user, title, message, type);
        }
    }

    private void saveNotification(User user, String title, String message, NotificationType type) {
        Notification noti = new Notification();
        noti.setUser(user);
        noti.setTitle(title);
        noti.setMessage(message);
        noti.setType(type);
        noti.setIsRead(false);
        noti.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(noti);
    }
}