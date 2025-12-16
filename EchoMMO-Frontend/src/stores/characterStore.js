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
      const lv = state.character.level || 1;
      const curExp = state.character.currentExp || 0;
      const needed = lv < 60 ? lv * 50 : lv * 100 + Math.pow(lv - 60, 2) * 200;
      return Math.min((curExp / needed) * 100, 100);
    },
    // [ĐỒNG BỘ] Dùng đúng currentHp
    hpPercent: (state) => {
      if (!state.character) return 0;
      const cur = state.character.currentHp || 0;
      const max = state.character.maxHp || 100;
      return Math.min((cur / max) * 100, 100);
    },
    // [ĐỒNG BỘ] Dùng đúng currentEnergy
    energyPercent: (state) => {
      if (!state.character) return 0;
      const cur = state.character.currentEnergy || 0;
      const max = state.character.maxEnergy || 50;
      return Math.min((cur / max) * 100, 100);
    },
  },

  actions: {
    async fetchCharacter() {
      this.isLoading = true;
      try {
        const res = await axiosClient.get("/character/me");
        // [ĐỒNG BỘ] Không map thủ công nữa, dùng thẳng data Backend
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

    async explore(payload = { mapId: 'MAP_01' }) {
      if (!this.character) return;
      // [ĐỒNG BỘ] Check currentEnergy
      if (this.character.currentEnergy < 1) {
        this.addLog("⚠️ Hết thể lực! Về trại nghỉ ngơi.", "WARNING");
        return;
      }

      try {
        const res = await axiosClient.post("/exploration/explore", payload);
        const data = res.data;

        // Nếu là Gathering -> Return để Component chuyển trang
        if (data.type === "GATHERING") return data;

        // [ĐỒNG BỘ] Cập nhật State với tên biến chuẩn
        this.character.currentEnergy = data.currentEnergy;
        this.character.currentExp = data.currentExp;
        // Backend explore response nên trả về currentHp nếu có combat (đã update ExplorationResponse ở các bước trước)
        if (data.currentHp !== undefined) this.character.currentHp = data.currentHp;

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