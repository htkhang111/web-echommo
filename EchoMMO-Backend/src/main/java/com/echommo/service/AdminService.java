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
    private final CharacterRepository characterRepository;
    private final NotificationService notificationService;

    // [NEW] Inject thêm các Repo để xóa dữ liệu liên quan
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

        // [FIX] Tính tổng Gold bằng BigDecimal
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

        // 1. Dọn dẹp dữ liệu liên quan đến Character (nếu có)
        // Vì quan hệ OneToMany không được cascade triệt để trong code Entity, ta phải xóa tay
        if (user.getCharacter() != null) {
            Character character = user.getCharacter();
            Integer charId = character.getCharId();

            // Xóa UserItems
            userItemRepository.findAll().stream()
                    .filter(ui -> ui.getCharacter().getCharId().equals(charId))
                    .forEach(userItemRepository::delete);

            // Xóa BattleSession
            battleSessionRepository.findAll().stream()
                    .filter(bs -> bs.getCharacter().getCharId().equals(charId))
                    .forEach(battleSessionRepository::delete);

            // Xóa PVP Queue
            pvpQueueRepository.findAll().stream()
                    .filter(pq -> pq.getCharId().equals(charId))
                    .forEach(pvpQueueRepository::delete);

            // Xóa Lịch sử PVP
            pvpMatchRepository.findAll().stream()
                    .filter(pm -> (pm.getPlayer1() != null && pm.getPlayer1().getCharId().equals(charId)) ||
                            (pm.getPlayer2() != null && pm.getPlayer2().getCharId().equals(charId)))
                    .forEach(pvpMatchRepository::delete);
        }

        // 2. Dọn dẹp dữ liệu liên quan đến User
        // Xóa Market Listings
        marketListingRepository.findAll().stream()
                .filter(ml -> ml.getSeller().getUserId().equals(userId))
                .forEach(marketListingRepository::delete);

        // Xóa Friendships [FIX: Sử dụng getRequester().getUserId() thay vì getRequesterId()]
        friendshipRepository.findAll().stream()
                .filter(f -> f.getRequester().getUserId().equals(userId) || f.getAddressee().getUserId().equals(userId))
                .forEach(friendshipRepository::delete);

        // Xóa Chat Messages (Global)
        messageRepository.findAll().stream()
                .filter(m -> m.getSender().getUserId().equals(userId))
                .forEach(messageRepository::delete);

        // Xóa Private Messages [FIX: Sử dụng getSender().getUserId() thay vì getSenderId()]
        privateMessageRepository.findAll().stream()
                .filter(pm -> pm.getSender().getUserId().equals(userId) || pm.getReceiver().getUserId().equals(userId))
                .forEach(privateMessageRepository::delete);

        // Xóa Daily Quests [FIX: Sử dụng getUser().getUserId() thay vì getUserId()]
        dailyQuestRepository.findAll().stream()
                .filter(dq -> dq.getUser().getUserId().equals(userId))
                .forEach(dailyQuestRepository::delete);

        // Xóa Notifications
        notificationRepository.findAll().stream()
                .filter(n -> n.getUser().getUserId().equals(userId))
                .forEach(notificationRepository::delete);

        // 3. Cuối cùng xóa User (Sẽ tự cascade xóa Wallet và Character entity)
        userRepository.delete(user);
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

        // [FIX] Cộng Gold dùng BigDecimal
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