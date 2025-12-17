package com.echommo.service;

import com.echommo.dto.CreateListingRequest;
import com.echommo.entity.*;
import com.echommo.entity.Character;
import com.echommo.enums.SlotType;
import com.echommo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MarketplaceService {

    @Autowired private ItemRepository itemRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private CharacterRepository charRepo;
    @Autowired private WalletRepository walletRepo;
    @Autowired private UserItemRepository uiRepo;
    @Autowired private MarketListingRepository listingRepo;

    private User getCurrentUser() {
        String u = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(u).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Character getMyChar() {
        User u = getCurrentUser();
        return charRepo.findByUser_UserId(u.getUserId())
                .orElseThrow(() -> new RuntimeException("Bạn chưa tạo nhân vật"));
    }

    // --- SHOP SYSTEM ---
    public List<Item> getShopItems() {
        return itemRepo.findAll().stream()
                .filter(i -> !"MATERIAL".equals(i.getType()))
                .collect(Collectors.toList());
    }

    @Transactional
    public String buyItem(Integer itemId, Integer qty) {
        User u = getCurrentUser();
        Item i = itemRepo.findById(itemId).orElseThrow(() -> new RuntimeException("Item không tồn tại"));

        if ("MATERIAL".equals(i.getType())) throw new RuntimeException("Vật phẩm này không còn bán.");

        BigDecimal cost = BigDecimal.valueOf(i.getBasePrice()).multiply(BigDecimal.valueOf(qty));
        // [FIX] Kiểm tra tiền kỹ càng hơn
        if (u.getWallet() == null || u.getWallet().getGold() == null) {
            throw new RuntimeException("Lỗi ví tiền.");
        }
        if (u.getWallet().getGold().compareTo(cost) < 0) throw new RuntimeException("Không đủ vàng (Cần: " + cost + ")");

        u.getWallet().setGold(u.getWallet().getGold().subtract(cost));
        walletRepo.save(u.getWallet());

        deliverSystemItem(getMyChar(), i, qty);
        return "Mua thành công!";
    }

    @Transactional
    public String sellItem(Long userItemId, Integer qty) {
        Character myChar = getMyChar();
        UserItem ui = uiRepo.findByUserItemIdAndCharacter_CharId(userItemId, myChar.getCharId())
                .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại"));

        if (Boolean.TRUE.equals(ui.getIsEquipped())) throw new RuntimeException("Không thể bán đồ đang mặc");
        if (ui.getQuantity() < qty) throw new RuntimeException("Không đủ số lượng");

        BigDecimal earn = BigDecimal.valueOf(ui.getItem().getBasePrice()).multiply(new BigDecimal("0.5")).multiply(BigDecimal.valueOf(qty));
        User u = myChar.getUser();
        u.getWallet().setGold(u.getWallet().getGold().add(earn));
        walletRepo.save(u.getWallet());

        if (ui.getQuantity() <= qty) uiRepo.delete(ui);
        else { ui.setQuantity(ui.getQuantity() - qty); uiRepo.save(ui); }
        return "Đã bán nhận " + earn + " vàng";
    }

    // --- PLAYER MARKET ---
    // [FIX QUAN TRỌNG] Thêm @Transactional(readOnly=true) để tránh lỗi Lazy Loading gây 500
    @Transactional(readOnly = true)
    public List<MarketListing> getPlayerListings() {
        List<MarketListing> allListings = listingRepo.findByStatusOrderByCreatedAtDesc("ACTIVE");
        // Lọc bằng Java để an toàn
        return allListings.stream()
                .filter(listing -> {
                    if (listing.getItem() == null) return false;
                    return !"MATERIAL".equals(listing.getItem().getType());
                })
                .collect(Collectors.toList());
    }

    public List<MarketListing> getMyListings() {
        return listingRepo.findBySeller_UserIdAndStatus(getCurrentUser().getUserId(), "ACTIVE");
    }

    @Transactional
    public String createListing(CreateListingRequest req) {
        User u = getCurrentUser();
        Character myChar = getMyChar();
        UserItem ui = uiRepo.findByUserItemIdAndCharacter_CharId(req.getUserItemId(), myChar.getCharId())
                .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại"));

        if ("MATERIAL".equals(ui.getItem().getType())) throw new RuntimeException("Chợ chỉ dành cho trang bị!");
        if (Boolean.TRUE.equals(ui.getIsEquipped())) throw new RuntimeException("Phải tháo đồ trước khi bán");

        MarketListing ml = new MarketListing();
        ml.setSeller(u); ml.setItem(ui.getItem()); ml.setUserItem(ui);
        ml.setQuantity(ui.getQuantity()); ml.setPrice(req.getPrice());
        ml.setEnhanceLevel(ui.getEnhancementLevel() != null ? ui.getEnhancementLevel() : 0);
        ml.setStatus("ACTIVE");
        listingRepo.save(ml);
        return "Đã đăng bán";
    }

    @Transactional
    public String buyPlayerListing(Integer listingId, Integer qty) {
        User buyer = getCurrentUser();
        Character buyerChar = getMyChar();
        MarketListing l = listingRepo.findById(listingId).orElseThrow(() -> new RuntimeException("Tin không tồn tại"));

        if (!"ACTIVE".equals(l.getStatus())) throw new RuntimeException("Vật phẩm đã được bán");
        if (l.getSeller().getUserId().equals(buyer.getUserId())) throw new RuntimeException("Không thể tự mua");

        BigDecimal total = l.getPrice();
        if (buyer.getWallet().getGold().compareTo(total) < 0) throw new RuntimeException("Không đủ vàng");

        buyer.getWallet().setGold(buyer.getWallet().getGold().subtract(total));
        walletRepo.save(buyer.getWallet());

        User seller = l.getSeller();
        BigDecimal receive = total.multiply(new BigDecimal("0.95"));
        seller.getWallet().setGold(seller.getWallet().getGold().add(receive));
        walletRepo.save(seller.getWallet());

        UserItem itemBeingSold = l.getUserItem();
        if (itemBeingSold == null) {
            deliverSystemItem(buyerChar, l.getItem(), l.getQuantity());
        } else {
            itemBeingSold.setCharacter(buyerChar);
            itemBeingSold.setIsEquipped(false);
            uiRepo.save(itemBeingSold);
        }
        l.setStatus("SOLD");
        listingRepo.save(l);
        return "Mua thành công!";
    }

    @Transactional
    public String cancelListing(Integer id) {
        User u = getCurrentUser();
        MarketListing l = listingRepo.findById(id).orElseThrow(() -> new RuntimeException("Tin lỗi"));
        if (!l.getSeller().getUserId().equals(u.getUserId())) throw new RuntimeException("Không chính chủ");
        if (!"ACTIVE".equals(l.getStatus())) throw new RuntimeException("Không thể hủy");
        l.setStatus("CANCELLED");
        listingRepo.save(l);
        return "Đã hủy bán.";
    }

    private void deliverSystemItem(Character c, Item item, int qty) {
        boolean isStackable = item.getSlotType() == SlotType.CONSUMABLE || item.getSlotType() == SlotType.NONE;
        if (isStackable) {
            UserItem ui = uiRepo.findByCharacter_CharIdAndItem_ItemId(c.getCharId(), item.getItemId())
                    .orElse(UserItem.builder().character(c).item(item).quantity(0).isEquipped(false).enhancementLevel(0).build());
            ui.setQuantity(ui.getQuantity() + qty);
            uiRepo.save(ui);
        } else {
            for (int k = 0; k < qty; k++) {
                UserItem ui = UserItem.builder().character(c).item(item).quantity(1).isEquipped(false).enhancementLevel(0).build();
                uiRepo.save(ui);
            }
        }
    }
}