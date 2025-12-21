import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

export const useLeaderboardStore = defineStore("leaderboard", {
  state: () => ({
    topLevels: [],
    topWealth: [],
    topMonsters: [],
    topPvp: [], // [MỚI] Lưu danh sách thắng PvP

    // Loading states
    loadingLevels: false,
    loadingWealth: false,
    loadingMonsters: false,
    loadingPvp: false, // [MỚI]

    // Error states
    errorLevels: null,
    errorWealth: null,
    errorMonsters: null,
    errorPvp: null, // [MỚI]
  }),

  actions: {
    // 1. TOP CAO THỦ
    async fetchLevelBoard() {
      this.loadingLevels = true;
      this.errorLevels = null;
      try {
        const res = await axiosClient.get("/leaderboard/level");
        this.topLevels = res.data || [];
      } catch (error) {
        this.errorLevels = error.message;
      } finally {
        this.loadingLevels = false;
      }
    },

    // 2. TOP PHÚ HỘ
    async fetchWealthBoard() {
      this.loadingWealth = true;
      this.errorWealth = null;
      try {
        const res = await axiosClient.get("/leaderboard/wealth");
        this.topWealth = res.data || [];
      } catch (error) {
        this.errorWealth = error.message;
      } finally {
        this.loadingWealth = false;
      }
    },

    // 3. TOP TRẢM YÊU
    async fetchMonsterBoard() {
      this.loadingMonsters = true;
      this.errorMonsters = null;
      try {
        const res = await axiosClient.get("/leaderboard/monster");
        this.topMonsters = res.data || [];
      } catch (error) {
        this.errorMonsters = error.message;
      } finally {
        this.loadingMonsters = false;
      }
    },

    // [MỚI] 4. TOP CHIẾN THẦN (PVP WINS)
    // Hàm này sẽ giúp tab PvP của LuxxMel hiện dữ liệu
    async fetchPvPBoard() {
      this.loadingPvp = true;
      this.errorPvp = null;
      try {
        // Gọi đến endpoint /pvp/leaderboard mà chúng ta đã viết ở Backend
        const res = await axiosClient.get("/pvp/leaderboard");
        
        // Vì giao diện Thiên Thư dùng biến 'username', 'avatar', 'value'
        // Nên chúng ta map lại dữ liệu từ Backend trả về cho chuẩn
        this.topPvp = (res.data || []).map(item => ({
          username: item.name,        // Tên nhân vật
          avatar: item.avatarUrl,     // Ảnh đại diện
          value: item.pvpWins + " Trận Thắng" // Hiển thị giá trị
        }));
      } catch (error) {
        console.error("Lỗi fetch PvP:", error);
        this.errorPvp = error.message;
      } finally {
        this.loadingPvp = false;
      }
    },
  },
});