package com.echommo.controller;

import com.echommo.entity.Item;
import com.echommo.service.MarketplaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop")
@CrossOrigin(originPatterns = "*")
public class ShopController {

    @Autowired
    private MarketplaceService service;

    @GetMapping("/items")
    public ResponseEntity<List<Item>> getShopItems() {
        return ResponseEntity.ok(service.getShopItems());
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyFromShop(@RequestBody Map<String, Object> req) {
        try {
            if (req.get("itemId") == null || req.get("quantity") == null) {
                return ResponseEntity.badRequest().body("Thiếu thông tin itemId hoặc quantity");
            }
            // Parse an toàn từ Object -> String -> Integer
            Integer itemId = Integer.parseInt(req.get("itemId").toString());
            Integer qty = Integer.parseInt(req.get("quantity").toString());

            return ResponseEntity.ok(service.buyItem(itemId, qty));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi mua hàng: " + e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellToShop(@RequestBody Map<String, Object> req) {
        try {
            if (req.get("userItemId") == null || req.get("quantity") == null) {
                return ResponseEntity.badRequest().body("Thiếu thông tin userItemId");
            }
            Long userItemId = Long.parseLong(req.get("userItemId").toString());
            Integer qty = Integer.parseInt(req.get("quantity").toString());

            return ResponseEntity.ok(service.sellItem(userItemId, qty));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}