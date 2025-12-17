package com.echommo.controller;

import com.echommo.entity.Item;
import com.echommo.service.MarketplaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/market/shop")
public class ShopController {

    @Autowired
    private MarketplaceService service;

    @GetMapping("/items")
    public ResponseEntity<List<Item>> getShopItems() {
        // Gọi getShopItems() từ MarketplaceService
        return ResponseEntity.ok(service.getShopItems());
    }

    @PostMapping("/buy")
    public ResponseEntity<String> buyFromShop(@RequestBody Map<String, Object> req) {
        Integer itemId = Integer.parseInt(req.get("itemId").toString());
        Integer qty = Integer.parseInt(req.get("quantity").toString());
        return ResponseEntity.ok(service.buyItem(itemId, qty));
    }

    @PostMapping("/sell")
    public ResponseEntity<String> sellToShop(@RequestBody Map<String, Object> req) {
        // Quan trọng: UserItem ID phải là Long
        Long userItemId = Long.parseLong(req.get("userItemId").toString());
        Integer qty = Integer.parseInt(req.get("quantity").toString());
        // Gọi sellItem(Long, Integer)
        return ResponseEntity.ok(service.sellItem(userItemId, qty));
    }
}