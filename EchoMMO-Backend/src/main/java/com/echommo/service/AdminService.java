package com.echommo.service;

import com.echommo.entity.*;
import com.echommo.entity.Character;
import com.echommo.enums.Rarity;
import com.echommo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {
    @Autowired private UserRepository userRepo;
    @Autowired private ItemRepository itemRepo;
    @Autowired private WalletRepository walletRepo;
    @Autowired private UserItemRepository uiRepo;
    @Autowired private MarketListingRepository listingRepo;
    @Autowired private NotificationService notiService;
    @Autowired private NotificationRepository notiRepo;
    @Autowired private CharacterRepository charRepo;

    // Các Repo phụ trợ
    @Autowired private FriendshipRepository friendRepo;
    @Autowired private MessageRepository msgRepo;
    @Autowired private PrivateMessageRepository pmRepo;
    @Autowired private BattleSessionRepository battleRepo;

    public Map<String, Object> getServerStats() {
        Map<String, Object> m = new HashMap<>();
        m.put("totalUsers", userRepo.count());
        m.put("totalItems", itemRepo.count());
        BigDecimal totalGold = walletRepo.findAll().stream()
                .map(Wallet::getGold)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        m.put("totalGold", totalGold);
        return m;
    }

    public List<Item> getAllItems() { return itemRepo.findAll(); }
    public List<User> getAllUsers() { return userRepo.findAll(); }
    public List<MarketListing> getAllListings() { return listingRepo.findAll(); }

    public Item createItem(Item i) {
        if(i.getImageUrl()==null || i.getImageUrl().isEmpty()) i.setImageUrl("default_item");
        return itemRepo.save(i);
    }

    public void deleteItem(Integer id) { itemRepo.deleteById(id); }

    public void deleteListing(Integer id) { listingRepo.deleteById(id); }

    @Transactional
    public void deleteUser(Integer id) {
        User u = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Xóa Listing trên chợ
        listingRepo.deleteAll(listingRepo.findAll().stream()
                .filter(l -> l.getSeller().getUserId().equals(id))
                .collect(Collectors.toList()));

        // 2. Xóa Thông báo (Notification)
        notiRepo.deleteAll(notiRepo.findAll().stream()
                .filter(n -> n.getUser().getUserId().equals(id))
                .collect(Collectors.toList()));

        // 3. [SỬA LỖI] Xóa Bạn bè (Friendship) - Dùng getRequester() và getAddressee()
        friendRepo.deleteAll(friendRepo.findAll().stream()
                .filter(f -> f.getRequester().getUserId().equals(id) || f.getAddressee().getUserId().equals(id))
                .collect(Collectors.toList()));

        // 4. Xóa Tin nhắn riêng (PrivateMessage) - gửi và nhận
        pmRepo.deleteAll(pmRepo.findAll().stream()
                .filter(m -> m.getSender().getUserId().equals(id) || m.getReceiver().getUserId().equals(id))
                .collect(Collectors.toList()));

        // 5. Xóa Chat thế giới (Message)
        msgRepo.deleteAll(msgRepo.findAll().stream()
                .filter(m -> m.getSender().getUserId().equals(id))
                .collect(Collectors.toList()));

        // 6. Xóa Ví tiền (Wallet)
        if (u.getWallet() != null) {
            walletRepo.delete(u.getWallet());
        }

        // 7. Xử lý Nhân vật (Character) & Battle Session
        Character character = charRepo.findByUser_UserId(id).orElse(null);
        if (character != null) {
            // Xóa Battle Session nếu đang đánh quái
            battleRepo.deleteAll(battleRepo.findAll().stream()
                    .filter(b -> b.getCharacter().getCharId().equals(character.getCharId()))
                    .collect(Collectors.toList()));

            // Xóa Items của nhân vật
            uiRepo.deleteAll(uiRepo.findByCharacter_CharIdOrderByAcquiredAtDesc(character.getCharId()));

            // Xóa Nhân vật
            charRepo.delete(character);
        }

        // 8. Cuối cùng xóa User
        userRepo.delete(u);
    }

    public void banUser(Integer id, String reason) {
        User u = userRepo.findById(id).orElseThrow();
        u.setIsActive(false);
        u.setBanReason(reason);
        u.setBannedAt(LocalDateTime.now());
        userRepo.save(u);
    }

    public void unbanUser(Integer id) {
        User u = userRepo.findById(id).orElseThrow();
        u.setIsActive(true);
        u.setBanReason(null);
        u.setBannedAt(null);
        userRepo.save(u);
    }

    public void toggleUser(Integer id) {
        User u = userRepo.findById(id).orElseThrow();
        if(u.getIsActive()) banUser(id, "Khóa nhanh bởi Admin");
        else unbanUser(id);
    }

    @Transactional
    public String grantGold(String uName, BigDecimal amt) {
        User u = userRepo.findByUsername(uName).orElseThrow();
        u.getWallet().setGold(u.getWallet().getGold().add(amt));
        walletRepo.save(u.getWallet());
        return "Done";
    }

    @Transactional
    public String grantItem(String uName, Integer iId, Integer qty) {
        User u = userRepo.findByUsername(uName).orElseThrow();
        Character c = charRepo.findByUser_UserId(u.getUserId()).orElseThrow();
        Item i = itemRepo.findById(iId).orElseThrow();

        UserItem ui = uiRepo.findByCharacter_CharIdAndItem_ItemId(c.getCharId(), iId).orElse(null);
        if (ui != null) {
            ui.setQuantity(ui.getQuantity() + qty);
        } else {
            ui = new UserItem();
            ui.setCharacter(c); ui.setItem(i); ui.setQuantity(qty);
            ui.setIsEquipped(false); ui.setEnhancementLevel(0);
            ui.setAcquiredAt(LocalDateTime.now()); ui.setRarity(Rarity.COMMON);
            ui.setSubStats("[]"); ui.setMainStatValue(BigDecimal.ZERO);
        }
        uiRepo.save(ui);
        return "Done";
    }

    @Transactional
    public void sendCustomNotification(Map<String, String> payload) {
        String title = payload.get("title");
        String message = payload.get("message");
        String recipient = payload.get("recipientUsername");
        if (recipient != null && !recipient.trim().isEmpty()) {
            User user = userRepo.findByUsername(recipient).orElseThrow();
            notiService.sendNotification(user, title, message, "SYSTEM");
        } else {
            userRepo.findAll().forEach(user -> notiService.sendNotification(user, title, message, "SYSTEM"));
        }
    }
}