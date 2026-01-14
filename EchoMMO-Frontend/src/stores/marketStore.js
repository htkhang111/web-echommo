// File: EchoMMO-Frontend/src/stores/marketStore.js

import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient"; // Shipper chuyên đi gọi API Backend
import { useInventoryStore } from "./inventoryStore"; // Gọi Store Túi đồ để update khi mua/bán
import { useAuthStore } from "./authStore"; // Gọi Store User để update tiền nong

export const useMarketStore = defineStore("market", {
  // === 1. STATE (KHO LƯU TRỮ) ===
  // Nơi chứa dữ liệu tải từ Server về để hiển thị lên màn hình
  state: () => ({
    shopItems: [],      // Danh sách đồ NPC bán (Shop hệ thống)
    playerListings: [], // Danh sách đồ người chơi khác treo bán (Chợ trời)
    myListings: [],     // Danh sách đồ mình đang treo bán
    isLoading: false,   // Trạng thái đang tải (để hiện vòng xoay xoay nếu cần)
  }),

  // === 2. ACTIONS (CÁC HÀNH ĐỘNG) ===
  // Các hàm này sẽ được gọi từ giao diện (file .vue)
  actions: {
    // --- LẤY DỮ LIỆU (GET) ---
    
    // Lấy danh sách đồ trong Shop NPC
    async fetchShopItems() {
      // Gọi API: GET /api/shop/items
      const res = await axiosClient.get("/shop/items");
      this.shopItems = res.data; // Lưu dữ liệu server trả về vào State
    },
    
    // Lấy danh sách đồ đang bán trên Chợ Trời (Active)
    async fetchPlayerListings() {
      // Gọi API: GET /api/market/player/active
      const res = await axiosClient.get("/market/player/active");
      this.playerListings = res.data;
    },
    
    // Lấy danh sách đồ MÌNH đang treo bán
    async fetchMyListings() {
      // Gọi API: GET /api/market/player/my
      const res = await axiosClient.get("/market/player/my");
      this.myListings = res.data;
    },

    // --- GIAO DỊCH VỚI NPC (SHOP HỆ THỐNG) ---
    
    // Mua đồ từ Shop
    async buyItem(id, qty) {
      // Gọi API: POST /api/shop/buy
      await axiosClient.post("/shop/buy", {
        itemId: id,
        quantity: qty,
      });
      // Mua xong thì phải Refresh lại toàn bộ (trừ tiền, cộng đồ...)
      await this.refresh();
    },

    // Bán đồ rác vào Shop
    async sellItem(id, qty) {
      // Gọi API: POST /api/shop/sell
      const res = await axiosClient.post("/shop/sell", {
        userItemId: id,
        quantity: qty,
      });
      await this.refresh(); // Bán xong thì update lại túi đồ + ví tiền
      return res.data; 
    },

    // --- GIAO DỊCH CHỢ TRỜI (P2P) ---
    
    // Mua đồ của người chơi khác
    async buyPlayerListing(id, qty) {
      // Gọi API: POST /api/market/player/buy
      await axiosClient.post("/market/player/buy", {
        listingId: id,
        quantity: qty,
      });
      await this.refresh();
    },

    // Treo bán đồ của mình lên chợ
    // [UPDATED] Thêm return data để UI hiển thị thông báo
    async createListing(id, price, qty) {
      // Gọi API: POST /api/market/player/list
      const res = await axiosClient.post("/market/player/list", {
        userItemId: id, // ID món đồ trong túi
        price: price,   // Giá muốn bán
        quantity: qty,  // Số lượng bán
      });
      await this.refresh(); // Treo bán xong thì món đồ phải biến mất khỏi túi (Inventory)
      return res.data;
    },

    // Hủy bán (Thu hồi đồ về túi)
    async cancelListing(id) {
      // Gọi API: POST /api/market/player/cancel/{id}
      await axiosClient.post(`/market/player/cancel/${id}`);
      await this.refresh(); // Hủy xong thì đồ phải hiện lại trong túi
    },

    // --- HÀM ĐA NĂNG (REFRESH ALL) ---
    // Hàm này cực quan trọng: Nó chạy song song tất cả các lệnh lấy dữ liệu
    // Để đảm bảo sau khi mua bán, giao diện cập nhật đồng bộ 100%
    async refresh() {
      // Promise.all giúp chạy 5 API này CÙNG LÚC (tiết kiệm thời gian chờ)
      await Promise.all([
        this.fetchShopItems(),        // 1. Tải lại shop NPC (phòng khi có thay đổi)
        this.fetchPlayerListings(),   // 2. Tải lại chợ trời (phòng khi ai đó vừa mua mất)
        this.fetchMyListings(),       // 3. Tải lại sạp hàng của mình
        useInventoryStore().fetchInventory(), // 4. Tải lại túi đồ (để thấy đồ mới mua/mất đồ vừa bán)
        useAuthStore().fetchProfile(),        // 5. Tải lại ví tiền (để thấy tiền tăng/giảm)
      ]);
    },
  },
});