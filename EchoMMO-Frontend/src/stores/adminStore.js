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
      try {
        await axiosClient.post(`/admin/user/ban/${id}`, { reason });
        await this.fetchUsers();
      } catch (error) {
        console.error("Lỗi cấm người dùng:", error);
        throw error;
      }
    },
    async unbanUser(id) {
      try {
        await axiosClient.post(`/admin/user/unban/${id}`);
        await this.fetchUsers();
      } catch (error) {
        console.error("Lỗi bỏ cấm người dùng:", error);
        throw error;
      }
    },
    async deleteUser(id) {
      try {
        await axiosClient.delete(`/admin/user/${id}`);
        await this.fetchUsers();
      } catch (error) {
        console.error("Lỗi xóa người dùng:", error);
        throw error;
      }
    },

    // --- Logic Xử Lý Item/Listing ---
    async deleteItem(id) {
      try {
        await axiosClient.delete(`/admin/item/${id}`);
        await this.fetchItems();
      } catch (error) {
        console.error("Lỗi xóa vật phẩm:", error);
        throw error;
      }
    },
    async deleteListing(id) {
      try {
        await axiosClient.delete(`/admin/listing/${id}`);
        await this.fetchListings();
      } catch (error) {
        console.error("Lỗi xóa tin rao bán:", error);
        throw error;
      }
    },

    // --- Logic Ban Thưởng & Thông Báo ---
    async grantGold(payload) {
      try {
        await axiosClient.post("/admin/grant-gold", payload);
      } catch (error) {
        console.error("Lỗi tặng vàng:", error);
        throw error;
      }
    },

    async grantItem(payload) {
      try {
        await axiosClient.post("/admin/grant-item", payload);
      } catch (error) {
        console.error("Lỗi tặng vật phẩm:", error);
        throw error;
      }
    },

    async sendNotification(payload) {
      try {
        await axiosClient.post("/admin/notification/create", payload);
      } catch (error) {
        console.error("Lỗi gửi thông báo:", error);
        throw error;
      }
    },
  },
});
