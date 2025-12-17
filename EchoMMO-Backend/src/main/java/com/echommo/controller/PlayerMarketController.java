package com.echommo.controller;

import com.echommo.dto.CreateListingRequest;
import com.echommo.service.MarketplaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/market/player")
@CrossOrigin(originPatterns = "*")
public class PlayerMarketController {

    @Autowired
    private MarketplaceService service;

    @GetMapping("/active")
    public ResponseEntity<?> getActiveListings() {
        return ResponseEntity.ok(service.getPlayerListings());
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyListings() {
        return ResponseEntity.ok(service.getMyListings());
    }

    @PostMapping("/list")
    public ResponseEntity<?> createListing(@RequestBody CreateListingRequest req) {
        try {
            return ResponseEntity.ok(service.createListing(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buy(@RequestBody Map<String, Object> req) {
        try {
            if (req.get("listingId") == null || req.get("quantity") == null) {
                return ResponseEntity.badRequest().body("Thiếu thông tin listingId");
            }
            Integer listingId = Integer.parseInt(req.get("listingId").toString());
            Integer qty = Integer.parseInt(req.get("quantity").toString());

            return ResponseEntity.ok(service.buyPlayerListing(listingId, qty));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancel(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.cancelListing(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}