import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";
import { useCharacterStore } from "./characterStore";

export const useBattleStore = defineStore("battle", {
  state: () => ({
    enemy: null,
    enemyVisual: null, // [THÊM MỚI] Để lưu hình ảnh/tên quái từ màn hình Encounter
    enemyHp: 0,
    enemyMaxHp: 100,
    playerHp: 0,
    playerMaxHp: 100,
    combatLogs: [],
    status: "IDLE",
    goldEarned: 0,
    expEarned: 0,
    droppedItem: null,
    isReady: false,
  }),

  actions: {
    // [THÊM MỚI] Hàm này để Component gọi khi gặp quái
    setEncounter(visualData) {
      this.enemyVisual = visualData;
    },

    async startBattle() {
      this.status = "LOADING";
      this.isReady = false;
      this.combatLogs = [];

      try {
        const res = await axiosClient.post("/battle/start");
        this.handleResult(res.data);
        this.isReady = true;
      } catch (e) {
        this.status = "ERROR";
        this.combatLogs.push("Lỗi kết nối: " + (e.response?.data || e.message));
        console.error(e);
      }
    },

    async autoTurn(isBuffed) {
      if (!this.isReady || !this.enemy || !this.enemy.enemyId) return null;

      try {
        const res = await axiosClient.post("/battle/attack", {
          enemyId: this.enemy.enemyId,
          enemyHp: this.enemyHp,
          isBuffed: isBuffed,
        });
        this.handleResult(res.data);
        return res.data;
      } catch (e) {
        console.error("Lỗi turn:", e);

        if (
          e.response &&
          (e.response.status === 403 || e.response.status === 401)
        ) {
          this.status = "ERROR";
          this.combatLogs.push(
            "Phiên đăng nhập hết hạn hoặc lỗi quyền truy cập.",
          );
          this.isReady = false;
        }
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

      if (data.enemyId) {
        this.enemy = {
          enemyId: data.enemyId,
          name: data.enemyName,
          level: data.enemyLv || "??",
        };
      } else if (data.enemy) {
        this.enemy = data.enemy;
      }

      this.enemyHp = data.enemyHp;
      this.enemyMaxHp = data.enemyMaxHp || this.enemyMaxHp;
      this.playerHp = data.playerHp;
      this.playerMaxHp = data.playerMaxHp || this.playerMaxHp;
      this.status = data.status;

      if (data.combatLog && Array.isArray(data.combatLog)) {
        this.combatLogs = [...this.combatLogs, ...data.combatLog].slice(-20);
      }

      if (data.status === "VICTORY") {
        this.goldEarned = data.goldEarned;
        this.expEarned = data.expEarned;
        if (data.droppedItemName) {
          this.droppedItem = {
            name: data.droppedItemName,
            image: data.droppedItemImage,
            rarity: data.droppedItemRarity,
          };
        }
        this.isReady = false;
      } else if (data.status === "DEFEAT") {
        this.isReady = false;
      }

      const charStore = useCharacterStore();
      if (charStore.character) {
        charStore.character.hp = data.playerHp;
      }
    },

    resetBattle() {
      this.status = "IDLE";
      this.enemy = null;
      this.enemyVisual = null; // [SỬA] Reset cả cái này
      this.combatLogs = [];
      this.isReady = false;
      this.droppedItem = null;
    },
  },
});
