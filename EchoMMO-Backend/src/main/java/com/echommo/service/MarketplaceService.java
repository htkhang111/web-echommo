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

    // --- SYSTEM SHOP ---
    public List<Item> getShopItems() {
        return itemRepo.findAll();
    }

    @Transactional
    public String buyItem(Integer itemId, Integer qty) {
        User u = getCurrentUser();
        Item i = itemRepo.findById(itemId).orElseThrow(() -> new RuntimeException("Item không tồn tại"));

        BigDecimal cost = BigDecimal.valueOf(i.getBasePrice()).multiply(BigDecimal.valueOf(qty));

        if (u.getWallet() == null) throw new RuntimeException("Lỗi dữ liệu ví tiền");
        if (u.getWallet().getGold() == null) u.getWallet().setGold(0L);

        // Gold là Long, Cost là BigDecimal -> So sánh
        if (BigDecimal.valueOf(u.getWallet().getGold()).compareTo(cost) < 0) {
            throw new RuntimeException("Không đủ vàng (Cần: " + cost + ")");
        }

        // Trừ tiền
        u.getWallet().setGold(u.getWallet().getGold() - cost.longValue());
        walletRepo.save(u.getWallet());

        deliverSystemItem(getMyChar(), i, qty);
        return "Mua thành công!";
    }

    @Transactional
    public String sellItem(Long userItemId, Integer qty) {
        Character myChar = getMyChar();
        // [FIX] Character dùng charId
        UserItem ui = uiRepo.findByUserItemIdAndCharacter_CharId(userItemId, myChar.getCharId())
                .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại hoặc không phải của bạn"));

        if (Boolean.TRUE.equals(ui.getIsEquipped())) throw new RuntimeException("Không thể bán đồ đang mặc");
        if (ui.getQuantity() < qty) throw new RuntimeException("Không đủ số lượng");

        BigDecimal earn = BigDecimal.valueOf(ui.getItem().getBasePrice())
                .multiply(new BigDecimal("0.5"))
                .multiply(BigDecimal.valueOf(qty));

        User u = myChar.getUser();
        if (u.getWallet() == null) throw new RuntimeException("Lỗi ví tiền");

        // Cộng tiền
        u.getWallet().setGold(u.getWallet().getGold() + earn.longValue());
        walletRepo.save(u.getWallet());

        if (ui.getQuantity() <= qty) uiRepo.delete(ui);
        else {
            ui.setQuantity(ui.getQuantity() - qty);
            uiRepo.save(ui);
        }
        return "Bán thành công, nhận " + earn.longValue() + " vàng";
    }

    // --- PLAYER MARKET ---
    @Transactional(readOnly = true)
    public List<MarketListing> getPlayerListings() {
        return listingRepo.findByStatusOrderByCreatedAtDesc("ACTIVE");
    }

    public List<MarketListing> getMyListings() {
        return listingRepo.findBySeller_UserIdAndStatus(getCurrentUser().getUserId(), "ACTIVE");
    }

    @Transactional
    public String createListing(CreateListingRequest req) {
        User u = getCurrentUser();
        Character myChar = getMyChar();

        // [FIX] Character dùng charId
        UserItem ui = uiRepo.findByUserItemIdAndCharacter_CharId(req.getUserItemId(), myChar.getCharId())
                .orElseThrow(() -> new RuntimeException("Vật phẩm lỗi"));

        if (Boolean.TRUE.equals(ui.getIsEquipped())) throw new RuntimeException("Phải tháo đồ trước khi bán");

        MarketListing ml = new MarketListing();
        ml.setSeller(u);
        ml.setItem(ui.getItem());
        ml.setUserItem(ui);

        // [FIX] Setters đã có nhờ @Data trong Entity mới
        ml.setQuantity(ui.getQuantity());
        ml.setPrice(req.getPrice()); // req.Price là BigDecimal -> Entity.Price là BigDecimal (OK)
        ml.setStatus("ACTIVE");
        ml.setCurrencyType("GOLD"); // Mặc định bán bằng vàng

        listingRepo.save(ml);
        return "Đã đăng bán";
    }

    @Transactional
    public String buyPlayerListing(Integer listingId, Integer qty) {
        User buyer = getCurrentUser();
        Character buyerChar = getMyChar();

        MarketListing l = listingRepo.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Tin không tồn tại"));

        // [FIX] Getter status đã có
        if (!"ACTIVE".equals(l.getStatus())) throw new RuntimeException("Đã bị người khác mua mất rồi!");
        if (l.getSeller().getUserId().equals(buyer.getUserId())) throw new RuntimeException("Không thể tự mua đồ của mình");

        BigDecimal total = l.getPrice();

        // Check tiền (Gold là Long)
        if (BigDecimal.valueOf(buyer.getWallet().getGold()).compareTo(total) < 0) {
            throw new RuntimeException("Không đủ vàng");
        }

        // Trừ tiền người mua
        buyer.getWallet().setGold(buyer.getWallet().getGold() - total.longValue());
        walletRepo.save(buyer.getWallet());

        // Cộng tiền người bán (Thuế 5%)
        User seller = l.getSeller();
        BigDecimal receive = total.multiply(new BigDecimal("0.95"));
        seller.getWallet().setGold(seller.getWallet().getGold() + receive.longValue());
        walletRepo.save(seller.getWallet());

        // Chuyển vật phẩm
        UserItem itemBeingSold = l.getUserItem();
        if (itemBeingSold != null) {
            itemBeingSold.setCharacter(buyerChar);
            itemBeingSold.setIsEquipped(false);
            uiRepo.save(itemBeingSold);
        } else {
            // Fallback (Logic tạo item mới nếu cần)
            deliverSystemItem(buyerChar, l.getItem(), l.getQuantity());
        }

        // [FIX] Setter status đã có
        l.setStatus("SOLD");
        listingRepo.save(l);
        return "Giao dịch thành công!";
    }

    @Transactional
    public String cancelListing(Integer id) {
        User u = getCurrentUser();
        MarketListing l = listingRepo.findById(id).orElseThrow(() -> new RuntimeException("Lỗi tin đăng"));
        if (!l.getSeller().getUserId().equals(u.getUserId())) throw new RuntimeException("Không phải của bạn");

        l.setStatus("CANCELLED");
        listingRepo.save(l);
        return "Đã thu hồi";
    }

    private void deliverSystemItem(Character c, Item item, int qty) {
        boolean isStackable = item.getSlotType() == SlotType.CONSUMABLE || item.getSlotType() == SlotType.NONE;

        // [FIX] Character dùng charId
        if (isStackable) {
            UserItem ui = uiRepo.findByCharacter_CharIdAndItem_ItemId(c.getCharId(), item.getItemId())
                    .orElse(UserItem.builder()
                            .character(c)
                            .item(item)
                            .quantity(0)
                            .isEquipped(false)
                            .enhancementLevel(0)
                            .build());
            ui.setQuantity(ui.getQuantity() + qty);
            uiRepo.save(ui);
        } else {
            for (int k = 0; k < qty; k++) {
                UserItem ui = UserItem.builder()
                        .character(c)
                        .item(item)
                        .quantity(1)
                        .isEquipped(false)
                        .enhancementLevel(0)
                        .build();
                uiRepo.save(ui);
            }
        }
    }
}