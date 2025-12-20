//package com.echommo.service;
//
//import com.echommo.dto.CreateListingRequest;
//import com.echommo.entity.*;
//import com.echommo.entity.Character;
//import com.echommo.enums.SlotType;
//import com.echommo.repository.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class MarketplaceService {
//
//    @Autowired private ItemRepository itemRepo;
//    @Autowired private UserRepository userRepo;
//    @Autowired private CharacterRepository charRepo;
//    @Autowired private WalletRepository walletRepo;
//    @Autowired private UserItemRepository uiRepo;
//    @Autowired private MarketListingRepository listingRepo;
//
//    private User getCurrentUser() {
//        String u = SecurityContextHolder.getContext().getAuthentication().getName();
//        return userRepo.findByUsername(u).orElseThrow(() -> new RuntimeException("User not found"));
//    }
//
//    private Character getMyChar() {
//        User u = getCurrentUser();
//        return charRepo.findByUser_UserId(u.getUserId())
//                .orElseThrow(() -> new RuntimeException("Bạn chưa tạo nhân vật"));
//    }
//
//    // --- SHOP SYSTEM ---
//    public List<Item> getShopItems() {
//        return itemRepo.findAll(); // Lấy tất cả, có thể lọc MATERIAL nếu muốn
//    }
//
//    @Transactional
//    public String buyItem(Integer itemId, Integer qty) {
//        User u = getCurrentUser();
//        Item i = itemRepo.findById(itemId).orElseThrow(() -> new RuntimeException("Item không tồn tại"));
//
//        BigDecimal cost = BigDecimal.valueOf(i.getBasePrice()).multiply(BigDecimal.valueOf(qty));
//
//        // Kiểm tra ví tiền
//        if (u.getWallet() == null) throw new RuntimeException("Lỗi dữ liệu ví tiền");
//        if (u.getWallet().getGold() == null) u.getWallet().setGold(BigDecimal.ZERO);
//
//        if (u.getWallet().getGold().compareTo(cost) < 0) {
//            throw new RuntimeException("Không đủ vàng (Cần: " + cost + ")");
//        }
//
//        u.getWallet().setGold(u.getWallet().getGold().subtract(cost));
//        walletRepo.save(u.getWallet());
//
//        deliverSystemItem(getMyChar(), i, qty);
//        return "Mua thành công!";
//    }
//
//    @Transactional
//    public String sellItem(Long userItemId, Integer qty) {
//        Character myChar = getMyChar();
//        UserItem ui = uiRepo.findByUserItemIdAndCharacter_CharId(userItemId, myChar.getCharId())
//                .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại"));
//
//        if (Boolean.TRUE.equals(ui.getIsEquipped())) throw new RuntimeException("Không thể bán đồ đang mặc");
//        if (ui.getQuantity() < qty) throw new RuntimeException("Không đủ số lượng");
//
//        BigDecimal earn = BigDecimal.valueOf(ui.getItem().getBasePrice()).multiply(new BigDecimal("0.5")).multiply(BigDecimal.valueOf(qty));
//        User u = myChar.getUser();
//
//        if (u.getWallet() == null) throw new RuntimeException("Lỗi ví tiền");
//        u.getWallet().setGold(u.getWallet().getGold().add(earn));
//        walletRepo.save(u.getWallet());
//
//        if (ui.getQuantity() <= qty) uiRepo.delete(ui);
//        else { ui.setQuantity(ui.getQuantity() - qty); uiRepo.save(ui); }
//        return "Bán thành công, nhận " + earn + " vàng";
//    }
//
//    // --- PLAYER MARKET ---
//    @Transactional(readOnly = true)
//    public List<MarketListing> getPlayerListings() {
//        return listingRepo.findByStatusOrderByCreatedAtDesc("ACTIVE");
//    }
//
//    public List<MarketListing> getMyListings() {
//        return listingRepo.findBySeller_UserIdAndStatus(getCurrentUser().getUserId(), "ACTIVE");
//    }
//
//    @Transactional
//    public String createListing(CreateListingRequest req) {
//        User u = getCurrentUser();
//        Character myChar = getMyChar();
//        UserItem ui = uiRepo.findByUserItemIdAndCharacter_CharId(req.getUserItemId(), myChar.getCharId())
//                .orElseThrow(() -> new RuntimeException("Vật phẩm lỗi"));
//
//        if (Boolean.TRUE.equals(ui.getIsEquipped())) throw new RuntimeException("Phải tháo đồ trước khi bán");
//
//        MarketListing ml = new MarketListing();
//        ml.setSeller(u); ml.setItem(ui.getItem()); ml.setUserItem(ui);
//        ml.setQuantity(ui.getQuantity()); ml.setPrice(req.getPrice());
//        ml.setEnhanceLevel(ui.getEnhancementLevel() != null ? ui.getEnhancementLevel() : 0);
//        ml.setStatus("ACTIVE");
//        listingRepo.save(ml);
//        return "Đã đăng bán";
//    }
//
//    @Transactional
//    public String buyPlayerListing(Integer listingId, Integer qty) {
//        User buyer = getCurrentUser();
//        Character buyerChar = getMyChar();
//        MarketListing l = listingRepo.findById(listingId).orElseThrow(() -> new RuntimeException("Tin không tồn tại"));
//
//        if (!"ACTIVE".equals(l.getStatus())) throw new RuntimeException("Đã bị người khác mua mất rồi!");
//        if (l.getSeller().getUserId().equals(buyer.getUserId())) throw new RuntimeException("Không thể tự mua đồ của mình");
//
//        BigDecimal total = l.getPrice();
//        if (buyer.getWallet().getGold().compareTo(total) < 0) throw new RuntimeException("Không đủ vàng");
//
//        // Trừ tiền người mua
//        buyer.getWallet().setGold(buyer.getWallet().getGold().subtract(total));
//        walletRepo.save(buyer.getWallet());
//
//        // Cộng tiền người bán
//        User seller = l.getSeller();
//        BigDecimal receive = total.multiply(new BigDecimal("0.95")); // Thuế 5%
//        seller.getWallet().setGold(seller.getWallet().getGold().add(receive));
//        walletRepo.save(seller.getWallet());
//
//        // Chuyển vật phẩm
//        UserItem itemBeingSold = l.getUserItem();
//        if (itemBeingSold != null) {
//            itemBeingSold.setCharacter(buyerChar);
//            itemBeingSold.setIsEquipped(false);
//            uiRepo.save(itemBeingSold);
//        } else {
//            // Fallback nếu data cũ
//            deliverSystemItem(buyerChar, l.getItem(), l.getQuantity());
//        }
//
//        l.setStatus("SOLD");
//        listingRepo.save(l);
//        return "Giao dịch thành công!";
//    }
//
//    @Transactional
//    public String cancelListing(Integer id) {
//        User u = getCurrentUser();
//        MarketListing l = listingRepo.findById(id).orElseThrow(() -> new RuntimeException("Lỗi tin đăng"));
//        if (!l.getSeller().getUserId().equals(u.getUserId())) throw new RuntimeException("Không phải của bạn");
//        l.setStatus("CANCELLED");
//        listingRepo.save(l);
//        return "Đã thu hồi";
//    }
//
//    private void deliverSystemItem(Character c, Item item, int qty) {
//        boolean isStackable = item.getSlotType() == SlotType.CONSUMABLE || item.getSlotType() == SlotType.NONE;
//        if (isStackable) {
//            UserItem ui = uiRepo.findByCharacter_CharIdAndItem_ItemId(c.getCharId(), item.getItemId())
//                    .orElse(UserItem.builder().character(c).item(item).quantity(0).isEquipped(false).enhancementLevel(0).build());
//            ui.setQuantity(ui.getQuantity() + qty);
//            uiRepo.save(ui);
//        } else {
//            for (int k = 0; k < qty; k++) {
//                UserItem ui = UserItem.builder().character(c).item(item).quantity(1).isEquipped(false).enhancementLevel(0).build();
//                uiRepo.save(ui);
//            }
//        }
//    }
//}

package com.echommo.service;

import com.echommo.entity.MarketListing;
import com.echommo.entity.User;
import com.echommo.entity.Wallet;
import com.echommo.repository.MarketListingRepository;
import com.echommo.repository.UserItemRepository;
import com.echommo.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MarketplaceService {
    private final MarketListingRepository listingRepository;
    private final WalletRepository walletRepository;
    private final UserItemRepository userItemRepository;

    @Transactional
    public void buyItem(User buyer, Long listingId) {
        MarketListing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        if (listing.getSeller().getUserId().equals(buyer.getUserId())) {
            throw new RuntimeException("Cannot buy your own item");
        }

        Wallet buyerWallet = walletRepository.findByUser(buyer).orElseThrow();
        Wallet sellerWallet = walletRepository.findByUser(listing.getSeller()).orElseThrow();

        // [FIX] Logic thanh toán
        if ("ECHO".equals(listing.getCurrencyType())) {
            BigDecimal price = BigDecimal.valueOf(listing.getPrice());

            if (buyerWallet.getEchoCoin().compareTo(price) < 0) {
                throw new RuntimeException("Not enough Echo Coin");
            }
            buyerWallet.setEchoCoin(buyerWallet.getEchoCoin().subtract(price));
            sellerWallet.setEchoCoin(sellerWallet.getEchoCoin().add(price));
        } else {
            long price = listing.getPrice();
            if (buyerWallet.getGold() < price) {
                throw new RuntimeException("Not enough Gold");
            }
            buyerWallet.setGold(buyerWallet.getGold() - price);
            sellerWallet.setGold(sellerWallet.getGold() + price);
        }

        walletRepository.save(buyerWallet);
        walletRepository.save(sellerWallet);

        listing.setSold(true);
        listingRepository.save(listing);
    }
}