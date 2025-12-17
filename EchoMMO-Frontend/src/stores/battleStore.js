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
    goldEarned: 0,
    expEarned: 0,
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

      try {
        const res = await axiosClient.post("/battle/start");
        console.log("Start Battle Data:", res.data); // Debug xem API trả về gì
        this.handleResult(res.data);
        this.isReady = true;
      } catch (e) {
        this.status = "ERROR";
        this.combatLogs.push("Lỗi kết nối: " + (e.response?.data || e.message));
        console.error(e);
      }
    },

    async autoTurn(isBuffed) {
      // Nếu chưa có enemyId thì thử lấy từ enemy hiện tại (phòng trường hợp backend cần)
      const currentEnemyId = this.enemy?.enemyId || 0; 
      
      if (!this.isReady) return null;

      try {
        const res = await axiosClient.post("/battle/attack", {
          enemyId: currentEnemyId,
          enemyHp: this.enemyHp,
          isBuffed: isBuffed,
        });
        this.handleResult(res.data);
        return res.data;
      } catch (e) {
        console.error("Lỗi turn:", e);

        if (e.response && (e.response.status === 403 || e.response.status === 401)) {
          this.status = "ERROR";
          this.combatLogs.push("Phiên đăng nhập hết hạn.");
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

    // --- HÀM QUAN TRỌNG ĐÃ SỬA ---
    handleResult(data) {
      if (!data) return;

      // 1. Xử lý thông tin Quái (Logic lỏng hơn để dễ bắt dính)
      // Ưu tiên 1: Có ID quái hoặc Tên quái từ Backend gửi dạng phẳng
      if (data.enemyId || data.enemyName) {
        this.enemy = {
          enemyId: data.enemyId || (this.enemy ? this.enemy.enemyId : 0),
          name: data.enemyName || "Quái Lạ", // Tên mặc định nếu thiếu
          level: data.enemyLv || "??",
        };
      } 
      // Ưu tiên 2: Backend gửi nguyên cục object 'enemy'
      else if (data.enemy) {
        this.enemy = data.enemy;
      }
      // Ưu tiên 3: Nếu đang đánh nhau (ONGOING) mà API không trả info quái
      // thì GIỮ NGUYÊN quái cũ, không set null (để tránh mất hình)
      else if (this.status === 'ONGOING' && this.enemy) {
        // Giữ nguyên this.enemy
      }

      // 2. Cập nhật chỉ số máu (Dùng toán tử ?? để không bị lỗi số 0)
      this.enemyHp = data.enemyHp ?? this.enemyHp;
      this.enemyMaxHp = data.enemyMaxHp || this.enemyMaxHp;
      
      this.playerHp = data.playerHp ?? this.playerHp;
      this.playerMaxHp = data.playerMaxHp || this.playerMaxHp;
      
      this.status = data.status || this.status;

      // 3. Xử lý Log chiến đấu
      if (data.combatLog && Array.isArray(data.combatLog)) {
        // Nối log mới vào log cũ, giữ tối đa 20 dòng
        this.combatLogs = [...this.combatLogs, ...data.combatLog].slice(-20);
      } else if (typeof data.combatLog === 'string') {
        this.combatLogs.push(data.combatLog);
      }

      // 4. Xử lý Kết quả trận đấu
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
        this.isReady = false; // Dừng auto fight
      } else if (data.status === "DEFEAT") {
        this.isReady = false; // Dừng auto fight
      }

      // 5. Đồng bộ HP về Character Store (để thanh máu trên Header cập nhật)
      const charStore = useCharacterStore();
      if (charStore.character) {
        charStore.character.hp = this.playerHp;
      }
    },

    resetBattle() {
      this.status = "IDLE";
      this.enemy = null;
      this.enemyVisual = null;
      this.combatLogs = [];
      this.isReady = false;
      this.droppedItem = null;
    },
  },
});