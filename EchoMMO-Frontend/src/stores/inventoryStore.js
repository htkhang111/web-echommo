import { defineStore } from "pinia";
import axiosClient from "@/api/axiosClient";
import { useAuthStore } from "./authStore";

export const useInventoryStore = defineStore("inventory", {
  state: () => ({
    items: [],
    loading: false,
    error: null,
  }),

  actions: {
    async fetchInventory() {
      // [FIX] Kiểm tra Auth trước khi gọi API
      const authStore = useAuthStore();
      if (!authStore.token) {
        console.warn("🛑 InventoryStore: Không có token, hủy gọi API lấy đồ.");
        this.items = []; // Reset đồ về rỗng cho an toàn
        return;
      }

      this.loading = true;
      try {
        const res = await axiosClient.get("/inventory/items");
        this.items = res.data;
      } catch (err) {
        // Chỉ log lỗi nếu không phải do hết hạn token (đã xử lý ở axios)
        if (err.response?.status !== 401) {
             this.error = err.response?.data || "Lỗi lấy túi đồ";
             console.error(err);
        }
      } finally {
        this.loading = false;
      }
    },

    async equipItem(userItemId) {
      try {
        await axiosClient.post(`/inventory/equip/${userItemId}`);
        await this.fetchInventory();
      } catch (err) {
        throw err.response?.data || "Lỗi trang bị";
      }
    },

    async unequipItem(userItemId) {
      try {
        await axiosClient.post(`/inventory/unequip/${userItemId}`);
        await this.fetchInventory();
      } catch (err) {
        throw err.response?.data || "Lỗi tháo đồ";
      }
    },

    async useItem(userItemId) {
      try {
        const res = await axiosClient.post(`/inventory/use/${userItemId}`);
        await this.fetchInventory();
        return res.data;
      } catch (err) {
        throw err.response?.data || "Lỗi sử dụng";
      }
    },

    async expandInventory() {
      try {
        const res = await axiosClient.post("/inventory/expand");
        const authStore = useAuthStore();
        await authStore.fetchProfile();
        return res.data;
      } catch (err) {
        throw err.response?.data || "Lỗi mở rộng";
      }
    },

    // [NEW] SỬA ĐỒ
    async repairItem(userItemId) {
      try {
        const res = await axiosClient.post(`/inventory/repair/${userItemId}`);

        const updatedItem = this.items.find((i) => i.userItemId === userItemId);
        if (updatedItem && updatedItem.maxDurability) {
          updatedItem.currentDurability = updatedItem.maxDurability;
        }

        const authStore = useAuthStore();
        await authStore.fetchProfile();

        return res.data;
      } catch (err) {
        throw err.response?.data || "Lỗi sửa đồ";
      }
    },
  },
}); 