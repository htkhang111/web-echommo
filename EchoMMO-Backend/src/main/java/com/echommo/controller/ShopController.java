package com.echommo.controller;

import com.echommo.service.MarketplaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shop")
public class ShopController {

    @Autowired private MarketplaceService service;

    @GetMapping("/items")
    public ResponseEntity<?> getShopItems() {
        return ResponseEntity.ok(service.getShopItems());
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyFromShop(@RequestBody Map<String, Integer> body) {
        try {
            return ResponseEntity.ok(service.buyItem(body.get("itemId"), body.get("quantity")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellToShop(@RequestBody Map<String, Object> body) {
        try {
            Long userItemId = ((Number) body.get("userItemId")).longValue();
            Integer quantity = ((Number) body.get("quantity")).intValue();
            return ResponseEntity.ok(service.sellItem(userItemId, quantity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
