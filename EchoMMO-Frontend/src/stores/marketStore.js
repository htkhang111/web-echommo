// File: EchoMMO-Frontend/src/stores/marketStore.js

import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";
import { useInventoryStore } from "./inventoryStore";
import { useAuthStore } from "./authStore";

export const useMarketStore = defineStore("market", {
  state: () => ({
    shopItems: [],
    playerListings: [],
    myListings: [],
    isLoading: false,
  }),
  actions: {
    async fetchShopItems() {
      const res = await axiosClient.get("/shop/items");
      this.shopItems = res.data;
    },
    async fetchPlayerListings() {
      const res = await axiosClient.get("/market/player/active");
      this.playerListings = res.data;
    },
    async fetchMyListings() {
      const res = await axiosClient.get("/market/player/my");
      this.myListings = res.data;
    },

    // --- GIAO DỊCH SHOP ---
    async buyItem(id, qty) {
      await axiosClient.post("/shop/buy", {
        itemId: id,
        quantity: qty,
      });
      await this.refresh();
    },

    async sellItem(id, qty) {
      const res = await axiosClient.post("/shop/sell", {
        userItemId: id,
        quantity: qty,
      });
      await this.refresh();
      return res.data; 
    },

    // --- GIAO DỊCH CHỢ TRỜI ---
    async buyPlayerListing(id, qty) {
      await axiosClient.post("/market/player/buy", {
        listingId: id,
        quantity: qty,
      });
      await this.refresh();
    },

    // [UPDATED] Thêm return data để UI hiển thị thông báo
    async createListing(id, price, qty) {
      const res = await axiosClient.post("/market/player/list", {
        userItemId: id,
        price: price,
        quantity: qty,
      });
      await this.refresh(); // Refresh cả Inventory (trừ đồ) và MyListings
      return res.data;
    },

    async cancelListing(id) {
      await axiosClient.post(`/market/player/cancel/${id}`);
      await this.refresh(); // Refresh để trả đồ về Inventory
    },

    async refresh() {
      await Promise.all([
        this.fetchShopItems(),
        this.fetchPlayerListings(),
        this.fetchMyListings(),
        useInventoryStore().fetchInventory(),
        useAuthStore().fetchProfile(),
      ]);
    },
  },
});