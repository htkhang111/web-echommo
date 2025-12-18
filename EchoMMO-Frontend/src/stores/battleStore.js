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
    enemy: null,        // { enemyId, name, level, ... }
    enemyVisual: null,  // Dự phòng cho hình ảnh từ Encounter
    
    // Chỉ số chiến đấu
    enemyHp: 0,
    enemyMaxHp: 100,
    playerHp: 0,
    playerMaxHp: 100,
    
    combatLogs: [],
    status: "IDLE", // IDLE, LOADING, ONGOING, VICTORY, DEFEAT, ERROR
    
    // Phần thưởng
    goldEarned: 0,
    expEarned: 0,
    droppedItem: null,
    
    isReady: false, // Cờ hiệu để component biết đã load xong dữ liệu đầu trận
  }),

  actions: {
    setEncounter(visualData) {
      this.enemyVisual = visualData;
    },

    // 1. BẮT ĐẦU TRẬN ĐẤU
    async startBattle() {
      this.status = "LOADING";
      this.isReady = false;
      this.combatLogs = [];
      this.resetRewards();

      try {
        const res = await axiosClient.post("/battle/start");
        console.log("Battle Start:", res.data);
        this.handleResult(res.data);
        this.isReady = true;
      } catch (e) {
        this.status = "ERROR";
        this.combatLogs.push("Lỗi kết nối: " + (e.response?.data?.message || e.message));
        console.error(e);
      }
    },

    // 2. ĐÁNH TỰ ĐỘNG
    async autoTurn(isBuffed) {
      if (!this.isReady) return null;

      try {
        const res = await axiosClient.post("/battle/attack", {
          // Backend có thể không cần ID nếu dùng Session, nhưng gửi thêm cho chắc
          enemyId: this.enemy?.enemyId || 0, 
          isBuffed: isBuffed,
        });
        this.handleResult(res.data);
        return res.data;
      } catch (e) {
        console.error("Turn Error:", e);
        if (e.response && (e.response.status === 403 || e.response.status === 401)) {
          this.status = "ERROR";
          this.combatLogs.push("Phiên đăng nhập hết hạn.");
          this.isReady = false;
        }
        return null;
      }
    },

    // 3. GỬI HÀNH ĐỘNG (QTE)
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

    // --- HÀM XỬ LÝ DỮ LIỆU TỪ SERVER (QUAN TRỌNG) ---
    handleResult(data) {
      if (!data) return;

      // A. Cập nhật thông tin Quái (nếu có thay đổi)
      if (data.enemyId || data.enemyName) {
        this.enemy = {
          enemyId: data.enemyId || (this.enemy ? this.enemy.enemyId : 0),
          name: data.enemyName || "Quái Lạ",
          level: data.enemyLv || "??",
        };
      } 
      else if (data.enemy) {
        // Trường hợp Backend trả về nguyên cục object
        this.enemy = data.enemy;
      }
      // Nếu data không có enemy info -> Giữ nguyên enemy cũ (đừng set null)

      // B. Cập nhật HP
      this.enemyHp = data.enemyHp ?? this.enemyHp;
      this.enemyMaxHp = data.enemyMaxHp || this.enemyMaxHp;
      
      this.playerHp = data.playerHp ?? this.playerHp;
      this.playerMaxHp = data.playerMaxHp || this.playerMaxHp;
      
      this.status = data.status || this.status;

      // C. Xử lý Log
      if (data.combatLog) {
        if (Array.isArray(data.combatLog)) {
           this.combatLogs = [...this.combatLogs, ...data.combatLog].slice(-20);
        } else if (typeof data.combatLog === 'string') {
           this.combatLogs.push(data.combatLog);
        }
      }

      // D. Xử lý Kết quả (Thắng/Thua)
      if (data.status === "VICTORY") {
        this.goldEarned = data.goldEarned || 0; // Khớp với DTO Java
        this.expEarned = data.expEarned || 0;   // Khớp với DTO Java
        
        if (data.droppedItemName) {
          this.droppedItem = {
            name: data.droppedItemName,
            image: data.droppedItemImage,
            rarity: data.droppedItemRarity,
          };
        }
        this.isReady = false; // Dừng đánh
      } else if (data.status === "DEFEAT") {
        this.isReady = false; // Dừng đánh
      }

      // E. Đồng bộ máu về Character Store (để thanh máu trên Header đẹp)
      const charStore = useCharacterStore();
      if (charStore.character) {
        charStore.character.currentHp = this.playerHp; 
        // Lưu ý: check xem store dùng 'currentHp' hay 'hp'
      }
    },

    resetRewards() {
        this.goldEarned = 0;
        this.expEarned = 0;
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