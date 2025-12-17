import { defineStore } from "pinia";
import { ref, computed } from "vue";

export const usePlayerStore = defineStore("player", () => {
  // --- STATE (Dữ liệu gốc) ---
  const name = ref("Chiến Binh");
  const level = ref(1);
  const exp = ref(0);

  // Tài nguyên
  const gold = ref(100);
  const diamond = ref(10); // Thêm Kim Cương

  // Chỉ số sinh tồn
  const hp = ref(100);
  const maxHp = ref(100);
  const energy = ref(50);
  const maxEnergy = ref(50);

  // --- GETTERS (Tính toán) ---

  // Logic Max EXP: Dễ trước 60, Khó sau 60 (Max Lv 70)
  const maxExp = computed(() => {
    if (level.value >= 70) return Infinity;

    const BASE_EXP = 100;
    if (level.value < 60) {
      // Giai đoạn DỄ (Lv 1-59): Mũ 2.2
      return Math.floor(BASE_EXP * Math.pow(level.value, 2.2));
    } else {
      // Giai đoạn KHÓ (Lv 60-69): Mũ 4.5 (Bức tường EXP)
      return Math.floor(BASE_EXP * Math.pow(level.value, 4.5));
    }
  });

  const expPercentage = computed(() => {
    if (level.value >= 70) return 100;
    if (maxExp.value === 0) return 0;
    return Math.min((exp.value / maxExp.value) * 100, 100).toFixed(1);
  });

  const hpPercentage = computed(() => (hp.value / maxHp.value) * 100);
  const energyPercentage = computed(
    () => (energy.value / maxEnergy.value) * 100,
  );

  // --- ACTIONS ---

  function gainExp(amount) {
    if (level.value >= 70) return;
    exp.value += amount;
    checkLevelUp();
  }

  function checkLevelUp() {
    if (level.value >= 70) return;

    if (exp.value >= maxExp.value) {
      const overflow = exp.value - maxExp.value;
      level.value++;

      // Tăng chỉ số khi lên cấp
      maxHp.value += 20;
      hp.value = maxHp.value;
      maxEnergy.value += 5;
      energy.value = maxEnergy.value;

      // Thưởng ít kim cương khi lên cấp
      diamond.value += 1;

      exp.value = overflow;
      if (exp.value >= maxExp.value) checkLevelUp();
      return true;
    }
    return false;
  }

  function takeDamage(amount) {
    hp.value = Math.max(0, hp.value - amount);
  }

  function heal(amount) {
    hp.value = Math.min(maxHp.value, hp.value + amount);
  }

  function useEnergy(amount) {
    if (energy.value >= amount) {
      energy.value -= amount;
      return true;
    }
    return false;
  }

  function addGold(amount) {
    gold.value += amount;
  }
  function addDiamond(amount) {
    diamond.value += amount;
  }

  return {
    name,
    level,
    exp,
    gold,
    diamond,
    hp,
    maxHp,
    energy,
    maxEnergy,
    maxExp,
    expPercentage,
    hpPercentage,
    energyPercentage,
    gainExp,
    checkLevelUp,
    takeDamage,
    heal,
    useEnergy,
    addGold,
    addDiamond,
  };
});
