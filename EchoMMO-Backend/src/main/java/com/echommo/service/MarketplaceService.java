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

    // --- SYSTEM SHOP ---
    public List<Item> getShopItems() {
        return itemRepo.findAll();
    }

    @Transactional
    public String buyItem(Integer itemId, Integer qty) {
        User u = getCurrentUser();
        Item i = itemRepo.findById(itemId).orElseThrow(() -> new RuntimeException("Item không tồn tại"));

        BigDecimal cost = i.getBasePrice().multiply(BigDecimal.valueOf(qty));

        if (u.getWallet() == null) throw new RuntimeException("Lỗi dữ liệu ví tiền");
        if (u.getWallet().getGold() == null) u.getWallet().setGold(BigDecimal.ZERO);

        if (u.getWallet().getGold().compareTo(cost) < 0) {
            throw new RuntimeException("Không đủ vàng (Cần: " + cost + ")");
        }

        u.getWallet().setGold(u.getWallet().getGold().subtract(cost));
        walletRepo.save(u.getWallet());

        deliverSystemItem(getMyChar(), i, qty);
        return "Mua thành công!";
    }

    @Transactional
    public String sellItem(Long userItemId, Integer qty) {
        Character myChar = getMyChar();

        UserItem ui = uiRepo.findByUserItemIdAndCharacter_CharId(userItemId, myChar.getCharId())
                .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại hoặc không phải của bạn"));

        if (Boolean.TRUE.equals(ui.getIsEquipped())) throw new RuntimeException("Không thể bán đồ đang mặc");
        if (ui.getQuantity() < qty) throw new RuntimeException("Không đủ số lượng");

        BigDecimal earn = ui.getItem().getBasePrice()
                .multiply(new BigDecimal("0.5"))
                .multiply(BigDecimal.valueOf(qty));

        User u = myChar.getUser();
        if (u.getWallet() == null) throw new RuntimeException("Lỗi ví tiền");
        if (u.getWallet().getGold() == null) u.getWallet().setGold(BigDecimal.ZERO);

        u.getWallet().setGold(u.getWallet().getGold().add(earn));
        walletRepo.save(u.getWallet());

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
        // [FIX] Gọi hàm mới ListedAt
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
        if (ui.getQuantity() < qtyToSell) throw new RuntimeException("Không đủ số lượng để bán!");

        // [CHECK] Không cho bán đồ đang bị khóa (ví dụ đang treo bán ở slot khác nếu có bug)
        if (Boolean.TRUE.equals(ui.getIsLocked())) throw new RuntimeException("Vật phẩm đang bị khóa!");

        UserItem itemToSell;

        // Nếu bán 1 phần trong stack -> tách ra item mới
        if (ui.getQuantity() > qtyToSell) {
            ui.setQuantity(ui.getQuantity() - qtyToSell);
            uiRepo.save(ui);

            itemToSell = UserItem.builder()
                    .item(ui.getItem())
                    .quantity(qtyToSell)
                    .character(myChar) // [FIXED] Vẫn thuộc về người bán
                    .isEquipped(false)
                    .isLocked(true)    // [FIXED] Khóa lại để ẩn khỏi túi
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
            // Nếu bán hết -> dùng luôn item cũ
            itemToSell = ui;
            itemToSell.setIsLocked(true); // [FIXED] Khóa lại thay vì setCharacter(null)
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
    public String buyPlayerListing(Long listingId, Integer qty) { // [FIX] Integer -> Long
        User buyer = getCurrentUser();
        Character buyerChar = getMyChar();

        MarketListing l = listingRepo.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Tin không tồn tại"));

        if (!"ACTIVE".equals(l.getStatus())) throw new RuntimeException("Đã bị người khác mua mất rồi!");
        if (l.getSeller().getUserId().equals(buyer.getUserId())) throw new RuntimeException("Không thể tự mua đồ của mình");

        BigDecimal total = l.getPrice();

        if (buyer.getWallet().getGold() == null) buyer.getWallet().setGold(BigDecimal.ZERO);
        if (buyer.getWallet().getGold().compareTo(total) < 0) {
            throw new RuntimeException("Không đủ vàng");
        }

        // Trừ tiền người mua
        buyer.getWallet().setGold(buyer.getWallet().getGold().subtract(total));
        walletRepo.save(buyer.getWallet());

        // Cộng tiền người bán (phí 5%)
        User seller = l.getSeller();
        if (seller.getWallet().getGold() == null) seller.getWallet().setGold(BigDecimal.ZERO);
        BigDecimal receive = total.multiply(new BigDecimal("0.95"));
        seller.getWallet().setGold(seller.getWallet().getGold().add(receive));
        walletRepo.save(seller.getWallet());

        // Chuyển đồ
        UserItem itemBeingSold = l.getUserItem();
        if (itemBeingSold != null) {
            boolean merged = false;
            // Logic gộp stack nếu vật phẩm cho phép gộp
            if (isStackable(itemBeingSold.getItem())) {
                Optional<UserItem> existing = uiRepo.findByCharacter_CharIdAndItem_ItemIdAndIsLockedFalse(buyerChar.getCharId(), itemBeingSold.getItem().getItemId())
                        .stream().findFirst();
                if (existing.isPresent()) {
                    UserItem exist = existing.get();
                    exist.setQuantity(exist.getQuantity() + itemBeingSold.getQuantity());
                    uiRepo.save(exist);

                    l.setUserItem(null); // Gỡ reference để tránh lỗi FK khi xóa
                    listingRepo.save(l);
                    uiRepo.delete(itemBeingSold); // Xóa item lẻ đã bán
                    merged = true;
                }
            }

            if (!merged) {
                // [FIXED] Chuyển chủ sở hữu và mở khóa
                itemBeingSold.setCharacter(buyerChar);
                itemBeingSold.setIsEquipped(false);
                itemBeingSold.setIsLocked(false);
                uiRepo.save(itemBeingSold);
            }
        } else {
            // Fallback nếu item bị null (hiếm)
            deliverSystemItem(buyerChar, l.getItem(), l.getQuantity());
        }

        l.setStatus("SOLD");
        listingRepo.save(l);
        return "Giao dịch thành công!";
    }

    @Transactional
    public String cancelListing(Long id) { // [FIX] Integer -> Long
        User u = getCurrentUser();
        Character myChar = getMyChar();

        MarketListing l = listingRepo.findById(id).orElseThrow(() -> new RuntimeException("Lỗi tin đăng"));
        if (!l.getSeller().getUserId().equals(u.getUserId())) throw new RuntimeException("Không phải của bạn");
        if (!"ACTIVE".equals(l.getStatus())) throw new RuntimeException("Tin không còn khả dụng để hủy");

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
                // [FIXED] Trả về chủ cũ (vẫn là myChar) và mở khóa
                ui.setIsLocked(false);
                uiRepo.save(ui);
            }
        }

        l.setStatus("CANCELLED");
        listingRepo.save(l);
        return "Đã thu hồi và trả vật phẩm về túi";
    }

    private void deliverSystemItem(Character c, Item item, int qty) {
        boolean isStackable = isStackable(item);

        if (isStackable) {
            // [FIX] Dùng hàm find mới
            UserItem ui = uiRepo.findByCharacter_CharIdAndItem_ItemIdAndIsLockedFalse(c.getCharId(), item.getItemId())
                    .stream().findFirst()
                    .orElse(UserItem.builder()
                            .character(c)
                            .item(item)
                            .quantity(0)
                            .isEquipped(false)
                            .enhanceLevel(0)
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
                        .enhanceLevel(0)
                        .build();
                uiRepo.save(ui);
            }
        }
    }

    private boolean isStackable(Item item) {
        return item.getSlotType() == SlotType.CONSUMABLE
                || item.getSlotType() == SlotType.NONE
                || "MATERIAL".equals(item.getType());
    }
}