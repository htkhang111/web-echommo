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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    // [FIX] Dùng hàm findByUserId mới
    private Wallet getUserWallet(Integer userId) {
        return walletRepo.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Lỗi nghiêm trọng: Không tìm thấy ví tiền của User ID " + userId));
    }

    // --- SYSTEM SHOP ---
    public List<Item> getShopItems() {
        return itemRepo.findAll();
    }

    @Transactional
    public String buyItem(Integer itemId, Integer qty) {
        User u = getCurrentUser();
        Item i = itemRepo.findById(itemId).orElseThrow(() -> new RuntimeException("Item không tồn tại"));

        BigDecimal cost = i.getBasePrice().multiply(BigDecimal.valueOf(qty));
        Wallet w = getUserWallet(u.getUserId());

        // Debug log nếu cần thiết (in ra console server)
        System.out.println("User: " + u.getUsername() + " | Gold hiện tại: " + w.getGold() + " | Cần: " + cost);

        if (w.getGold().compareTo(cost) < 0) {
            throw new RuntimeException("Không đủ vàng (Cần: " + cost + ", Có: " + w.getGold() + ")");
        }

        w.setGold(w.getGold().subtract(cost));
        walletRepo.save(w);

        deliverSystemItem(getMyChar(), i, qty);
        return "Mua thành công!";
    }

    @Transactional
    public String sellItem(Long userItemId, Integer qty) {
        Character myChar = getMyChar();
        User u = myChar.getUser();

        UserItem ui = uiRepo.findByUserItemIdAndCharacter_CharId(userItemId, myChar.getCharId())
                .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại"));

        if (Boolean.TRUE.equals(ui.getIsEquipped())) throw new RuntimeException("Không thể bán đồ đang mặc");
        if (ui.getQuantity() < qty) throw new RuntimeException("Không đủ số lượng");

        BigDecimal earn = ui.getItem().getBasePrice()
                .multiply(new BigDecimal("0.5"))
                .multiply(BigDecimal.valueOf(qty));

        Wallet w = getUserWallet(u.getUserId());
        w.setGold(w.getGold().add(earn));
        walletRepo.save(w);

        if (ui.getQuantity() <= qty) uiRepo.delete(ui);
        else {
            ui.setQuantity(ui.getQuantity() - qty);
            uiRepo.save(ui);
        }
        return "Bán thành công, nhận " + earn + " vàng";
    }

    // --- PLAYER MARKET ---
    @Transactional(readOnly = true)
    public List<MarketListing> getPlayerListings() {
        return listingRepo.findByStatusOrderByListedAtDesc("ACTIVE");
    }

    public List<MarketListing> getMyListings() {
        return listingRepo.findBySeller_UserIdAndStatus(getCurrentUser().getUserId(), "ACTIVE");
    }

    @Transactional
    public String createListing(CreateListingRequest req) {
        User u = getCurrentUser();
        Character myChar = getMyChar();

        Long itemId = req.getUserItemId();
        int qtyToSell = (req.getQuantity() != null && req.getQuantity() > 0) ? req.getQuantity() : 1;

        UserItem ui = uiRepo.findByUserItemIdAndCharacter_CharId(itemId, myChar.getCharId())
                .orElseThrow(() -> new RuntimeException("Vật phẩm lỗi hoặc không tồn tại"));

        if (Boolean.TRUE.equals(ui.getIsEquipped())) throw new RuntimeException("Phải tháo đồ trước khi bán");
        if (ui.getQuantity() < qtyToSell) throw new RuntimeException("Không đủ số lượng!");

        // [QUAN TRỌNG] Nếu chạy SQL fix ở Bước 1, lỗi này sẽ biến mất
        if (Boolean.TRUE.equals(ui.getIsLocked())) throw new RuntimeException("Vật phẩm đang bị khóa (Hãy liên hệ Admin hoặc chạy lệnh fix DB)!");

        UserItem itemToSell;

        if (ui.getQuantity() > qtyToSell) {
            ui.setQuantity(ui.getQuantity() - qtyToSell);
            uiRepo.save(ui);

            itemToSell = UserItem.builder()
                    .item(ui.getItem())
                    .quantity(qtyToSell)
                    .character(myChar)
                    .isEquipped(false)
                    .isLocked(true)
                    .enhanceLevel(ui.getEnhanceLevel())
                    .rarity(ui.getRarity())
                    .mainStatType(ui.getMainStatType())
                    .mainStatValue(ui.getMainStatValue())
                    .originalMainStatValue(ui.getOriginalMainStatValue())
                    .subStats(ui.getSubStats())
                    .maxDurability(ui.getMaxDurability())
                    .currentDurability(ui.getCurrentDurability())
                    .isMythic(ui.getIsMythic())
                    .mythicStars(ui.getMythicStars())
                    .acquiredAt(LocalDateTime.now())
                    .build();
            itemToSell = uiRepo.save(itemToSell);
        } else {
            itemToSell = ui;
            itemToSell.setIsLocked(true);
            uiRepo.save(itemToSell);
        }

        MarketListing ml = new MarketListing();
        ml.setSeller(u);
        ml.setItem(itemToSell.getItem());
        ml.setUserItem(itemToSell);
        ml.setQuantity(qtyToSell);
        ml.setPrice(req.getPrice());
        ml.setStatus("ACTIVE");
        ml.setCurrencyType("GOLD");

        listingRepo.save(ml);
        return "Đã đăng bán";
    }

    @Transactional
    public String buyPlayerListing(Long listingId, Integer qty) {
        User buyer = getCurrentUser();
        Character buyerChar = getMyChar();

        MarketListing l = listingRepo.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Tin không tồn tại"));

        if (!"ACTIVE".equals(l.getStatus())) throw new RuntimeException("Đã bị người khác mua mất rồi!");
        if (l.getSeller().getUserId().equals(buyer.getUserId())) throw new RuntimeException("Không thể tự mua đồ của mình");

        BigDecimal total = l.getPrice();

        // [FIX] Lấy ví chuẩn xác từ DB
        Wallet buyerWallet = getUserWallet(buyer.getUserId());

        if (buyerWallet.getGold().compareTo(total) < 0) {
            throw new RuntimeException("Không đủ vàng (Cần: " + total + ", Có: " + buyerWallet.getGold() + ")");
        }

        buyerWallet.setGold(buyerWallet.getGold().subtract(total));
        walletRepo.save(buyerWallet);

        User seller = l.getSeller();
        Wallet sellerWallet = getUserWallet(seller.getUserId());

        BigDecimal receive = total.multiply(new BigDecimal("0.95"));
        sellerWallet.setGold(sellerWallet.getGold().add(receive));
        walletRepo.save(sellerWallet);

        UserItem itemBeingSold = l.getUserItem();
        if (itemBeingSold != null) {
            boolean merged = false;
            if (isStackable(itemBeingSold.getItem())) {
                Optional<UserItem> existing = uiRepo.findByCharacter_CharIdAndItem_ItemIdAndIsLockedFalse(buyerChar.getCharId(), itemBeingSold.getItem().getItemId())
                        .stream().findFirst();
                if (existing.isPresent()) {
                    UserItem exist = existing.get();
                    exist.setQuantity(exist.getQuantity() + itemBeingSold.getQuantity());
                    uiRepo.save(exist);

                    l.setUserItem(null);
                    listingRepo.save(l);
                    uiRepo.delete(itemBeingSold);
                    merged = true;
                }
            }

            if (!merged) {
                itemBeingSold.setCharacter(buyerChar);
                itemBeingSold.setIsEquipped(false);
                itemBeingSold.setIsLocked(false);
                uiRepo.save(itemBeingSold);
            }
        } else {
            deliverSystemItem(buyerChar, l.getItem(), l.getQuantity());
        }

        l.setStatus("SOLD");
        listingRepo.save(l);
        return "Giao dịch thành công!";
    }

    @Transactional
    public String cancelListing(Long id) {
        User u = getCurrentUser();
        Character myChar = getMyChar();

        MarketListing l = listingRepo.findById(id).orElseThrow(() -> new RuntimeException("Lỗi tin đăng"));
        if (!l.getSeller().getUserId().equals(u.getUserId())) throw new RuntimeException("Không phải của bạn");
        if (!"ACTIVE".equals(l.getStatus())) throw new RuntimeException("Tin không còn khả dụng");

        UserItem ui = l.getUserItem();
        if (ui != null) {
            boolean merged = false;
            if (isStackable(ui.getItem())) {
                Optional<UserItem> existingStack = uiRepo.findByCharacter_CharIdAndItem_ItemIdAndIsLockedFalse(myChar.getCharId(), ui.getItem().getItemId())
                        .stream().findFirst();
                if (existingStack.isPresent()) {
                    UserItem exist = existingStack.get();
                    exist.setQuantity(exist.getQuantity() + ui.getQuantity());
                    uiRepo.save(exist);

                    l.setUserItem(null);
                    listingRepo.save(l);
                    uiRepo.delete(ui);
                    merged = true;
                }
            }
            if (!merged) {
                ui.setIsLocked(false);
                uiRepo.save(ui);
            }
        }

        l.setStatus("CANCELLED");
        listingRepo.save(l);
        return "Đã thu hồi";
    }

    private void deliverSystemItem(Character c, Item item, int qty) {
        boolean isStackable = isStackable(item);
        if (isStackable) {
            UserItem ui = uiRepo.findByCharacter_CharIdAndItem_ItemIdAndIsLockedFalse(c.getCharId(), item.getItemId())
                    .stream().findFirst()
                    .orElse(UserItem.builder()
                            .character(c).item(item).quantity(0).isEquipped(false).enhanceLevel(0).build());
            ui.setQuantity(ui.getQuantity() + qty);
            uiRepo.save(ui);
        } else {
            for (int k = 0; k < qty; k++) {
                UserItem ui = UserItem.builder()
                        .character(c).item(item).quantity(1).isEquipped(false).enhanceLevel(0).build();
                uiRepo.save(ui);
            }
        }
    }

    private boolean isStackable(Item item) {
        return item.getSlotType() == SlotType.CONSUMABLE || item.getSlotType() == SlotType.NONE || "MATERIAL".equals(item.getType());
    }
}