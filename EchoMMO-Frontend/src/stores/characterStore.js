// import { defineStore } from "pinia";
// import axiosClient from "../api/axiosClient";
// import router from "../router";

// export const useCharacterStore = defineStore("character", {
//   state: () => ({
//     character: null,
//     isLoading: false,
//     logs: [],
//     explorationState: {
//       playerPos: 10,
//       moveDir: 1,
//     },
//   }),

//   getters: {
//     // [FIX] Tính % XP dựa trên level và currentExp
//     xpPercent: (state) => {
//       if (!state.character) return 0;
//       const lv = state.character.level || 1;
//       const curExp = state.character.currentExp || 0;
//       // Công thức XP cần khớp với Backend
//       const needed = lv < 60 ? lv * 50 : lv * 100 + Math.pow(lv - 60, 2) * 200;
//       if (needed === 0) return 0;
//       return Math.min((curExp / needed) * 100, 100);
//     },

//     // [FIX] Tính % HP: Dùng currentHp và maxHp
//     hpPercent: (state) => {
//       if (!state.character || !state.character.maxHp) return 0;
//       const cur = state.character.currentHp || 0;
//       const max = state.character.maxHp;
//       return Math.min((cur / max) * 100, 100);
//     },

//     // [FIX] Tính % Energy: Dùng currentEnergy và maxEnergy
//     energyPercent: (state) => {
//       if (!state.character || !state.character.maxEnergy) return 0;
//       const cur = state.character.currentEnergy || 0;
//       const max = state.character.maxEnergy;
//       return Math.min((cur / max) * 100, 100);
//     },
//   },

//   actions: {
//     async fetchCharacter() {
//       // Nếu đang loading thì thôi, tránh gọi chồng chéo
//       if (this.isLoading) return;

//       this.isLoading = true;
//       try {
//         const res = await axiosClient.get("/character/me");
//         if (res.data) {
//           this.character = res.data;
//         }
//       } catch (error) {
//         console.error("Lỗi tải nhân vật:", error);
//         if (error.response && [401, 403].includes(error.response.status)) {
//           // Chỉ redirect nếu thực sự mất token
//           // router.push("/login"); 
//         }
//       } finally {
//         this.isLoading = false;
//       }
//     },

//     async createCharacter(name) {
//       try {
//         const res = await axiosClient.post("/character/create", { name });
//         this.character = res.data;
//         return true;
//       } catch (error) {
//         alert(error.response?.data || "Lỗi tạo nhân vật");
//         return false;
//       }
//     },

//     async explore(payload = { mapId: 'MAP_01' }) {
//       if (!this.character) return;

//       // [FIX] Kiểm tra đúng biến currentEnergy
//       if (this.character.currentEnergy < 1) {
//         this.addLog("⚠️ Hết thể lực! Về trại nghỉ ngơi.", "WARNING");
//         return;
//       }

//       try {
//         const res = await axiosClient.post("/exploration/explore", payload);
//         const data = res.data;

//         if (data.type === "GATHERING") return data;

//         // [FIX] Cập nhật State trực tiếp ngay lập tức để UI phản hồi nhanh
//         if (this.character) {
//           if (data.currentEnergy !== undefined) this.character.currentEnergy = data.currentEnergy;
//           if (data.currentExp !== undefined) this.character.currentExp = data.currentExp;
//           if (data.currentHp !== undefined) this.character.currentHp = data.currentHp;

//           // Nếu lên cấp, cập nhật cả level và các chỉ số max (nếu có thay đổi)
//           if (data.newLevel) {
//             this.character.level = data.newLevel;
//             // Nên fetch lại để lấy chỉ số max mới chuẩn xác nhất
//             await this.fetchCharacter();
//             this.addLog(`🎉 LÊN CẤP ${data.newLevel}!`, "LEVEL_UP");
//           }
//         }

//         this.addLog(data.message, data.type === "ENEMY" ? "ENEMY" : "INFO");
//         return data;
//       } catch (error) {
//         const msg = error.response?.data?.message || error.response?.data || "Lỗi";
//         if (msg === "CAPTCHA") throw new Error("CAPTCHA");
//         this.addLog("❌ " + msg, "ERROR");
//         throw error;
//       }
//     },

//     addLog(message, type = "INFO") {
//       this.logs.unshift({ id: Date.now(), message, type });
//       if (this.logs.length > 50) this.logs.pop();
//     },
//   },
// });

import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";
import router from "../router";

export const useCharacterStore = defineStore("character", {
  state: () => ({
    character: null,
    isLoading: false,
    logs: [],
    explorationState: {
      playerPos: 10,
      moveDir: 1,
    },
    // [NEW] Lưu thông tin tài nguyên do Server trả về
    gatheringState: {
      itemId: null,
      amount: 0,
      name: "",
    },
  }),

  getters: {
    xpPercent: (state) => {
      if (!state.character) return 0;
      const lv = state.character.level || 1;
      const curExp = state.character.currentExp || 0;
      const needed = lv < 60 ? lv * 50 : lv * 100 + Math.pow(lv - 60, 2) * 200;
      if (needed === 0) return 0;
      return Math.min((curExp / needed) * 100, 100);
    },
    hpPercent: (state) => {
      if (!state.character || !state.character.maxHp) return 0;
      return Math.min(
        (state.character.currentHp / state.character.maxHp) * 100,
        100
      );
    },
    energyPercent: (state) => {
      if (!state.character || !state.character.maxEnergy) return 0;
      return Math.min(
        (state.character.currentEnergy / state.character.maxEnergy) * 100,
        100
      );
    },
  },

  actions: {
    async fetchCharacter() {
      if (this.isLoading) return;
      this.isLoading = true;
      try {
        const res = await axiosClient.get("/character/me");
        if (res.data) this.character = res.data;
      } catch (error) {
        console.error(error);
      } finally {
        this.isLoading = false;
      }
    },

    async explore(payload = { mapId: "MAP_01" }) {
      if (!this.character) return;
      // Hành tẩu không tốn năng lượng (theo yêu cầu của bro)
      
      try {
        // [NOTE] Backend đã được sửa để nhận JSON body, nên call này sẽ hoạt động OK
        const res = await axiosClient.post("/exploration/explore", payload);
        const data = res.data;

        // [LOGIC MỚI] Nếu là GATHERING -> Lưu state và Return
        if (data.type === "GATHERING") {
          this.gatheringState = {
            itemId: data.rewardItemId,
            amount: data.rewardAmount,
            name: data.rewardName,
          };
          return data; // Trả về để Component chuyển trang
        }

        // Cập nhật state
        if (this.character) {
          if (data.currentEnergy !== undefined)
            this.character.currentEnergy = data.currentEnergy;
          if (data.currentExp !== undefined)
            this.character.currentExp = data.currentExp;
          if (data.currentHp !== undefined)
            this.character.currentHp = data.currentHp;
          if (data.newLevel) {
            this.character.level = data.newLevel;
            await this.fetchCharacter();
            this.addLog(`🎉 LÊN CẤP ${data.newLevel}!`, "LEVEL_UP");
          }
        }
        this.addLog(data.message, data.type === "ENEMY" ? "ENEMY" : "INFO");
        return data;
      } catch (error) {
        const msg =
          error.response?.data?.message || error.response?.data || "Lỗi";
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