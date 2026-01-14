// File: EchoMMO-Backend/src/main/java/com/echommo/controller/PlayerMarketController.java

package com.echommo.controller;

import com.echommo.dto.CreateListingRequest;
import com.echommo.service.MarketplaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/market/player") // Đường dẫn gốc: localhost:8080/api/market/player
@CrossOrigin(originPatterns = "*") // Cho phép Frontend (Vue) ở port khác gọi vào (CORS)
public class PlayerMarketController {

    @Autowired
    private MarketplaceService service; // Service chuyên xử lý giao dịch Chợ Trời

    // === API 1: XEM DANH SÁCH ĐANG BÁN ===
    @GetMapping("/active")
    public ResponseEntity<?> getActiveListings() {
        // Lấy tất cả các Listing có status = ACTIVE
        // [NOTE] Jackson (thư viện JSON) sẽ tự động chuyển object Java sang JSON trả về cho Vue
        return ResponseEntity.ok(service.getPlayerListings());
    }

    // === API 2: XEM SẠP HÀNG CỦA MÌNH ===
    @GetMapping("/my")
    public ResponseEntity<?> getMyListings() {
        // Lấy danh sách Listing do chính user đang đăng nhập tạo ra
        // Service sẽ tự lấy user từ SecurityContext (Token)
        return ResponseEntity.ok(service.getMyListings());
    }

    // === API 3: TREO BÁN ĐỒ LÊN CHỢ ===
    @PostMapping("/list")
    public ResponseEntity<?> createListing(@RequestBody CreateListingRequest req) {
        try {
            // req chứa: userItemId (món đồ), price (giá bán), quantity (số lượng)
            // Service sẽ trừ đồ trong túi và tạo record Listing mới
            return ResponseEntity.ok(service.createListing(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // === API 4: MUA ĐỒ CỦA NGƯỜI KHÁC ===
    @PostMapping("/buy")
    public ResponseEntity<?> buy(@RequestBody Map<String, Object> req) {
        try {
            // Kiểm tra dữ liệu đầu vào
            if (req.get("listingId") == null || req.get("quantity") == null) {
                return ResponseEntity.badRequest().body("Thiếu thông tin listingId");
            }

            // [FIXED BUG TIỀM TÀNG]
            // JSON gửi số lên thường được hiểu là Integer, nhưng Database ID là Long.
            // Cần parse về String rồi parse lại Long để tránh lỗi ClassCastException.
            Long listingId = Long.parseLong(req.get("listingId").toString());
            Integer qty = Integer.parseInt(req.get("quantity").toString());

            // Service thực hiện: Trừ tiền người mua -> Cộng tiền người bán -> Chuyển đồ
            return ResponseEntity.ok(service.buyPlayerListing(listingId, qty));
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console server để debug
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // === API 5: HỦY BÁN (THU HỒI ĐỒ) ===
    @PostMapping("/cancel/{id}")
    // [FIXED] Dùng @PathVariable để lấy ID từ URL (ví dụ: /cancel/123)
    // ID ở đây là Long (ListingId)
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        try {
            // Service thực hiện: Xóa Listing -> Trả đồ lại vào túi (UserItem)
            return ResponseEntity.ok(service.cancelListing(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}