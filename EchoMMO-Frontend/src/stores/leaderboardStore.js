import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

export const useLeaderboardStore = defineStore("leaderboard", {
  state: () => ({
    topLevels: [],
    topWealth: [],
    topMonsters: [],
    topPvp: [], // Danh sách BXH PvP

    // Loading states
    loadingLevels: false,
    loadingWealth: false,
    loadingMonsters: false,
    loadingPvp: false,

    // Error states
    errorLevels: null,
    errorWealth: null,
    errorMonsters: null,
    errorPvp: null,
  }),

  actions: {
    // --- 1. TOP CAO THỦ (LEVEL) ---
    async fetchLevelBoard() {
      this.loadingLevels = true;
      this.errorLevels = null;
      try {
        const res = await axiosClient.get("/leaderboard/level");
        this.topLevels = this.extractData(res.data);
      } catch (error) {
        console.error("Lỗi tải BXH Level:", error);
        this.errorLevels = error.message;
      } finally {
        this.loadingLevels = false;
      }
    },

    // --- 2. TOP PHÚ HỘ (WEALTH) ---
    async fetchWealthBoard() {
      this.loadingWealth = true;
      this.errorWealth = null;
      try {
        const res = await axiosClient.get("/leaderboard/wealth");
        this.topWealth = this.extractData(res.data);
      } catch (error) {
        console.error("Lỗi tải BXH Phú Hộ:", error);
        this.errorWealth = error.message;
      } finally {
        this.loadingWealth = false;
      }
    },

    // --- 3. TOP TRẢM YÊU (MONSTER KILLS) ---
    async fetchMonsterBoard() {
      this.loadingMonsters = true;
      this.errorMonsters = null;
      try {
        const res = await axiosClient.get("/leaderboard/monster");
        this.topMonsters = this.extractData(res.data);
      } catch (error) {
        console.error("Lỗi tải BXH Trảm Yêu:", error);
        this.errorMonsters = error.message;
      } finally {
        this.loadingMonsters = false;
      }
    },

    // --- 4. TOP VÕ ĐÀI (PVP & DANH VỌNG) ---
    async fetchPvPBoard() {
      this.loadingPvp = true;
      this.errorPvp = null;
      try {
        // [FIX 1] Bỏ chữ "/api" ở đầu vì axiosClient đã có baseURL rồi
        // Đường dẫn này khớp với controller PvP: @RequestMapping("/api/pvp") + @GetMapping("/leaderboard")
        const res = await axiosClient.get("/pvp/leaderboard");
        
        // [FIX 2] Sử dụng hàm helper để lấy dữ liệu dù là Array hay Page
        this.topPvp = this.extractData(res.data);
        
      } catch (error) {
        console.error("Lỗi tải BXH PvP:", error);
        this.errorPvp = error.message;
        this.topPvp = []; 
      } finally {
        this.loadingPvp = false;
      }
    },

    // --- HELPER: Xử lý dữ liệu trả về từ Spring Boot ---
    extractData(responseData) {
      if (!responseData) return [];
      
      // Trường hợp 1: Trả về mảng trực tiếp [{}, {}]
      if (Array.isArray(responseData)) {
        return responseData;
      }
      
      // Trường hợp 2: Trả về Spring Data Page { content: [{}, {}], ... }
      if (responseData.content && Array.isArray(responseData.content)) {
        return responseData.content;
      }

      // Trường hợp 3: Trả về wrapper { data: [...] }
      if (responseData.data && Array.isArray(responseData.data)) {
        return responseData.data;
      }

      return [];
    }
  },
});