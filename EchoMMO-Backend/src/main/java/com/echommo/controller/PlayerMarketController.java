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
    public ResponseEntity<List<MarketListing>> getActiveListings() {
        return ResponseEntity.ok(service.getPlayerListings());
    }

    @GetMapping("/my")
    public ResponseEntity<List<MarketListing>> getMyListings() {
        // Gọi phương thức getMyListings() đã fix trong MarketplaceService
        return ResponseEntity.ok(service.getMyListings());
    }

    @PostMapping("/list")
    public ResponseEntity<String> createListing(@RequestBody CreateListingRequest req) {
        // Gọi phương thức createListing(CreateListingRequest)
        return ResponseEntity.ok(service.createListing(req));
    }

    @PostMapping("/buy")
    public ResponseEntity<String> buy(@RequestBody Map<String, Object> req) {
        Integer listingId = Integer.parseInt(req.get("listingId").toString());
        Integer qty = Integer.parseInt(req.get("quantity").toString());
        // Gọi buyPlayerListing(Integer, Integer)
        return ResponseEntity.ok(service.buyPlayerListing(listingId, qty));
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<String> cancel(@PathVariable Integer id) {
        // Gọi cancelListing(Integer)
        return ResponseEntity.ok(service.cancelListing(id));
    }
}