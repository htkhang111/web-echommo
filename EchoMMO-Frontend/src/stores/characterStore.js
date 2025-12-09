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
  }),

  getters: {
    xpPercent: (state) => {
      if (!state.character) return 0;
      const needed = state.character.level * 100; // Hoặc theo công thức backend
      return Math.min((state.character.currentExp / needed) * 100, 100);
    },
    hpPercent: (state) => {
      if (!state.character) return 0;
      return Math.min((state.character.hp / state.character.maxHp) * 100, 100);
    },
    energyPercent: (state) => {
      if (!state.character) return 0;
      return Math.min(
        (state.character.energy / state.character.maxEnergy) * 100,
        100
      );
    },
  },

  actions: {
    async fetchCharacter() {
      this.isLoading = true;
      try {
        const res = await axiosClient.get("/character/me");
        this.character = res.data || null;
      } catch (error) {
        if (error.response && [401, 403].includes(error.response.status)) {
          router.push("/login");
        }
      } finally {
        this.isLoading = false;
      }
    },

    async createCharacter(name) {
      try {
        const res = await axiosClient.post("/character/create", { name });
        this.character = res.data;
        return true;
      } catch (error) {
        alert(error.response?.data || "Lỗi tạo nhân vật");
        return false;
      }
    },

    // Logic Thám Hiểm (Tích hợp GameFi)
    async explore() {
      if (!this.character) return null;
      if (this.character.energy < 1) {
        this.addLog("⚠️ Hết thể lực! Hãy về khách điếm nghỉ ngơi.", "WARNING");
        return null;
      }

      try {
        const res = await axiosClient.post("/exploration/explore");
        const data = res.data;

        // Cập nhật state từ response
        this.character.energy = data.currentEnergy;
        this.character.currentExp = data.currentExp;
        this.character.hp = this.character.hp; // Backend chưa trả về HP, giữ nguyên hoặc fetch lại

        if (data.newLevel) {
          this.fetchCharacter(); // Reload nếu lên cấp để lấy maxHp mới
          this.addLog(
            `🎉 CHÚC MỪNG! Đã đột phá lên Cảnh Giới ${data.newLevel}`,
            "LEVEL_UP"
          );
        }

        this.addLog(data.message, data.type === "ENEMY" ? "ENEMY" : "INFO");

        // Trả về data để Explore.vue có thể sử dụng
        return data;
      } catch (error) {
        const msg =
          error.response?.data?.message || error.response?.data || "Lỗi";
        if (msg === "CAPTCHA" || msg === "CAPTCHA_REQUIRED") {
          throw new Error("CAPTCHA"); // Để view xử lý hiện popup
        }
        this.addLog("❌ " + msg, "ERROR");
        throw error;
      }
    },

    addLog(message, type = "INFO") {
      this.logs.unshift({
        id: Date.now(),
        message,
        type,
      });
      if (this.logs.length > 50) this.logs.pop();
    },
  },
});

// =========================================================

// code test


// import { defineStore } from 'pinia';
// import axiosClient from '../api/axiosClient';

// export const useCharacterStore = defineStore('character', {
//   state: () => ({
//     character: null,
//     isLoading: false,
//     error: null,
//   }),

//   actions: {
//     async fetchCharacter() {
//       this.isLoading = true;
//       try {
//         // Gọi API lấy thông tin nhân vật
//         const response = await axiosClient.get('/game/character'); 
//         const data = response.data;

//         if (data && typeof data === 'object') {
//           // --- AUTO FIX DATA (CHỐNG LỖI UI) ---
//           const safeMaxHp = data.maxHp || 100;
//           const safeHp = (data.currentHp !== null && data.currentHp !== undefined) ? data.currentHp : safeMaxHp;
          
//           const safeMaxEnergy = data.maxEnergy || 50;
//           const safeEnergy = (data.currentEnergy !== null && data.currentEnergy !== undefined) ? data.currentEnergy : safeMaxEnergy;

//           this.character = {
//             ...data,
//             hp: safeHp,
//             maxHp: safeMaxHp,
//             energy: safeEnergy,
//             maxEnergy: safeMaxEnergy,
            
//             // Map các chỉ số khác an toàn
//             str: data.str || 0,
//             dex: data.dex || 0,
//             vit: data.vit || 0,
//             agi: data.agi || 0,
//             intelligence: data.intelligence || 0,
//             luck: data.luck || 0,
//           };
//         } else {
//             console.warn("[CharacterStore] API trả về dữ liệu rỗng");
//         }
        
//         this.error = null;
//       } catch (err) {
//         console.error("[CharacterStore] Lỗi tải dữ liệu:", err);
//         this.error = err.response?.data?.message || "Lỗi kết nối server";
//       } finally {
//         this.isLoading = false;
//       }
//     },

//     updateLocalStats(newHp, newEnergy) {
//       if (this.character) {
//         this.character.hp = newHp;
//         this.character.currentHp = newHp;
//         this.character.energy = newEnergy;
//         this.character.currentEnergy = newEnergy;
//       }
//     }
//   },
// });       if (data.newLevel) await this.fetchCharacter();
//         }
//         return data;
//       } catch (e) {
//         // Ném lỗi ra để View xử lý (VD: hiện Captcha)
//         throw e.response?.data || e;
//       }
//     },
//   },
// });
