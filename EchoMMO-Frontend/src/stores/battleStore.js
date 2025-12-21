// import { defineStore } from "pinia";
// import axiosClient from "../api/axiosClient";
// import { useCharacterStore } from "./characterStore";

// export const useBattleStore = defineStore("battle", {
//   state: () => ({
//     enemy: null,
//     enemyVisual: null, // [THÊM MỚI] Để lưu hình ảnh/tên quái từ màn hình Encounter
//     enemyHp: 0,
//     enemyMaxHp: 100,
//     playerHp: 0,
//     playerMaxHp: 100,
//     combatLogs: [],
//     status: "IDLE",
//     goldEarned: 0,
//     expEarned: 0,
//     droppedItem: null,
//     isReady: false,
//   }),

//   actions: {
//     // [THÊM MỚI] Hàm này để Component gọi khi gặp quái
//     setEncounter(visualData) {
//       this.enemyVisual = visualData;
//     },

//     async startBattle() {
//       this.status = "LOADING";
//       this.isReady = false;
//       this.combatLogs = [];

//       try {
//         const res = await axiosClient.post("/battle/start");
//         this.handleResult(res.data);
//         this.isReady = true;
//       } catch (e) {
//         this.status = "ERROR";
//         this.combatLogs.push("Lỗi kết nối: " + (e.response?.data || e.message));
//         console.error(e);
//       }
//     },

//     async autoTurn(isBuffed) {
//       if (!this.isReady || !this.enemy || !this.enemy.enemyId) return null;

//       try {
//         const res = await axiosClient.post("/battle/attack", {
//           enemyId: this.enemy.enemyId,
//           enemyHp: this.enemyHp,
//           isBuffed: isBuffed,
//         });
//         this.handleResult(res.data);
//         return res.data;
//       } catch (e) {
//         console.error("Lỗi turn:", e);

//         if (
//           e.response &&
//           (e.response.status === 403 || e.response.status === 401)
//         ) {
//           this.status = "ERROR";
//           this.combatLogs.push(
//             "Phiên đăng nhập hết hạn hoặc lỗi quyền truy cập.",
//           );
//           this.isReady = false;
//         }
//         return null;
//       }
//     },

//     async sendAction(actionType) {
//       try {
//         const res = await axiosClient.post("/battle/action", {
//           action: actionType,
//         });
//         this.handleResult(res.data);
//       } catch (e) {
//         console.error(e);
//       }
//     },

//     handleResult(data) {
//       if (!data) return;

//       if (data.enemyId) {
//         this.enemy = {
//           enemyId: data.enemyId,
//           name: data.enemyName,
//           level: data.enemyLv || "??",
//         };
//       } else if (data.enemy) {
//         this.enemy = data.enemy;
//       }

//       this.enemyHp = data.enemyHp;
//       this.enemyMaxHp = data.enemyMaxHp || this.enemyMaxHp;
//       this.playerHp = data.playerHp;
//       this.playerMaxHp = data.playerMaxHp || this.playerMaxHp;
//       this.status = data.status;

//       if (data.combatLog && Array.isArray(data.combatLog)) {
//         this.combatLogs = [...this.combatLogs, ...data.combatLog].slice(-20);
//       }

//       if (data.status === "VICTORY") {
//         this.goldEarned = data.goldEarned;
//         this.expEarned = data.expEarned;
//         if (data.droppedItemName) {
//           this.droppedItem = {
//             name: data.droppedItemName,
//             image: data.droppedItemImage,
//             rarity: data.droppedItemRarity,
//           };
//         }
//         this.isReady = false;
//       } else if (data.status === "DEFEAT") {
//         this.isReady = false;
//       }

//       const charStore = useCharacterStore();
//       if (charStore.character) {
//         charStore.character.hp = data.playerHp;
//       }
//     },

//     resetBattle() {
//       this.status = "IDLE";
//       this.enemy = null;
//       this.enemyVisual = null; // [SỬA] Reset cả cái này
//       this.combatLogs = [];
//       this.isReady = false;
//       this.droppedItem = null;
//     },
//   },
// });
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
        // Hiển thị lỗi rõ ràng
        const msg =
          e.response?.data?.message ||
          "Không thể tải trận đấu (Có thể do lỗi server hoặc dữ liệu cũ).";
        this.combatLogs.push(msg);
        console.error(e);
      }
    },

    async autoTurn(isBuffed) {
      if (!this.isReady) return null;

      try {
        const res = await axiosClient.post("/battle/attack", {
          enemyId: this.enemy?.enemyId || 0,
          isBuffed: isBuffed,
        });
        this.handleResult(res.data);
        return res.data;
      } catch (e) {
        console.error("Turn Error:", e);

        // [FIX QUAN TRỌNG: CHẶN ĐỨNG VÒNG LẶP NẾU CÓ LỖI]
        this.status = "ERROR"; // Chuyển trạng thái sang Error để hiện nút Về Làng
        this.isReady = false; // Dừng vòng lặp auto

        const msg =
          e.response?.data?.message || e.message || "Mất kết nối với trận đấu.";
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

      if (data.enemyId || data.enemyName) {
        this.enemy = {
          enemyId: data.enemyId || (this.enemy ? this.enemy.enemyId : 0),
          name: data.enemyName || "Quái Lạ",
          level: data.enemyLv || "??",
        };
      } else if (data.enemy) {
        this.enemy = data.enemy;
      }

      this.enemyHp = data.enemyHp ?? this.enemyHp;
      this.enemyMaxHp = data.enemyMaxHp || this.enemyMaxHp;
      this.playerHp = data.playerHp ?? this.playerHp;
      this.playerMaxHp = data.playerMaxHp || this.playerMaxHp;
      this.status = data.status || this.status;

      if (data.combatLog) {
        if (Array.isArray(data.combatLog)) {
          this.combatLogs = [...this.combatLogs, ...data.combatLog].slice(-20);
        } else if (typeof data.combatLog === "string") {
          this.combatLogs.push(data.combatLog);
        }
      }

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
