import { defineStore } from "pinia";
import axiosClient from "../api/axiosClient"; // Giả định axiosClient đã được setup
import { reactive, computed } from "vue";

export const usePlayerStore = defineStore("player", () => {
  // --- 1. STATE (Reactive) ---
  const stats = reactive({
    // Dùng giá trị mặc định cho đến khi fetch
    id: null,
    name: "Đang tải...",
    lv: 1,
    exp: 0,
    nextLevelExp: 100,
    hp: 100,
    maxHp: 100,
    energy: 50,
    maxEnergy: 50,
    atk: 10,
    def: 5,
    spd: 10,
    gold: 0,
    // (Thêm các base crit, v.v. nếu cần)
  });

  const explorationState = reactive({
    playerPos: 10,
    moveDir: 1,
  });

  const inventory = reactive([]); // List UserItem objects

  // --- 2. GETTERS (Computed) ---
  const equipment = computed(() => {
    const slots = {
      WEAPON: null,
      HELM: null,
      ARMOR: null,
      BOOTS: null,
      RING: null,
      NECKLACE: null,
    };

    inventory.forEach((item) => {
      // FIX: Sử dụng item.item.type vì inventory là UserItem, chứa Item bên trong
      const itemType = item.item?.type;

      // Xử lý mapping type (HELMET -> HELM)
      const mappedType = itemType === "HELMET" ? "HELM" : itemType;

      if (item.isEquipped && slots.hasOwnProperty(mappedType)) {
        slots[mappedType] = item;
      }
    });
    return slots;
  });

  // --- 3. ACTIONS (Logic) ---

  // [V1 Logic] Helper xử lý Inventory data thô từ API
  const processInventory = (items) => {
    inventory.splice(0, inventory.length); // Xóa dữ liệu cũ
    items.forEach((i) => {
      // LƯU Ý: i là UserItem, i.item là Item
      inventory.push({
        id: i.userItemId,
        name: i.item.name,
        icon: i.item.imageUrl || "https://via.placeholder.com/64",
        type: i.item.type,
        isEquipped: i.isEquipped,
        quantity: i.quantity,
        enhanceLevel: i.enhanceLevel,
        stats: i.item, // Chứa atkBonus, defBonus...
      });
    });
  };

  const fetchPlayerData = async () => {
    try {
      const [resChar, resInv, resUser] = await Promise.all([
        axiosClient.get("/character/me"),
        axiosClient.get("/equipment/inventory"),
        axiosClient.get("/user/me"),
      ]);

      const char = resChar.data;
      const user = resUser.data;
      const wallet = user.wallet;

      // Map chỉ số mới (Sửa lỗi naming convention: lv, exp, baseAtk)
      Object.assign(stats, {
        id: user.userId,
        name: char.name,
        lv: char.lv, // Sửa Level -> lv
        exp: char.exp, // Sửa currentExp -> exp
        nextLevelExp: char.nextLevelExp || 100 * Math.pow(char.lv, 2),
        hp: char.hp,
        maxHp: char.maxHp,
        energy: char.energy,
        maxEnergy: char.maxEnergy,
        atk: char.baseAtk, // Lấy BaseAtk
        def: char.baseDef, // Lấy BaseDef
        gold: wallet.gold,
        // Thêm các chỉ số khác (spd, crit)
        spd: char.baseSpeed,
        critRate: char.baseCritRate,
      });

      // Xử lý Inventory & Equipment
      processInventory(resInv.data);
    } catch (e) {
      console.error("Lỗi tải data player:", e);
      stats.name = "Lỗi kết nối";
    }
  };

  // [V2 Logic] Hành động khám phá (đã được sửa logic ở backend)
  const explore = async () => {
    try {
      // Gọi API, backend tự lấy userId từ session
      const res = await axiosClient.post("/exploration/explore");
      const data = res.data;

      // Cập nhật stats từ data trả về của ExplorationResponse
      stats.energy = data.currentEnergy;
      stats.gold = data.currentGold || stats.gold; // Fix gold update
      stats.exp = data.currentExp;
      stats.lv = data.currentLv;
      stats.maxEnergy = data.maxEnergy; // Dùng cho trường hợp hồi phục

      // Nếu lên cấp, nên reload toàn bộ data để đồng bộ
      if (data.newLevel) await fetchPlayerData();

      return {
        type: data.type,
        message: data.message,
        expGain: data.expGain, // Cần trường này để popup
      };
    } catch (e) {
      const msg = e.response?.data?.message || "Lỗi mạng";
      if (msg.includes("CAPTCHA")) throw new Error("CAPTCHA");
      return { type: "ERROR", message: msg };
    }
  };

  const equipItemApi = async (item) => {
    try {
      await axiosClient.post(`/equipment/equip/${item.id}`);
      await fetchPlayerData();
    } catch (e) {
      alert(e.response?.data);
    }
  };

  const unequipItemApi = async (item) => {
    try {
      await axiosClient.post(`/equipment/unequip/${item.id}`);
      await fetchPlayerData();
    } catch (e) {
      alert(e.response?.data);
    }
  };

  // Export các state và actions cần thiết
  return {
    stats,
    explorationState,
    inventory,
    equipment, // Getter

    fetchPlayerData,
    explore,
    equipItemApi,
    unequipItemApi,
    // attackEnemy, restAtInn, (cần nằm trong BattleStore/GameService)
  };
});
