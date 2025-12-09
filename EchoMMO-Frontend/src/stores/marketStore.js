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
      try {
        const res = await axiosClient.get("/market/shop-items");
        this.shopItems = res.data;
      } catch (error) {
        console.error("Lỗi lấy danh sách shop:", error);
        throw error;
      }
    },
    async fetchPlayerListings() {
      try {
        const res = await axiosClient.get("/market/listings");
        this.playerListings = res.data;
      } catch (error) {
        console.error("Lỗi lấy danh sách rao bán:", error);
        throw error;
      }
    },
    async fetchMyListings() {
      try {
        const res = await axiosClient.get("/market/my-listings");
        this.myListings = res.data;
      } catch (error) {
        console.error("Lỗi lấy danh sách của tôi:", error);
        throw error;
      }
    },

    // Mua từ Shop hệ thống (NPC)
    async buyItem(id, qty) {
      try {
        await axiosClient.post("/market/buy-shop", {
          itemId: id,
          quantity: qty,
        });
        await this.refresh();
      } catch (error) {
        console.error("Lỗi mua vật phẩm:", error);
        throw error;
      }
    },

    // Bán cho NPC
    async sellItem(id, qty) {
      try {
        await axiosClient.post("/market/sell", {
          userItemId: id,
          quantity: qty,
        });
        await this.refresh();
      } catch (error) {
        console.error("Lỗi bán vật phẩm:", error);
        throw error;
      }
    },

    // Đăng bán lên chợ
    async createListing(id, price, qty) {
      try {
        await axiosClient.post("/market/create", {
          userItemId: id,
          price,
          quantity: qty,
        });
        await this.refresh();
      } catch (error) {
        console.error("Lỗi tạo tin rao bán:", error);
        throw error;
      }
    },

    // Hủy bán
    async cancelListing(id) {
      try {
        await axiosClient.post(`/market/cancel/${id}`);
        await this.refresh();
      } catch (error) {
        console.error("Lỗi hủy tin rao bán:", error);
        throw error;
      }
    },

    // Mua từ người chơi (P2P)
    async buyPlayerListing(id, qty) {
      try {
        await axiosClient.post(`/market/buy/${id}`, { quantity: qty });
        await this.refresh();
      } catch (error) {
        console.error("Lỗi mua từ người chơi:", error);
        throw error;
      }
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
