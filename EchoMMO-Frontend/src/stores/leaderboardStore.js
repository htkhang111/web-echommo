import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

export const useLeaderboardStore = defineStore("leaderboard", {
  state: () => ({
    topLevels: [],
    topWealth: [],
    isLoading: false,
  }),
  actions: {
    async fetchLevelBoard() {
      this.isLoading = true;
      try {
        const res = await axiosClient.get("/leaderboard/level");
        this.topLevels = res.data;
      } catch (error) {
        console.error(error);
      } finally {
        this.isLoading = false;
      }
    },
    async fetchWealthBoard() {
      this.isLoading = true;
      try {
        const res = await axiosClient.get("/leaderboard/wealth");
        this.topWealth = res.data;
      } catch (error) {
        console.error(error);
      } finally {
        this.isLoading = false;
      }
    },
  },
});
