import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

export const useAdminStore = defineStore("admin", {
  state: () => ({
    stats: {
      totalUsers: 0,
      totalItems: 0,
      totalEchoMined: 0,
      totalGold: 0,
    },
    users: [],
    items: [],
    loading: false,
    error: null,
  }),

  actions: {
    async fetchStats() {
      this.loading = true;
      try {
        const res = await axiosClient.get("/admin/stats");
        this.stats = res.data;
      } catch (err) {
        this.error = err.response?.data?.message || "Lỗi tải thống kê";
        console.error(err);
      } finally {
        this.loading = false;
      }
    },

    async fetchUsers() {
      this.loading = true;
      try {
        const res = await axiosClient.get("/admin/users");
        this.users = res.data;
      } catch (err) {
        this.error = "Lỗi tải Users";
      } finally {
        this.loading = false;
      }
    },

    async banUser(userId, reason) {
      await axiosClient.post(`/admin/user/ban/${userId}`, { reason });
      await this.fetchUsers();
    },

    async unbanUser(userId) {
      await axiosClient.post(`/admin/user/unban/${userId}`);
      await this.fetchUsers();
    },

    async deleteUser(userId) {
      if (!confirm("Xóa vĩnh viễn user này?")) return;
      await axiosClient.delete(`/admin/user/${userId}`);
      await this.fetchUsers();
    },

    async fetchItems() {
      this.loading = true;
      try {
        const res = await axiosClient.get("/admin/items");
        this.items = res.data;
      } catch (err) {
        this.error = "Lỗi tải Items";
      } finally {
        this.loading = false;
      }
    },

    async createItem(itemData) {
      await axiosClient.post("/admin/item/create", itemData);
      await this.fetchItems();
    },

    async deleteItem(itemId) {
      if (!confirm("Xóa item này?")) return;
      await axiosClient.delete(`/admin/item/${itemId}`);
      await this.fetchItems();
    },

    // --- Rewards ---
    async grantGold(username, amount) {
      await axiosClient.post("/admin/grant-gold", { username, amount });
    },

    // [NEW] Action phát EchoCoin
    async grantEcho(username, amount) {
      await axiosClient.post("/admin/grant-echo", { username, amount });
    },

    async grantItem(username, itemId, quantity) {
      await axiosClient.post("/admin/grant-item", {
        username,
        itemId,
        quantity,
      });
    },

    async sendNotification(payload) {
      await axiosClient.post("/admin/notification/create", payload);
    },
  },
});
