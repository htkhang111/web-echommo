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
      const res = await axiosClient.get("/market/shop-items");
      this.shopItems = res.data;
    },
    async fetchPlayerListings() {
      const res = await axiosClient.get("/market/listings");
      this.playerListings = res.data;
    },
    async fetchMyListings() {
      const res = await axiosClient.get("/market/my-listings");
      this.myListings = res.data;
    },

    // Mua từ Shop hệ thống (NPC)
    async buyItem(id, qty) {
      // Để component xử lý try-catch và hiển thị thông báo
      await axiosClient.post("/market/buy-shop", {
        itemId: id,
        quantity: qty,
      });
      await this.refresh();
    },

    // Bán cho NPC
    async sellItem(id, qty) {
      await axiosClient.post("/market/sell", {
        userItemId: id,
        quantity: qty,
      });
      await this.refresh();
    },

    // Đăng bán lên chợ
    async createListing(id, price, qty) {
      await axiosClient.post("/market/create", {
        userItemId: id,
        price,
        quantity: qty,
      });
      await this.refresh();
    },

    // Hủy bán
    async cancelListing(id) {
      await axiosClient.post(`/market/cancel/${id}`);
      await this.refresh();
    },

    // Mua từ người chơi (P2P)
    async buyPlayerListing(id, qty) {
      await axiosClient.post(`/market/buy/${id}`, { quantity: qty });
      await this.refresh();
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
