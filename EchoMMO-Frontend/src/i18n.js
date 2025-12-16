import { createI18n } from "vue-i18n";

// Lấy ngôn ngữ đã lưu, mặc định là 'vi'
const savedLocale = localStorage.getItem("user_lang") || "vi";

const messages = {
  vi: {
    menu: {
      home: "Phủ Đệ", // Sảnh Chính -> Phủ Đệ (Nhà riêng)
      game: "Giang Hồ", // Thế Giới
      inventory: "Hành Trang", // Kho đồ
      market: "Thương Hội", // Chợ
      friends: "Bằng Hữu", // Bạn bè
      rank: "Bảng Vàng", // Xếp hạng
      admin: "Quan Phủ", // Admin
      logout: "Quy Ẩn", // Đăng xuất
      profile: "Tại Hạ", // Profile -> Tại Hạ (Cách xưng hô khiêm tốn)
    },
    stats: {
      atk: "Công Lực",
      def: "Hộ Thể",
      hp: "Sinh Lực",
      spd: "Thân Pháp",
      crit: "Bạo Kích",
      energy: "Nội Lực", // Thể lực -> Nội Lực
      level: "Cảnh Giới", // Level -> Cảnh Giới
    },
    actions: {
      explore: "Hành Tẩu", // Du ngoạn -> Hành tẩu
      attack: "Xuất Chiêu",
      buy: "Thu Mua",
      sell: "Bán Ra",
      equip: "Trang Bị",
      unequip: "Tháo Gỡ",
      enhance: "Luyện Hóa",
      sell_system: "Bán Tiệm Cầm Đồ",
      sell_p2p: "Treo Sàn Đấu Giá",
    },
    market: {
      tab_sys: "Tạp Hóa Điếm", // Cửa hàng hệ thống
      tab_p2p: "Chợ Đen", // Chợ người chơi
      tab_sell: "Bày Bán",
      stock: "Tồn kho",
      price: "Ngân lượng",
      seller: "Thương nhân",
    },
    common: {
      confirm: "Chấp Thuận",
      cancel: "Bỏ Qua",
      success: "Thành Công",
      error: "Thất Bại",
    },
    // --- PHẦN PROFILE (VĂN PHONG KIẾM HIỆP) ---
    profile_page: {
      title: "HỒ SƠ HIỆP KHÁCH", // Agent Dossier -> Hồ Sơ Hiệp Khách
      identity: "LAI LỊCH", // Identity Record -> Lai Lịch
      modify_avatar: "DỊ DUNG THUẬT", // Modify Avatar -> Dị Dung Thuật (Thuật đổi mặt)
      codename: "DANH HIỆU", // Display Name -> Danh Hiệu
      reg_date: "XUẤT ĐẠO", // Reg Date -> Xuất Đạo (Ngày ra giang hồ)
      status: "TÌNH TRẠNG",
      operational: "ĐANG HÀNH TẨU", // Operational -> Đang Hành Tẩu
      rename_title: "CẢI DANH ĐỔI TÍNH", // Rename -> Cải Danh Đổi Tính
      display_name: "TÊN HIỆU MỚI",
      placeholder_name: "Nhập danh hiệu muốn đổi...",
      execute: "THI TRIỂN", // Execute -> Thi Triển
      warning_unique: "Danh hiệu là độc nhất vô nhị trên giang hồ.",
      security_title: "TÂM PHÁP BẢO MẬT", // Security -> Tâm Pháp Bảo Mật
      current_pass: "KHẨU QUYẾT CŨ", // Password -> Khẩu Quyết
      new_pass: "KHẨU QUYẾT MỚI",
      confirm_pass: "NHẮC LẠI KHẨU QUYẾT",
      update_cred: "TU LUYỆN KHẨU QUYẾT", // Change Pass -> Tu Luyện
      updating: "ĐANG VẬN KHÍ...", // Loading -> Đang Vận Khí
      msg_rename_success: "Đã thay tên đổi họ thành công.",
      msg_pass_success: "Khẩu quyết tâm pháp đã được ghi nhớ.",
      msg_pass_mismatch: "Tâm ma quấy nhiễu, khẩu quyết không trùng khớp.",
    },
  },
  en: {
    menu: {
      home: "Manor",
      game: "Jianghu",
      inventory: "Bag",
      market: "Chamber",
      friends: "Allies",
      rank: "Rankings",
      admin: "Magistrate",
      logout: "Retire",
      profile: "Myself",
    },
    stats: {
      atk: "Attack",
      def: "Defense",
      hp: "Health",
      spd: "Agility",
      crit: "Crit",
      energy: "Qi", // Energy -> Qi (Khí)
      level: "Realm", // Level -> Realm (Cảnh giới)
    },
    actions: {
      explore: "Wander", // Hành tẩu
      attack: "Strike",
      buy: "Acquire",
      sell: "Trade",
      equip: "Wield",
      unequip: "Remove",
      enhance: "Refine",
      sell_system: "Pawn",
      sell_p2p: "Auction",
    },
    market: {
      tab_sys: "General Store",
      tab_p2p: "Black Market",
      tab_sell: "Peddle",
      stock: "Stock",
      price: "Tael", // Lượng bạc
      seller: "Merchant",
    },
    common: {
      confirm: "Accept",
      cancel: "Ignore",
      success: "Success",
      error: "Failure",
    },
    // --- PROFILE SECTION (WUXIA ENGLISH) ---
    profile_page: {
      title: "HERO'S DOSSIER",
      identity: "ORIGIN RECORD",
      modify_avatar: "DISGUISE ART", // Dị dung
      codename: "TITLE",
      reg_date: "DEBUT DATE",
      status: "CONDITION",
      operational: "ROAMING",
      rename_title: "RENAME RITUAL",
      display_name: "NEW ALIAS",
      placeholder_name: "Enter new alias...",
      execute: "CAST",
      warning_unique: "This title is unique in the Jianghu.",
      security_title: "SECRET MANTRA",
      current_pass: "OLD MANTRA",
      new_pass: "NEW MANTRA",
      confirm_pass: "VERIFY MANTRA",
      update_cred: "CULTIVATE MANTRA",
      updating: "CHANNELING QI...",
      msg_rename_success: "Alias changed successfully.",
      msg_pass_success: "Secret mantra updated.",
      msg_pass_mismatch: "Mantras do not match.",
    },
  },
};

const i18n = createI18n({
  legacy: false,
  locale: savedLocale,
  fallbackLocale: "vi",
  messages,
});

export default i18n;
