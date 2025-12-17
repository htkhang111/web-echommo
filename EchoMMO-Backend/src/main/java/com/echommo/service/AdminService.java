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

@Service
public class AdminService {
    @Autowired private UserRepository userRepo;
    @Autowired private ItemRepository itemRepo;
    @Autowired private WalletRepository walletRepo;
    @Autowired private UserItemRepository uiRepo;
    @Autowired private MarketListingRepository listingRepo;
    @Autowired private NotificationService notiService;
    @Autowired private CharacterRepository charRepo;

    public Map<String, Object> getServerStats() {
        Map<String, Object> m = new HashMap<>();
        m.put("totalUsers", userRepo.count());
        m.put("totalItems", itemRepo.count());
        BigDecimal totalGold = walletRepo.findAll().stream()
                .map(Wallet::getGold) // [LƯU Ý] Đảm bảo Wallet.java dùng field là 'gold'
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

    @Transactional
    public void deleteUser(Integer id) {
        // 1. Tìm user
        User u = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Tìm Character của User
        // [FIX LỖI] Xóa .longValue() đi vì Repository nhận Integer
        Character character = charRepo.findByUser_UserId(id).orElse(null);

        if (character != null) {
            // Lấy tất cả đồ của nhân vật đó để xóa
            List<UserItem> charItems = uiRepo.findByCharacter_CharIdOrderByAcquiredAtDesc(character.getCharId());
            if (charItems != null && !charItems.isEmpty()) {
                uiRepo.deleteAll(charItems);
            }
            // Xóa nhân vật
            charRepo.delete(character);
        }

        // 3. Xóa User
        userRepo.delete(u);
    }

    public void deleteListing(Integer id) { listingRepo.deleteById(id); }

    public void banUser(Integer id, String reason) {
        User u = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        u.setIsActive(false);
        u.setBanReason(reason);
        u.setBannedAt(LocalDateTime.now());
        userRepo.save(u);
    }

    public void unbanUser(Integer id) {
        User u = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
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
        // [LƯU Ý] Đảm bảo Wallet dùng field 'gold' (BigDecimal) thay vì 'balance'
        u.getWallet().setGold(u.getWallet().getGold().add(amt));
        walletRepo.save(u.getWallet());
        return "Done";
    }

    @Transactional
    public String grantItem(String uName, Integer iId, Integer qty) {
        // 1. Tìm User
        User u = userRepo.findByUsername(uName)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // 2. Tìm Character
        // [FIX LỖI] Xóa .longValue(), truyền thẳng Integer vào
        Character c = charRepo.findByUser_UserId(u.getUserId())
                .orElseThrow(() -> new RuntimeException("User này chưa tạo nhân vật!"));

        // 3. Tìm Item gốc
        Item i = itemRepo.findById(iId)
                .orElseThrow(() -> new RuntimeException("Item ID không tồn tại"));

        // 4. Kiểm tra stack số lượng
        UserItem ui = uiRepo.findByCharacter_CharIdAndItem_ItemId(c.getCharId(), iId).orElse(null);

        if (ui != null) {
            ui.setQuantity(ui.getQuantity() + qty);
        } else {
            // Tạo mới UserItem cho Character
            ui = new UserItem();
            ui.setCharacter(c);
            ui.setItem(i);
            ui.setQuantity(qty);
            ui.setIsEquipped(false);
            ui.setEnhancementLevel(0);
            ui.setAcquiredAt(LocalDateTime.now());

            ui.setRarity(Rarity.COMMON);
            ui.setSubStats("[]");
            ui.setMainStatValue(BigDecimal.ZERO);

            if (i.getSlotType() != null) {
                ui.setMainStatType("ATK_FLAT");
                ui.setMainStatValue(BigDecimal.valueOf(10));
            }
        }

        uiRepo.save(ui);
        return "Đã tặng " + qty + " x " + i.getName() + " cho " + uName;
    }

    @Transactional
    public void sendCustomNotification(Map<String, String> payload) {
        String title = payload.get("title");
        String message = payload.get("message");
        String type = payload.get("type");
        String recipient = payload.get("recipientUsername");

        if (recipient != null && !recipient.trim().isEmpty()) {
            User user = userRepo.findByUsername(recipient).orElseThrow();
            notiService.sendNotification(user, title, message, type);
        } else {
            List<User> allUsers = userRepo.findAll();
            for (User user : allUsers) {
                notiService.sendNotification(user, title, message, type);
            }
        }
    }
}