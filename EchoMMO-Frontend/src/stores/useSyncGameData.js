// src/composables/useSyncGameData.js
import { useCharacterStore } from "@/stores/characterStore";
// Nếu bạn có store túi đồ thì import thêm ở đây, ví dụ:
// import { useInventoryStore } from "@/stores/inventoryStore";

export function useSyncGameData() {
  const charStore = useCharacterStore();
  // const inventoryStore = useInventoryStore();

  const syncAll = async () => {
    try {
      // 1. Cập nhật lại thông tin nhân vật (để update thanh Nội năng, EXP...)
      await charStore.fetchCharacter();

      // 2. Nếu sau này có Inventory, bạn bỏ comment dòng dưới để update túi đồ luôn
      // await inventoryStore.fetchInventory();

      console.log("🔄 Đã đồng bộ dữ liệu game thành công!");
    } catch (error) {
      console.error("❌ Lỗi đồng bộ dữ liệu:", error);
    }
  };

  return {
    syncAll,
  };
}
