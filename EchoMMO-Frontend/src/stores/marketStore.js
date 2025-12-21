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
      // Backend: ShopController chờ { itemId, quantity }
      await axiosClient.post("/shop/buy", {
        itemId: id,
        quantity: qty,
      });
      await this.refresh();
    },

    async sellItem(id, qty) {
      // Backend: ShopController chờ { userItemId, quantity }
      await axiosClient.post("/shop/sell", {
        userItemId: id,
        quantity: qty,
      });
      await this.refresh();
    },

    // --- GIAO DỊCH CHỢ TRỜI ---
    async buyPlayerListing(id, qty) {
      // Backend: PlayerMarketController chờ { listingId, quantity }
      await axiosClient.post("/market/player/buy", {
        listingId: id,
        quantity: qty,
      });
      await this.refresh();
    },

    async createListing(id, price, qty) {
      await axiosClient.post("/market/player/list", {
        userItemId: id,
        price: price,
        quantity: qty,
      });
      await this.refresh();
    },

    async cancelListing(id) {
      await axiosClient.post(`/market/player/cancel/${id}`);
      await this.refresh();
    },

    async refresh() {
      // Load lại tất cả để cập nhật tiền và túi đồ
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
