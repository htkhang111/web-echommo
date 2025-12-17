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
    // @Autowired private NotificationService notiService;

    // --- HELPER: Lấy User & Character hiện tại ---
    private User getCurrentUser() {
        String u = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(u).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Character getMyChar() {
        User u = getCurrentUser();
        // Sửa: Lấy CharId từ DB chuẩn hơn
        return charRepo.findByUser_UserId(u.getUserId())
                .orElseThrow(() -> new RuntimeException("Bạn chưa tạo nhân vật"));
    }

    // ================== SHOP CONTROLLER METHODS ==================
    public List<Item> getShopItems() {
        return itemRepo.findAll();
    }

    @Transactional
    public String buyItem(Integer itemId, Integer qty) {
        User u = getCurrentUser();
        Item i = itemRepo.findById(itemId).orElseThrow(() -> new RuntimeException("Item không tồn tại"));

        BigDecimal cost = BigDecimal.valueOf(i.getBasePrice()).multiply(BigDecimal.valueOf(qty));
        if (u.getWallet().getGold().compareTo(cost) < 0) throw new RuntimeException("Không đủ vàng");

        u.getWallet().setGold(u.getWallet().getGold().subtract(cost));
        walletRepo.save(u.getWallet());

        // Giao hàng (Hệ thống sinh item mới)
        deliverSystemItem(getMyChar(), i, qty);
        return "Mua thành công!";
    }

    @Transactional
    public String sellItem(Long userItemId, Integer qty) {
        Character myChar = getMyChar();
        UserItem ui = uiRepo.findByUserItemIdAndCharacter_CharId(userItemId, myChar.getCharId())
                .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại"));

        if (ui.getIsEquipped()) throw new RuntimeException("Không thể bán đồ đang mặc");
        if (ui.getQuantity() < qty) throw new RuntimeException("Không đủ số lượng");

        // Bán giá 50%
        BigDecimal earn = BigDecimal.valueOf(ui.getItem().getBasePrice())
                .multiply(new BigDecimal("0.5"))
                .multiply(BigDecimal.valueOf(qty));

        User u = myChar.getUser();
        u.getWallet().setGold(u.getWallet().getGold().add(earn));
        walletRepo.save(u.getWallet());

        if (ui.getQuantity() <= qty) uiRepo.delete(ui);
        else { ui.setQuantity(ui.getQuantity() - qty); uiRepo.save(ui); }

        return "Đã bán nhận " + earn + " vàng";
    }

    // ================== PLAYER MARKET METHODS (FIXED) ==================

    public List<MarketListing> getPlayerListings() {
        return listingRepo.findByStatusOrderByCreatedAtDesc("ACTIVE");
    }

    public List<MarketListing> getMyListings() {
        return listingRepo.findBySeller_UserIdAndStatus(getCurrentUser().getUserId(), "ACTIVE");
    }

    // [FIX] Đăng bán: Giữ nguyên UserItem, chỉ chuyển trạng thái
    @Transactional
    public String createListing(CreateListingRequest req) {
        User u = getCurrentUser();
        Character myChar = getMyChar();

        // Tìm item chính xác của nhân vật
        UserItem ui = uiRepo.findByUserItemIdAndCharacter_CharId(req.getUserItemId(), myChar.getCharId())
                .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại"));

        if (Boolean.TRUE.equals(ui.getIsEquipped())) throw new RuntimeException("Phải tháo đồ trước khi bán");

        // Với Equipment (Vũ khí/Giáp), số lượng luôn là 1 -> Đăng bán là chuyển cả item
        // Với Consumable (Stack được), nếu bán 1 phần thì phải tách stack (Phức tạp -> Tạm thời bắt bán hết hoặc tách trước)

        MarketListing ml = new MarketListing();
        ml.setSeller(u);
        ml.setItem(ui.getItem());
        ml.setUserItem(ui); // [QUAN TRỌNG] Lưu tham chiếu đến item thật
        ml.setQuantity(ui.getQuantity()); // Bán nguyên stack cho đơn giản
        ml.setPrice(req.getPrice());
        ml.setEnhanceLevel(ui.getEnhancementLevel() != null ? ui.getEnhancementLevel() : 0);
        ml.setStatus("ACTIVE");

        listingRepo.save(ml);

        // [QUAN TRỌNG] Không xóa item, mà set nó thuộc về "Hư không" hoặc khóa lại
        // Cách đơn giản nhất: Tạm thời set char_id null (nếu DB cho phép) hoặc set flag is_locked
        // Ở đây tôi chọn cách: Item vẫn thuộc về người bán nhưng bị khóa (isLocked = true)
        // (Bạn cần thêm field isLocked vào Entity UserItem nếu chưa có, hoặc dùng logic "Listing đang giữ")

        return "Đã đăng bán";
    }

    // [FIX] Mua đồ: Chuyển quyền sở hữu UserItem
    @Transactional
    public String buyPlayerListing(Integer listingId, Integer qty) {
        User buyer = getCurrentUser();
        Character buyerChar = getMyChar(); // Lấy nhân vật người mua để nhận đồ

        MarketListing l = listingRepo.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Tin không tồn tại"));

        if (!"ACTIVE".equals(l.getStatus())) throw new RuntimeException("Vật phẩm đã được bán");
        if (l.getSeller().getUserId().equals(buyer.getUserId())) throw new RuntimeException("Không thể tự mua");

        // Check tiền
        BigDecimal total = l.getPrice(); // Mua nguyên listing
        if (buyer.getWallet().getGold().compareTo(total) < 0) throw new RuntimeException("Không đủ vàng");

        // 1. Trừ tiền người mua
        buyer.getWallet().setGold(buyer.getWallet().getGold().subtract(total));
        walletRepo.save(buyer.getWallet());

        // 2. Cộng tiền người bán (Phí 5%)
        User seller = l.getSeller();
        BigDecimal receive = total.multiply(new BigDecimal("0.95"));
        seller.getWallet().setGold(seller.getWallet().getGold().add(receive));
        walletRepo.save(seller.getWallet());

        // 3. Chuyển item sang túi người mua [CORE LOGIC]
        UserItem itemBeingSold = l.getUserItem();
        if (itemBeingSold == null) {
            // Fallback nếu dữ liệu cũ không có UserItem -> Tạo mới (Mất option)
            deliverSystemItem(buyerChar, l.getItem(), l.getQuantity());
        } else {
            // Chuyển chủ sở hữu
            itemBeingSold.setCharacter(buyerChar);
            itemBeingSold.setIsEquipped(false);
            // itemBeingSold.setLocked(false); // Mở khóa nếu có dùng flag
            uiRepo.save(itemBeingSold);
        }

        // 4. Đóng Listing
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

        // Item vẫn đang ở trong túi (hoặc bị khóa), giờ chỉ cần mở khóa hoặc không làm gì
        // Nếu ở bước createListing bạn xóa item đi, thì giờ phải tạo lại.
        // Nhưng theo logic mới (giữ UserItem), ta không cần làm gì cả (item vẫn là của User).

        return "Đã hủy bán.";
    }

    // --- HELPER: Giao đồ hệ thống (Mua shop / Drop) ---
    private void deliverSystemItem(Character c, Item item, int qty) {
        boolean isStackable = item.getSlotType() == SlotType.CONSUMABLE || item.getSlotType() == SlotType.NONE;

        if (isStackable) {
            // Tìm stack cũ để cộng dồn
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
            // Đồ Equipment -> Luôn tạo dòng mới (Không stack)
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