package com.echommo.service;

import com.echommo.entity.*;
import com.echommo.entity.Character;
import com.echommo.enums.NotificationType;
import com.echommo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final CharacterRepository characterRepository;
    private final NotificationService notificationService;

    // Inject PasswordEncoder để mã hóa mật khẩu khi admin đổi pass cho user
    private final PasswordEncoder passwordEncoder;

    // Các Repo để xóa dữ liệu liên quan
    private final MarketListingRepository marketListingRepository;
    private final FriendshipRepository friendshipRepository;
    private final MessageRepository messageRepository;
    private final PrivateMessageRepository privateMessageRepository;
    private final DailyQuestRepository dailyQuestRepository;
    private final NotificationRepository notificationRepository;
    private final BattleSessionRepository battleSessionRepository;
    private final PvpQueueRepository pvpQueueRepository;
    private final PvpMatchRepository pvpMatchRepository;

    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalItems", itemRepository.count());

        BigDecimal totalEcho = walletRepository.findAll().stream()
                .map(w -> w.getEchoCoin() != null ? w.getEchoCoin() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGold = walletRepository.findAll().stream()
                .map(Wallet::getGold)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalEchoMined", totalEcho);
        stats.put("totalGold", totalGold);
        return stats;
    }

    // --- USER MANAGEMENT ---
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // [NEW] Hàm cập nhật thông tin User
    @Transactional
    public void updateUser(Integer userId, Map<String, Object> payload) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (payload.containsKey("email")) {
            user.setEmail((String) payload.get("email"));
        }
        if (payload.containsKey("avatarUrl")) {
            user.setAvatarUrl((String) payload.get("avatarUrl"));
        }
        // Xử lý đổi mật khẩu nếu có
        if (payload.containsKey("newPassword")) {
            String newPass = (String) payload.get("newPassword");
            if (newPass != null && !newPass.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(newPass));
            }
        }
        userRepository.save(user);
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // 1. Dọn dẹp dữ liệu liên quan đến Character
        if (user.getCharacter() != null) {
            Character character = user.getCharacter();
            Integer charId = character.getCharId();

            userItemRepository.findAll().stream()
                    .filter(ui -> ui.getCharacter().getCharId().equals(charId))
                    .forEach(userItemRepository::delete);

            battleSessionRepository.findAll().stream()
                    .filter(bs -> bs.getCharacter().getCharId().equals(charId))
                    .forEach(battleSessionRepository::delete);

            pvpQueueRepository.findAll().stream()
                    .filter(pq -> pq.getCharId().equals(charId))
                    .forEach(pvpQueueRepository::delete);

            pvpMatchRepository.findAll().stream()
                    .filter(pm -> (pm.getPlayer1() != null && pm.getPlayer1().getCharId().equals(charId)) ||
                            (pm.getPlayer2() != null && pm.getPlayer2().getCharId().equals(charId)))
                    .forEach(pvpMatchRepository::delete);
        }

        // 2. Dọn dẹp dữ liệu liên quan đến User
        marketListingRepository.findAll().stream()
                .filter(ml -> ml.getSeller().getUserId().equals(userId))
                .forEach(marketListingRepository::delete);

        friendshipRepository.findAll().stream()
                .filter(f -> f.getRequester().getUserId().equals(userId) || f.getAddressee().getUserId().equals(userId))
                .forEach(friendshipRepository::delete);

        messageRepository.findAll().stream()
                .filter(m -> m.getSender().getUserId().equals(userId))
                .forEach(messageRepository::delete);

        privateMessageRepository.findAll().stream()
                .filter(pm -> pm.getSender().getUserId().equals(userId) || pm.getReceiver().getUserId().equals(userId))
                .forEach(privateMessageRepository::delete);

        dailyQuestRepository.findAll().stream()
                .filter(dq -> dq.getUser().getUserId().equals(userId))
                .forEach(dailyQuestRepository::delete);

        notificationRepository.findAll().stream()
                .filter(n -> n.getUser().getUserId().equals(userId))
                .forEach(notificationRepository::delete);

        // 3. Xóa User
        userRepository.delete(user);
    }

    // --- ITEM MANAGEMENT ---
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    // [NEW] Hàm cập nhật Item
    @Transactional
    public Item updateItem(Integer itemId, Item itemDetails) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found: " + itemId));

        // Update basic info
        item.setName(itemDetails.getName());
        item.setDescription(itemDetails.getDescription());
        item.setBasePrice(itemDetails.getBasePrice());
        item.setImageUrl(itemDetails.getImageUrl());

        // Update stats
        item.setAtkBonus(itemDetails.getAtkBonus());
        item.setDefBonus(itemDetails.getDefBonus());
        item.setHpBonus(itemDetails.getHpBonus());
        item.setSpeedBonus(itemDetails.getSpeedBonus());

        // Update tool stats
        item.setMaxDurability(itemDetails.getMaxDurability());
        item.setMaxLuck(itemDetails.getMaxLuck());

        return itemRepository.save(item);
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

        wallet.setGold(wallet.getGold().add(BigDecimal.valueOf(amount)));
        walletRepository.save(wallet);

        createNotification(user.getUsername(), "Thưởng Hệ Thống",
                "Bạn nhận được " + amount + " vàng từ ban quản trị.", NotificationType.REWARD);
    }

    @Transactional
    public void grantEcho(String username, BigDecimal amount) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        BigDecimal currentEcho = wallet.getEchoCoin() != null ? wallet.getEchoCoin() : BigDecimal.ZERO;
        wallet.setEchoCoin(currentEcho.add(amount));
        walletRepository.save(wallet);

        createNotification(user.getUsername(), "Thưởng EchoCoin",
                "Bạn nhận được " + amount + " EchoCoin từ ban quản trị.", NotificationType.REWARD);
    }

    @Transactional
    public void grantItem(String username, Integer itemId, int quantity) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Character character = characterRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User chưa tạo nhân vật, không thể nhận đồ!"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        UserItem userItem = userItemRepository.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), itemId)
                .orElse(new UserItem());

        if (userItem.getUserItemId() == null) {
            userItem.setCharacter(character);
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