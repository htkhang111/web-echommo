import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

export const useAdminStore = defineStore("admin", {
  state: () => ({
    stats: {},
    users: [],
    items: [],
    listings: [],
  }),
  actions: {
    async fetchStats() {
      try {
        const res = await axiosClient.get("/admin/stats");
        this.stats = res.data;
      } catch (e) {
        console.error(e);
      }
    },
    async fetchUsers() {
      try {
        const res = await axiosClient.get("/admin/users");
        this.users = res.data;
      } catch (e) {
        console.error(e);
      }
    },
    async fetchItems() {
      try {
        const res = await axiosClient.get("/admin/items");
        this.items = res.data;
      } catch (e) {
        console.error(e);
      }
    },
    async fetchListings() {
      try {
        const res = await axiosClient.get("/admin/listings");
        this.listings = res.data;
      } catch (e) {
        console.error(e);
      }
    },

    // --- Logic Xử Lý User ---
    async banUser(id, reason) {
      // Để component xử lý try-catch và popup
      await axiosClient.post(`/admin/user/ban/${id}`, { reason });
      await this.fetchUsers();
    },
    async unbanUser(id) {
      await axiosClient.post(`/admin/user/unban/${id}`);
      await this.fetchUsers();
    },
    async deleteUser(id) {
      // Bỏ confirm ở đây, Component đã lo vụ hỏi xác nhận rồi
      await axiosClient.delete(`/admin/user/${id}`);
      await this.fetchUsers();
    },

    // --- Logic Xử Lý Item/Listing ---
    async deleteItem(id) {
      await axiosClient.delete(`/admin/item/${id}`);
      await this.fetchItems();
    },
    async deleteListing(id) {
      await axiosClient.delete(`/admin/listing/${id}`);
      await this.fetchListings();
    },

    // --- Logic Ban Thưởng & Thông Báo ---
    // Bỏ try-catch và alert, để lỗi bắn ra cho Admin.vue bắt lấy
    async grantGold(payload) {
      await axiosClient.post("/admin/grant-gold", payload);
    },

    async grantItem(payload) {
      await axiosClient.post("/admin/grant-item", payload);
    },

    async sendNotification(payload) {
      await axiosClient.post("/admin/notification/create", payload);
    },
  },
});
