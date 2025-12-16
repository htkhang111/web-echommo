package com.echommo.controller;

import com.echommo.dto.CreateListingRequest;
import com.echommo.service.MarketplaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/player-market")
public class PlayerMarketController {

    @Autowired private MarketplaceService service;

    @GetMapping("/listings")
    public ResponseEntity<?> getListings() {
        return ResponseEntity.ok(service.getPlayerListings());
    }

    @GetMapping("/my-listings")
    public ResponseEntity<?> getMyListings() {
        return ResponseEntity.ok(service.getMyListings());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createListing(@RequestBody CreateListingRequest req) {
        try {
            return ResponseEntity.ok(service.createListing(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/buy/{id}")
    public ResponseEntity<?> buyListingByPath(@PathVariable Integer id, @RequestBody(required = false) Map<String, Integer> body) {
        try {
            int quantity = (body != null && body.containsKey("quantity")) ? body.get("quantity") : 1;
            return ResponseEntity.ok(service.buyPlayerListing(id, quantity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyListing(@RequestBody Map<String, Object> body) {
        try {
            Integer listingId = ((Number) body.get("listingId")).intValue();
            Integer quantity = ((Number) body.get("quantity")).intValue();
            return ResponseEntity.ok(service.buyPlayerListing(listingId, quantity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancelListing(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.cancelListing(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
