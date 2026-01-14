// File: EchoMMO-Backend/src/main/java/com/echommo/controller/ShopController.java

package com.echommo.controller;

import com.echommo.dto.BuyItemRequest;
import com.echommo.dto.SellItemRequest;
import com.echommo.entity.Character;
import com.echommo.entity.Item;
import com.echommo.entity.User;
import com.echommo.entity.UserItem;
import com.echommo.entity.Wallet;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.ItemRepository;
import com.echommo.repository.UserItemRepository;
import com.echommo.repository.UserRepository;
import com.echommo.repository.WalletRepository;
import com.echommo.security.JwtUtils;
import com.echommo.service.MarketplaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController // Đánh dấu đây là nơi nhận API request
@RequestMapping("/api/shop") // Đường dẫn gốc: localhost:8080/api/shop
public class ShopController extends BaseController {

    // [DEPENDENCY INJECTION] Gọi các kho dữ liệu (Repository) và Service cần dùng
    @Autowired private ItemRepository itemRepository;       // Kho Item gốc
    @Autowired private UserItemRepository userItemRepository; // Kho đồ của người chơi
    @Autowired private WalletRepository walletRepository;   // Ví tiền
    @Autowired private UserRepository userRepository;       // Tài khoản
    @Autowired private CharacterRepository characterRepository; // Nhân vật
    @Autowired private JwtUtils jwtUtils;                   // Giải mã token đăng nhập
    @Autowired private MarketplaceService marketplaceService; // Service xử lý logic phức tạp

    // === API 1: LẤY DANH SÁCH ĐỒ TRONG SHOP ===
    @GetMapping("/items")
    public ResponseEntity<?> getShopItems() {
        // 1. Lấy tất cả item được đánh dấu là "Hàng hệ thống" (isSystemItem = true)
        List<Item> allItems = itemRepository.findByIsSystemItemTrue();

        // 2. [LỌC DỮ LIỆU] Stream API để lọc bớt đồ xịn quá không bán shop
        List<Item> filteredItems = allItems.stream()
                .filter(item -> {
                    // Logic: Nếu là TOOL (Công cụ) và Tier >= 5 (Hàng xịn) -> Ẩn đi, không bán.
                    if ("TOOL".equals(item.getType()) && item.getTier() != null && item.getTier() >= 5) {
                        return false;
                    }
                    return true; // Các món khác thì giữ lại
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(filteredItems);
    }

    // === API 2: MUA ĐỒ TỪ SHOP (QUAN TRỌNG) ===
    @PostMapping("/buy")
    @Transactional // [QUAN TRỌNG] Nếu có lỗi ở bất kỳ bước nào, rollback toàn bộ (không bị mất tiền mà không nhận được đồ)
    public ResponseEntity<?> buyItem(@RequestBody BuyItemRequest request, @RequestHeader("Authorization") String token) {
        // 1. Xác thực người dùng từ Token
        String username = jwtUtils.getUserNameFromJwtToken(token.substring(7)); // Cắt chữ "Bearer "
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Kiểm tra xem user đã tạo Nhân Vật chưa
        Character character = characterRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Chưa tạo nhân vật!"));

        // 3. Tìm món đồ user muốn mua
        // Lưu ý: request.getItemId() trả về Long nên cần ép kiểu về int nếu DB dùng int
        Item item = itemRepository.findById(request.getItemId().intValue())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // 4. [VALIDATION] Kiểm tra logic game
        // - Không bán đồ drop/limited
        if (Boolean.FALSE.equals(item.getIsSystemItem())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vật phẩm này không được bán trong Shop (Limited/Drop only)."));
        }
        // - Không bán đồ Thần thánh (Tier 5) bằng vàng thường
        if ("TOOL".equals(item.getType()) && item.getTier() != null && item.getTier() >= 5) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vật phẩm Huyền Thoại không thể mua bằng tiền thường!"));
        }

        // 5. Kiểm tra ví tiền
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        int quantity = request.getQuantity() > 0 ? request.getQuantity() : 1;
        // Tính tổng tiền: Giá gốc * Số lượng
        BigDecimal totalCost = item.getBasePrice().multiply(BigDecimal.valueOf(quantity));

        // So sánh: Nếu tiền trong ví < tổng tiền -> Báo lỗi
        if (wallet.getGold().compareTo(totalCost) < 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không đủ ngân lượng!"));
        }

        // 6. [TRỪ TIỀN]
        wallet.setGold(wallet.getGold().subtract(totalCost));
        walletRepository.save(wallet);

        // 7. [CỘNG ĐỒ]
        // Tìm xem trong túi đồ user đã có món này chưa
        UserItem userItem = userItemRepository.findByCharacterAndItem(character, item)
                .orElse(new UserItem()); // Nếu chưa có thì tạo mới object rỗng

        // Nếu là món mới tinh (ID chưa có) -> Set các thông tin cơ bản
        if (userItem.getUserItemId() == null) {
            userItem.setCharacter(character);
            userItem.setItem(item);
            userItem.setQuantity(0);
            // Độ bền mặc định
            userItem.setMaxDurability(item.getMaxDurability() != null ? item.getMaxDurability() : 100);
            userItem.setCurrentDurability(userItem.getMaxDurability());
        }

        // Cộng dồn số lượng
        userItem.setQuantity(userItem.getQuantity() + quantity);
        userItemRepository.save(userItem);

        return ResponseEntity.ok(Map.of("message", "Giao dịch thành công!", "goldBalance", wallet.getGold()));
    }

    // === API 3: BÁN ĐỒ VÀO SHOP (NPC THU MUA) ===
    @PostMapping("/sell")
    @Transactional
    public ResponseEntity<?> sellItem(@RequestBody SellItemRequest request) {
        try {
            if (request.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Số lượng bán phải lớn hơn 0"));
            }

            // [LOGIC DELEGATION] Đẩy việc khó cho Service làm
            // Service sẽ lo việc kiểm tra tồn kho, tính giá (80%), cộng tiền, xóa đồ
            String resultMessage = marketplaceService.sellItem(request.getUserItemId(), request.getQuantity());

            return ResponseEntity.ok(Map.of("message", resultMessage));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}