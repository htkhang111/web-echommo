package com.echommo.controller;

import com.echommo.dto.CreateListingRequest;
import com.echommo.entity.MarketListing;
import com.echommo.service.MarketplaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/market/player")
public class PlayerMarketController {

    @Autowired
    private MarketplaceService service;

    @GetMapping("/active")
    public ResponseEntity<?> getActiveListings() {
        try {
            return ResponseEntity.ok(service.getPlayerListings());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi tải chợ: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyListings() {
        try {
            return ResponseEntity.ok(service.getMyListings());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi tải hàng của bạn");
        }
    }

    @PostMapping("/list")
    public ResponseEntity<?> createListing(@RequestBody CreateListingRequest req) {
        try {
            return ResponseEntity.ok(service.createListing(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi đăng bán");
        }
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buy(@RequestBody Map<String, Object> req) {
        try {
            Integer listingId = Integer.parseInt(req.get("listingId").toString());
            Integer qty = Integer.parseInt(req.get("quantity").toString());

            String result = service.buyPlayerListing(listingId, qty);
            return ResponseEntity.ok(result);

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Dữ liệu đầu vào không hợp lệ");
        } catch (RuntimeException e) {
            // Bắt lỗi nghiệp vụ (không đủ tiền, hết hàng...)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi giao dịch: " + e.getMessage());
        }
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancel(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.cancelListing(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}