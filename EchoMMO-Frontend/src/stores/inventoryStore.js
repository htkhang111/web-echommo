import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";
import { useCharacterStore } from "./characterStore";

export const useInventoryStore = defineStore("inventory", {
  state: () => ({
    items: [],
    isLoading: false,
  }),

  getters: {
    equippedItems: (state) => {
      const slots = {
        WEAPON: null,
        ARMOR: null,
        HELMET: null,
        BOOTS: null,
        RING: null,
        NECKLACE: null,
      };

      if (state.items && state.items.length > 0) {
        state.items.forEach((userItem) => {
          if (userItem.isEquipped && userItem.item) {
            const type = userItem.item.type; // Backend trả về type (WEAPON, etc)
            if (Object.prototype.hasOwnProperty.call(slots, type)) {
              slots[type] = userItem;
            }
          }
        });
      }
      return slots;
    },
  },

  actions: {
    async fetchInventory() {
      this.isLoading = true;
      try {
        // [FIX QUAN TRỌNG] Đổi đường dẫn API cho đúng với InventoryController
        const res = await axiosClient.get("/inventory/items");
        this.items = res.data;
      } catch (error) {
        console.error("Lỗi lấy Inventory:", error);
      } finally {
        this.isLoading = false;
      }
    },

    async equipItem(id) {
      const charStore = useCharacterStore();
      try {
        // [FIX] endpoint: /inventory/equip
        await axiosClient.post(`/inventory/equip/${id}`);
        await Promise.all([this.fetchInventory(), charStore.fetchCharacter()]);
      } catch (e) {
        console.error(e);
        alert(e.response?.data || "Lỗi trang bị vật phẩm");
      }
    },

    async unequipItem(id) {
      const charStore = useCharacterStore();
      try {
        // [FIX] endpoint: /inventory/unequip
        await axiosClient.post(`/inventory/unequip/${id}`);
        await Promise.all([this.fetchInventory(), charStore.fetchCharacter()]);
      } catch (e) {
        console.error(e);
        alert(e.response?.data || "Lỗi tháo trang bị");
      }
    },

    async useItem(id) {
      const charStore = useCharacterStore();
      try {
        // [FIX] endpoint: /inventory/use
        const res = await axiosClient.post(`/inventory/use/${id}`);
        alert(res.data || "Sử dụng thành công");
        await Promise.all([this.fetchInventory(), charStore.fetchCharacter()]);
      } catch (e) {
        console.error(e);
        alert(e.response?.data || "Lỗi sử dụng vật phẩm");
      }
    },
    
    async expandInventory() {
        try {
            // [FIX] endpoint: /inventory/expand
            const res = await axiosClient.post("/inventory/expand");
            alert(res.data);
            await this.fetchInventory();
        } catch (e) {
            alert(e.response?.data || "Lỗi mở rộng kho");
        }
    }
  },
});