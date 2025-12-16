import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient";
import router from "../router";

export const useCharacterStore = defineStore("character", {
  state: () => ({
    character: null,
    isLoading: false,
    logs: [],
    explorationState: { playerPos: 10, moveDir: 1 },
  }),

  getters: {
    xpPercent: (state) => {
      if (!state.character) return 0;
      // Công thức hiển thị tương đối (Đồng bộ với backend Soft Cap)
      const lv = state.character.level;
      const needed = lv < 60 ? lv * 50 : lv * 100 + Math.pow(lv - 60, 2) * 200;
      return Math.min((state.character.currentExp / needed) * 100, 100);
    },
    hpPercent: (state) => {
      if (!state.character) return 0;
      return Math.min((state.character.hp / state.character.maxHp) * 100, 100);
    },
    energyPercent: (state) => {
      if (!state.character) return 0;
      return Math.min((state.character.energy / state.character.maxEnergy) * 100, 100);
    },
  },

  actions: {
    async fetchCharacter() {
      this.isLoading = true;
      try {
        const res = await axiosClient.get("/character/me");
        this.character = res.data || null;
      } catch (error) {
        if (error.response && [401, 403].includes(error.response.status)) router.push("/login");
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

    // [MODIFIED] Thêm tham số payload để gửi mapId
    async explore(payload = { mapId: 'MAP_01' }) {
      if (!this.character) return;
      if (this.character.energy < 1) {
        this.addLog("⚠️ Hết thể lực!", "WARNING");
        return;
      }

      try {
        const res = await axiosClient.post("/exploration/explore", payload);
        const data = res.data;

        this.character.energy = data.currentEnergy;
        this.character.currentExp = data.currentExp;
        
        if (data.newLevel) {
          await this.fetchCharacter(); 
          this.addLog(`🎉 LÊN CẤP ${data.newLevel}!`, "LEVEL_UP");
        }

        this.addLog(data.message, data.type === "ENEMY" ? "ENEMY" : "INFO");
        return data;
      } catch (error) {
        const msg = error.response?.data?.message || error.response?.data || "Lỗi";
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