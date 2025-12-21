// import { defineStore } from "pinia";
// import axiosClient from "../api/axiosClient";
// import { useCharacterStore } from "./characterStore";

// export const useInventoryStore = defineStore("inventory", {
//   state: () => ({
//     items: [],
//     isLoading: false,
//   }),

//   getters: {
//     equippedItems: (state) => {
//       // Khởi tạo các slot rỗng
//       const slots = {
//         WEAPON: null,
//         ARMOR: null,
//         HELMET: null,
//         BOOTS: null,
//         RING: null,
//         NECKLACE: null,
//       };

//       // Duyệt qua inventory để map vào slot
//       if (state.items && state.items.length > 0) {
//         state.items.forEach((userItem) => {
//           // Kiểm tra null safety cho item
//           if (userItem.isEquipped && userItem.item) {
//             const type = userItem.item.type;
//             // Chỉ gán nếu type đó có tồn tại trong slots định nghĩa ở trên
//             if (Object.prototype.hasOwnProperty.call(slots, type)) {
//               slots[type] = userItem;
//             }
//           }
//         });
//       }
//       return slots;
//     },
//   },

//   actions: {
//     async fetchInventory() {
//       this.isLoading = true;
//       try {
//         const res = await axiosClient.get("/equipment/inventory");
//         this.items = res.data;
//       } catch (error) {
//         console.error("Lỗi lấy Inventory:", error);
//         if (
//           error.response &&
//           (error.response.status === 401 || error.response.status === 403)
//         ) {
//           // alert("Phiên đăng nhập hết hạn, vui lòng đăng nhập lại.");
//         }
//       } finally {
//         this.isLoading = false;
//       }
//     },

//     async equipItem(id) {
//       const charStore = useCharacterStore();
//       try {
//         await axiosClient.post(`/equipment/equip/${id}`);
//         await Promise.all([this.fetchInventory(), charStore.fetchCharacter()]);
//       } catch (e) {
//         console.error(e);
//         alert(e.response?.data || "Lỗi trang bị vật phẩm");
//       }
//     },

//     async unequipItem(id) {
//       const charStore = useCharacterStore();
//       try {
//         await axiosClient.post(`/equipment/unequip/${id}`);
//         await Promise.all([this.fetchInventory(), charStore.fetchCharacter()]);
//       } catch (e) {
//         console.error(e);
//         alert(e.response?.data || "Lỗi tháo trang bị");
//       }
//     },

//     async useItem(id) {
//       const charStore = useCharacterStore();
//       try {
//         const res = await axiosClient.post(`/equipment/use/${id}`);
//         alert(res.data || "Sử dụng thành công");
//         await Promise.all([this.fetchInventory(), charStore.fetchCharacter()]);
//       } catch (e) {
//         console.error(e);
//         alert(e.response?.data || "Lỗi sử dụng vật phẩm");
//       }
//     },
//   },
// });


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
      // Khởi tạo các slot rỗng
      const slots = {
        WEAPON: null,
        ARMOR: null,
        HELMET: null,
        BOOTS: null,
        RING: null,
        NECKLACE: null,
      };

      // Duyệt qua inventory để map vào slot
      if (state.items && state.items.length > 0) {
        state.items.forEach((userItem) => {
          // Kiểm tra null safety cho item
          if (userItem.isEquipped && userItem.item) {
            const type = userItem.item.type;
            // Chỉ gán nếu type đó có tồn tại trong slots định nghĩa ở trên
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
        const res = await axiosClient.get("/equipment/inventory");
        
        // [FIX] Map dữ liệu từ Backend để khớp tên biến Frontend
        // Backend trả về: enhancementLevel -> Frontend dùng: enhanceLevel / level
        this.items = res.data.map((item) => ({
          ...item,
          enhanceLevel: item.enhancementLevel || 0,
          level: item.enhancementLevel || 0, // Alias dự phòng cho các component cũ
        }));

      } catch (error) {
        console.error("Lỗi lấy Inventory:", error);
        if (
          error.response &&
          (error.response.status === 401 || error.response.status === 403)
        ) {
          // Có thể thêm logic logout hoặc refresh token ở đây nếu cần
        }
      } finally {
        this.isLoading = false;
      }
    },

    async equipItem(id) {
      const charStore = useCharacterStore();
      try {
        await axiosClient.post(`/equipment/equip/${id}`);
        // Refresh cả inventory lẫn thông tin nhân vật (để update chỉ số)
        await Promise.all([this.fetchInventory(), charStore.fetchCharacter()]);
      } catch (e) {
        console.error(e);
        alert(e.response?.data || "Lỗi trang bị vật phẩm");
      }
    },

    async unequipItem(id) {
      const charStore = useCharacterStore();
      try {
        await axiosClient.post(`/equipment/unequip/${id}`);
        await Promise.all([this.fetchInventory(), charStore.fetchCharacter()]);
      } catch (e) {
        console.error(e);
        alert(e.response?.data || "Lỗi tháo trang bị");
      }
    },

    async useItem(id) {
      const charStore = useCharacterStore();
      try {
        const res = await axiosClient.post(`/equipment/use/${id}`);
        alert(res.data || "Sử dụng thành công");
        await Promise.all([this.fetchInventory(), charStore.fetchCharacter()]);
      } catch (e) {
        console.error(e);
        alert(e.response?.data || "Lỗi sử dụng vật phẩm");
      }
    },
  },
});