package com.echommo.controller;

import com.echommo.entity.Item;
import com.echommo.service.MarketplaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
// [SỬA 1] Đổi thành /api/shop để khớp với log lỗi của Frontend
@RequestMapping("/api/shop")
// [SỬA 2] Thêm CORS để Frontend (localhost:3000) gọi được
@CrossOrigin(origins = "http://localhost:3000")
public class ShopController {

    @Autowired
    private MarketplaceService service;

    // Endpoint: /api/shop/items
    @GetMapping("/items")
    public ResponseEntity<List<Item>> getShopItems() {
        return ResponseEntity.ok(service.getShopItems());
    }

    // Endpoint: /api/shop/buy
    @PostMapping("/buy")
    public ResponseEntity<String> buyFromShop(@RequestBody Map<String, Object> req) {
        // Parse an toàn hơn đề phòng null
        if (req.get("itemId") == null || req.get("quantity") == null) {
            return ResponseEntity.badRequest().body("Thiếu thông tin itemId hoặc quantity");
        }
        Integer itemId = Integer.parseInt(req.get("itemId").toString());
        Integer qty = Integer.parseInt(req.get("quantity").toString());
        return ResponseEntity.ok(service.buyItem(itemId, qty));
    }

    // Endpoint: /api/shop/sell
    @PostMapping("/sell")
    public ResponseEntity<String> sellToShop(@RequestBody Map<String, Object> req) {
        if (req.get("userItemId") == null || req.get("quantity") == null) {
            return ResponseEntity.badRequest().body("Thiếu thông tin userItemId hoặc quantity");
        }
        Long userItemId = Long.parseLong(req.get("userItemId").toString());
        Integer qty = Integer.parseInt(req.get("quantity").toString());
        return ResponseEntity.ok(service.sellItem(userItemId, qty));
    }
}