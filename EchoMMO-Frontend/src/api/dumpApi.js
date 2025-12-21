import axiosClient from "./axiosClient";

export default {
  /**
   * Thả cá xuống hồ
   * @param {string} type - "NORMAL" hoặc "SHARK"
   * @param {number} amount - Số lượng
   */
  dumpFish(type, amount) {
    return axiosClient.post("/dump/fish", { type, amount });
  },
};
