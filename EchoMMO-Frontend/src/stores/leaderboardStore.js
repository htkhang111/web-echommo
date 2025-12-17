import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

export const useLeaderboardStore = defineStore("leaderboard", {
  state: () => ({
    topLevels: [],
    topWealth: [],
    
    // 1. Separate loading states for better UX
    loadingLevels: false,
    loadingWealth: false,

    // 2. Add error states to display alerts in the UI
    errorLevels: null,
    errorWealth: null,
  }),

  actions: {
    async fetchLevelBoard() {
      this.loadingLevels = true;
      this.errorLevels = null; // Reset error on new request

      try {
        const res = await axiosClient.get("/leaderboard/level");
        this.topLevels = res.data;
      } catch (error) {
        console.error("Failed to fetch level leaderboard:", error);
        this.errorLevels = error.message || "Failed to load level rankings.";
      } finally {
        this.loadingLevels = false;
      }
    },

    async fetchWealthBoard() {
      this.loadingWealth = true;
      this.errorWealth = null; // Reset error on new request

      try {
        const res = await axiosClient.get("/leaderboard/wealth");
        this.topWealth = res.data;
      } catch (error) {
        console.error("Failed to fetch wealth leaderboard:", error);
        this.errorWealth = error.message || "Failed to load wealth rankings.";
      } finally {
        this.loadingWealth = false;
      }
    },
    
    // Optional: A convenience action to fetch both at once
    async fetchAll() {
        await Promise.all([this.fetchLevelBoard(), this.fetchWealthBoard()]);
    }
  },
});