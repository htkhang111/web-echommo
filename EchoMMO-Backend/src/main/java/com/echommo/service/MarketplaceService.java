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

    // [HELPER] [FIXED] Tìm ví theo Entity User thay vì ID để tránh lỗi Type
    private Wallet getOrCreateWallet(User user) {
        return walletRepo.findByUser(user)
                .orElseGet(() -> {
                    System.out.println(">>> [DEBUG WALLET] Không thấy ví của " + user.getUsername() + ". Đang tạo mới...");

                    // Kiểm tra kỹ lần nữa xem có ví nào tồn tại theo ID không (đề phòng cache)
                    Optional<Wallet> doubleCheck = walletRepo.findByUser_UserId(user.getUserId());
                    if (doubleCheck.isPresent()) {
                        System.out.println(">>> [DEBUG WALLET] Tìm thấy ví sau khi double check!");
                        return doubleCheck.get();
                    }

                    Wallet newWallet = new Wallet();
                    newWallet.setUser(user);
                    newWallet.setGold(new BigDecimal("1000000")); // Tặng 1 triệu test
                    newWallet.setEchoCoin(BigDecimal.ZERO);
                    try {
                        return walletRepo.save(newWallet);
                    } catch (Exception e) {
                        // Nếu vẫn lỗi trùng lặp, cố gắng load lại lần cuối
                        System.out.println(">>> [ERROR] Lỗi tạo ví (có thể đã tồn tại): " + e.getMessage());
                        return walletRepo.findByUser(user).orElseThrow(() -> new RuntimeException("Lỗi hệ thống ví: " + e.getMessage()));
                    }
                });
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

        System.out.println(">>> [SHOP] Bắt đầu giao dịch mua vật phẩm ID: " + itemId);

        // 1. Lấy ví (Đã fix)
        Wallet w = getOrCreateWallet(u);
        BigDecimal goldBefore = w.getGold();

        // 2. In Log chứng minh TRƯỚC khi trừ
        System.out.println("--------------------------------------------------");
        System.out.println(">>> [CHỨNG MINH] User: " + u.getUsername() + " mua System Item.");
        System.out.println(">>> Tiền hiện có: " + goldBefore);
        System.out.println(">>> Giá phải trả: " + cost);

        if (goldBefore.compareTo(cost) < 0) {
            System.out.println(">>> KẾT QUẢ: Thất bại (Không đủ tiền)");
            throw new RuntimeException("Không đủ vàng (Cần: " + cost + ", Có: " + goldBefore + ")");
        }

        // 3. Thực hiện trừ tiền
        w.setGold(goldBefore.subtract(cost));
        Wallet savedWallet = walletRepo.save(w); // Lưu DB ngay lập tức

        // 4. In Log chứng minh SAU khi trừ
        System.out.println(">>> Tiền sau khi mua: " + savedWallet.getGold());
        System.out.println(">>> Trạng thái: ĐÃ TRỪ TIỀN THÀNH CÔNG TRONG DB");
        System.out.println("--------------------------------------------------");

        deliverSystemItem(getMyChar(), i, qty);
        return "Mua thành công! (Số dư cũ: " + goldBefore + " -> Mới: " + savedWallet.getGold() + ")";
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

        Wallet w = getOrCreateWallet(u);
        System.out.println(">>> [BÁN SHOP] User: " + u.getUsername() + " | Tiền gốc: " + w.getGold() + " | Cộng thêm: " + earn);

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
        if (Boolean.TRUE.equals(ui.getIsLocked())) throw new RuntimeException("Vật phẩm đang bị khóa (Check DB)!");

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

        // 1. Lấy ví người mua (Đã fix)
        Wallet buyerWallet = getOrCreateWallet(buyer);
        BigDecimal buyerGoldBefore = buyerWallet.getGold();

        // 2. Log Chứng minh
        System.out.println("--------------------------------------------------");
        System.out.println(">>> [CHỨNG MINH CHỢ] Buyer: " + buyer.getUsername() + " mua đồ Listing #" + listingId);
        System.out.println(">>> Tiền Buyer TRƯỚC: " + buyerGoldBefore);
        System.out.println(">>> Giá: " + total);

        if (buyerGoldBefore.compareTo(total) < 0) {
            System.out.println(">>> KẾT QUẢ: Thất bại (Buyer nghèo)");
            throw new RuntimeException("Không đủ vàng (Cần: " + total + ", Có: " + buyerGoldBefore + ")");
        }

        // 3. Trừ tiền Buyer
        buyerWallet.setGold(buyerGoldBefore.subtract(total));
        walletRepo.save(buyerWallet);
        System.out.println(">>> Tiền Buyer SAU: " + buyerWallet.getGold() + " (Đã lưu DB)");

        // 4. Cộng tiền Seller
        User seller = l.getSeller();
        Wallet sellerWallet = getOrCreateWallet(seller);
        BigDecimal receive = total.multiply(new BigDecimal("0.95")); // Trừ phí 5%

        System.out.println(">>> Seller: " + seller.getUsername() + " | Tiền trước: " + sellerWallet.getGold());
        sellerWallet.setGold(sellerWallet.getGold().add(receive));
        walletRepo.save(sellerWallet);
        System.out.println(">>> Seller nhận: " + receive + " | Tiền sau: " + sellerWallet.getGold());
        System.out.println("--------------------------------------------------");

        // Xử lý chuyển đồ
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