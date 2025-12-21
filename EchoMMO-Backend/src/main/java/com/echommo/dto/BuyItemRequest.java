package com.echommo.dto;

public class BuyItemRequest {
    private Long itemId;
    private int quantity;

    // Constructors
    public BuyItemRequest() {
    }

    public BuyItemRequest(Long itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}