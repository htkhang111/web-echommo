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
      const res = await axiosClient.get("/player-market/listings");
      this.playerListings = res.data;
    },
    async fetchMyListings() {
      const res = await axiosClient.get("/player-market/my-listings");
      this.myListings = res.data;
    },

    async buyItem(id, qty) {
      await axiosClient.post("/shop/buy", {
        itemId: id,
        quantity: qty,
      });
      await this.refresh();
    },

    async sellItem(id, qty) {
      await axiosClient.post("/shop/sell", {
        userItemId: id,
        quantity: qty,
      });
      await this.refresh();
    },

    async createListing(id, price, qty) {
      await axiosClient.post("/player-market/create", {
        userItemId: id,
        price,
        quantity: qty,
      });
      await this.refresh();
    },

    async cancelListing(id) {
      await axiosClient.post(`/player-market/cancel/${id}`);
      await this.refresh();
    },

    async buyPlayerListing(id, qty) {
      await axiosClient.post(`/player-market/buy/${id}`, { quantity: qty });
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
