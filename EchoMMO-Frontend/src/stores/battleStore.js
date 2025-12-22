import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";
import { useCharacterStore } from "./characterStore";

export const useBattleStore = defineStore("battle", {
  state: () => ({
    enemy: null,
    enemyVisual: null,

    enemyHp: 0,
    enemyMaxHp: 100,
    playerHp: 0,
    playerMaxHp: 100,

    combatLogs: [],
    status: "IDLE",

    rewardGold: 0,
    rewardExp: 0,
    droppedItem: null,

    isReady: false,
  }),

  actions: {
    setEncounter(visualData) {
      this.enemyVisual = visualData;
    },

    async startBattle() {
      this.status = "LOADING";
      this.isReady = false;
      this.combatLogs = [];
      this.resetRewards();

      try {
        const res = await axiosClient.post("/battle/start");
        this.handleResult(res.data);
        this.isReady = true;
      } catch (e) {
        this.status = "ERROR";
        const msg = e.response?.data?.message || "Không thể tải trận đấu.";
        this.combatLogs.push(msg);
        console.error(e);
      }
    },

    // [FIX QUAN TRỌNG] Thêm tham số playerStats để nhận chỉ số tổng từ Vue Component
    async autoTurn(isBuffed, playerStats = null) {
      if (!this.isReady) return null;

      try {
        // Tạo payload gửi lên server
        const payload = {
          enemyId: this.enemy?.enemyId || 0,
          isBuffed: isBuffed,
        };

        // Nếu có chỉ số từ frontend (đã cộng đồ), gửi kèm theo
        if (playerStats) {
          payload.playerStats = playerStats;
        }

        const res = await axiosClient.post("/battle/attack", payload);
        this.handleResult(res.data);
        return res.data;
      } catch (e) {
        console.error("Turn Error:", e);
        this.status = "ERROR";
        this.isReady = false; // Ngắt vòng lặp ngay khi lỗi
        
        const msg = e.response?.data?.message || e.message || "Mất kết nối.";
        this.combatLogs.push("⚠️ LỖI: " + msg);
        return null;
      }
    },

    async sendAction(actionType) {
      try {
        const res = await axiosClient.post("/battle/action", {
          action: actionType,
        });
        this.handleResult(res.data);
      } catch (e) {
        console.error(e);
      }
    },

    handleResult(data) {
      if (!data) return;

      // Cập nhật thông tin Quái (giữ nguyên ID nếu server không trả về)
      if (data.enemyId || data.enemyName) {
        this.enemy = {
          enemyId: data.enemyId || (this.enemy ? this.enemy.enemyId : 0),
          name: data.enemyName || "Quái Lạ",
          level: data.enemyLv || "??",
        };
      } else if (data.enemy) {
        this.enemy = data.enemy;
      }

      // Cập nhật HP & Trạng thái
      this.enemyHp = data.enemyHp ?? this.enemyHp;
      this.enemyMaxHp = data.enemyMaxHp || this.enemyMaxHp;
      this.playerHp = data.playerHp ?? this.playerHp;
      this.playerMaxHp = data.playerMaxHp || this.playerMaxHp;
      this.status = data.status || this.status;

      // Xử lý Log chiến đấu
      if (data.combatLog) {
        if (Array.isArray(data.combatLog)) {
          this.combatLogs = [...this.combatLogs, ...data.combatLog].slice(-20);
        } else if (typeof data.combatLog === "string") {
          this.combatLogs.push(data.combatLog);
        }
      }

      // Xử lý Kết thúc trận đấu
      if (data.status === "VICTORY") {
        this.rewardGold = data.goldEarned || 0;
        this.rewardExp = data.expEarned || 0;

        if (data.droppedItemName) {
          this.droppedItem = {
            name: data.droppedItemName,
            image: data.droppedItemImage,
            rarity: data.droppedItemRarity || "COMMON",
          };
        }
        this.isReady = false;
      } else if (data.status === "DEFEAT") {
        this.isReady = false;
      }

      // Đồng bộ máu hiện tại về Character Store
      const charStore = useCharacterStore();
      if (charStore.character) {
        charStore.character.currentHp = this.playerHp;
      }
    },

    resetRewards() {
      this.rewardGold = 0;
      this.rewardExp = 0;
      this.droppedItem = null;
    },

    resetBattle() {
      this.status = "IDLE";
      this.enemy = null;
      this.enemyVisual = null;
      this.combatLogs = [];
      this.isReady = false;
      this.resetRewards();
    },
  },
});