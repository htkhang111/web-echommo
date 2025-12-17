import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

export const useLeaderboardStore = defineStore("leaderboard", {
  state: () => ({
    topLevels: [],
    topWealth: [],
    topMonsters: [], // [QUAN TRỌNG] Phải khởi tạo mảng rỗng để tránh lỗi .length

    // Loading states
    loadingLevels: false,
    loadingWealth: false,
    loadingMonsters: false, // Thêm cái này

    // Error states
    errorLevels: null,
    errorWealth: null,
    errorMonsters: null, // Thêm cái này
  }),

  actions: {
    async fetchLevelBoard() {
      this.loadingLevels = true;
      this.errorLevels = null;
      try {
        const res = await axiosClient.get("/leaderboard/level");
        this.topLevels = res.data || [];
      } catch (error) {
        console.error(error);
        this.errorLevels = error.message;
      } finally {
        this.loadingLevels = false;
      }
    },

    async fetchWealthBoard() {
      this.loadingWealth = true;
      this.errorWealth = null;
      try {
        const res = await axiosClient.get("/leaderboard/wealth");
        this.topWealth = res.data || [];
      } catch (error) {
        console.error(error);
        this.errorWealth = error.message;
      } finally {
        this.loadingWealth = false;
      }
    },

    // [FIX LỖI] Thêm hàm này để Component gọi được
    async fetchMonsterBoard() {
      this.loadingMonsters = true;
      this.errorMonsters = null;
      try {
        // Gọi đúng endpoint backend chúng ta đã viết: /leaderboard/monster
        const res = await axiosClient.get("/leaderboard/monster");
        this.topMonsters = res.data || [];
      } catch (error) {
        console.error(error);
        this.errorMonsters = error.message;
      } finally {
        this.loadingMonsters = false;
      }
    },
  },
});