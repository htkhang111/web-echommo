// File: EchoMMO-Backend/src/main/java/com/echommo/dto/SellItemRequest.java

package com.echommo.dto;

public class SellItemRequest {
    // [FIX] Dùng Integer theo ý ông
    private Integer userItemId;
    private int quantity;

    public SellItemRequest() {
    }

    public SellItemRequest(Integer userItemId, int quantity) {
        this.userItemId = userItemId;
        this.quantity = quantity;
    }

    public Integer getUserItemId() {
        return userItemId;
    }

    public void setUserItemId(Integer userItemId) {
        this.userItemId = userItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}