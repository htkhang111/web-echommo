// import { defineStore } from 'pinia';
// import axiosClient from '../api/axiosClient';

// export const useChatStore = defineStore('chat', {
//   state: () => ({
//     messages: [], 
//     isConnected: false,
//     isLoading: false,
    
//     // State quản lý Chat Widget (Chat riêng)
//     isWidgetOpen: false,
//     privateChatTarget: null 
//   }),
  
//   actions: {
//     // --- CHAT THẾ GIỚI ---
//     async fetchMessages() {
//       this.isLoading = true;
//       try {
//         const res = await axiosClient.get('/chat/recent');
//         if (res.data) {
//           this.messages = res.data;
//         }
//       } catch (error) {
//         console.warn("Lỗi tải chat:", error);
//       } finally {
//         this.isLoading = false;
//       }
//     },

//     addMessage(message) {
//       const exists = this.messages.some(m => 
//         m.timestamp === message.timestamp && 
//         m.senderName === message.senderName && 
//         m.content === message.content
//       );
//       if (!exists) {
//         this.messages.push(message);
//         if (this.messages.length > 50) this.messages.shift();
//       }
//     },

//     setConnected(status) {
//       this.isConnected = status;
//     },

//     // --- CHAT RIÊNG (WIDGET) ---
    
//     // [FIX] Hàm này đã được sửa logic
//     openChatWith(user) {
//       // B1: Gán thông tin người cần chat trước
//       if (user) {
//         this.privateChatTarget = {
//           id: user.id || user.userId,
//           username: user.username || user.senderName,
//           avatarUrl: user.avatarUrl
//         };
//       }
      
//       // B2: Mới mở Widget lên -> Lúc này Watcher bên ChatWidget mới chạy và thấy có privateChatTarget
//       this.isWidgetOpen = true;
//     },

//     openChat() {
//       this.isWidgetOpen = true;
//     },

//     closeChat() {
//       this.isWidgetOpen = false;
//       this.privateChatTarget = null;
//     }
//   }
// });

import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";

export const useCharacterStore = defineStore("character", {
  state: () => ({
    character: null,
    isLoading: false,
    logs: [],
    // State ảo cho việc di chuyển (giữ nguyên logic cũ của đạo hữu)
    explorationState: {
      playerPos: 10,
      moveDir: 1,
    },
  }),

  getters: {
    // 1. Level an toàn
    level: (state) => state.character?.level || 1,

    // 2. Tính % EXP (Logic đồng bộ với Backend)
    xpPercent: (state) => {
      if (!state.character) return 0;
      const lv = state.character.level || 1;
      const curExp = state.character.currentExp || 0;
      
      // Công thức EXP (Phải khớp với Service Java)
      // Lv < 60: lv * 50
      // Lv >= 60: lv * 100 + (lv-60)^2 * 200
      const needed = lv < 60 ? lv * 50 : lv * 100 + Math.pow(lv - 60, 2) * 200;
      
      if (needed === 0) return 0;
      return Math.min((curExp / needed) * 100, 100);
    },

    // 3. Tính % HP
    hpPercent: (state) => {
      if (!state.character || !state.character.maxHp) return 0;
      return Math.min((state.character.currentHp / state.character.maxHp) * 100, 100);
    },

    // 4. Tính % Energy
    energyPercent: (state) => {
      if (!state.character || !state.character.maxEnergy) return 0;
      return Math.min((state.character.currentEnergy / state.character.maxEnergy) * 100, 100);
    },

    // 5. Helpers lấy tài nguyên (tránh lỗi undefined khi truy cập sâu)
    gold: (state) => state.character?.user?.wallet?.gold || 0,
    diamond: (state) => state.character?.user?.wallet?.diamonds || 0,
  },

  actions: {
    // Hàm đồng bộ dữ liệu chính
    async syncGameData() {
      await this.fetchCharacter();
    },

    async fetchCharacter() {
      // Cho phép gọi song song nhưng xử lý lỗi nhẹ nhàng
      try {
        const res = await axiosClient.get("/character/me");
        if (res.data) {
          // [QUAN TRỌNG] Merge dữ liệu thay vì gán đè toàn bộ để tránh flicker UI
          // Nếu character chưa có thì gán mới, có rồi thì update field
          this.character = this.character ? { ...this.character, ...res.data } : res.data;
        }
      } catch (error) {
        console.warn("Sync char data failed:", error.message);
      }
    },

    async explore(payload = { mapId: "MAP_01" }) {
      if (!this.character) return;

      try {
        const res = await axiosClient.post("/exploration/explore", payload);
        const data = res.data;

        // [OPTIMISTIC UPDATE] Cập nhật ngay State hiển thị để UI mượt mà
        if (this.character) {
            // Cập nhật các chỉ số sinh tồn
            if (data.currentHp !== undefined) this.character.currentHp = data.currentHp;
            if (data.currentEnergy !== undefined) this.character.currentEnergy = data.currentEnergy;
            if (data.currentExp !== undefined) this.character.currentExp = data.currentExp;
            
            // Xử lý Level Up ngay tại client
            if (data.newLevel) {
                this.character.level = data.newLevel;
                // Gọi fetch lại để đồng bộ chỉ số Max HP/Max Energy mới từ Server
                this.fetchCharacter();
                this.addLog(`🎉 CHÚC MỪNG! ĐẠO HỮU ĐÃ ĐỘT PHÁ CẢNH GIỚI ${data.newLevel}!`, "LEVEL_UP");
            }

            // Xử lý Gathering (Thu thập)
            if (data.type === 'GATHERING') {
                this.character.gatheringItemId = data.rewardItemId;
                this.character.gatheringRemainingAmount = data.rewardAmount;
                // Có thể update ví tiền ảo ở đây nếu response trả về số lượng item mới
            }
        }

        // Ghi log (Trừ loại Gathering để đỡ spam)
        if (data.type !== 'GATHERING') {
            this.addLog(data.message, data.type === "ENEMY" ? "ENEMY" : "INFO");
        }
        
        return data; 
      } catch (error) {
        const msg = error.response?.data?.message || "Lỗi kết nối vệ tinh";
        if (msg === "CAPTCHA") throw new Error("CAPTCHA");
        this.addLog("❌ " + msg, "ERROR");
        throw error;
      }
    },

    addLog(message, type = "INFO") {
      this.logs.unshift({ id: Date.now(), message, type });
      if (this.logs.length > 50) this.logs.pop();
    },
  },
});