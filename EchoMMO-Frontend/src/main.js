import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import "./style.css";
import "./assets/main.css";

// --- KHỞI TẠO APP ---
const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);

app.mount("#app");

// --- [CONSOLE ART] GIAO DIỆN LOG XỊN XÒ ---
const printSignature = () => {
  // 1. ASCII ART LOGO
  const logo = `
  ███████╗ ██████╗██╗  ██╗ ██████╗     ███╗   ███╗███╗   ███╗ ██████╗ 
  ██╔════╝██╔════╝██║  ██║██╔═══██╗    ████╗ ████║████╗ ████║██╔═══██╗
  █████╗  ██║     ███████║██║   ██║    ██╔████╔██║██╔████╔██║██║   ██║
  ██╔══╝  ██║     ██╔══██║██║   ██║    ██║╚██╔╝██║██║╚██╔╝██║██║   ██║
  ███████╗╚██████╗██║  ██║╚██████╔╝    ██║ ╚═╝ ██║██║ ╚═╝ ██║╚██████╔╝
  ╚══════╝ ╚═════╝╚═╝  ╚═╝ ╚═════╝     ╚═╝     ╚═╝╚═╝     ╚═╝ ╚═════╝ 
  `;

  // 2. STYLES ĐỊNH DẠNG
  const styles = {
    title: `
      font-family: 'Cinzel', serif;
      font-size: 50px;
      font-weight: 900;
      color: #fbc02d;
      text-shadow: 3px 3px 0 #3e2723, 5px 5px 0 #b71c1c;
      line-height: 1.2;
    `,
    badge: `
      background: linear-gradient(135deg, #3e2723 0%, #5d4037 100%);
      border: 1px solid #fbc02d;
      color: #fff;
      padding: 5px 10px;
      border-radius: 4px;
      font-family: 'Noto Serif TC', serif;
      font-weight: bold;
    `,
    text: `
      color: #795548;
      font-family: 'Noto Serif TC', serif;
      font-size: 12px;
      font-style: italic;
    `,
    warningHeader: `
      color: #d32f2f;
      font-size: 30px;
      font-weight: bold;
      background: #000;
      padding: 10px;
      border-radius: 5px;
      font-family: sans-serif;
    `,
    warningText: `
      font-size: 14px; 
      color: #fff; 
      background: #333; 
      padding: 5px; 
      border-radius: 4px;
    `,
  };

  // 3. IN RA CONSOLE
  console.log(`%c${logo}`, "color: #5d4037; font-weight: bold;");

  console.groupCollapsed(
    "%c📜 NHẬT KÝ HỆ THỐNG (Click để xem)",
    "color: #8d6e63; font-weight: bold;",
  );
  console.log(`%c ➤ Phiên bản: 1.0.0 (Alpha) `, styles.badge);
  console.log(`%c ➤ Engine: Vue 3 + Pinia + Vite `, styles.badge);
  console.log(
    `%c ➤ Trạng thái: Linh khí sung mãn, sẵn sàng hành tẩu! `,
    "color: #2e7d32; font-weight: bold; margin-top: 5px;",
  );
  console.log(
    `%c ➤ Dev: LuNu & Trung Tình `,
    "color: #1976d2; font-weight: bold;",
  );
  console.groupEnd();

  // 4. CẢNH BÁO SELF-XSS (Style Hacker)
  console.log(`%c⛔ CẤM ĐỊA GIANG HỒ! ⛔`, styles.warningHeader);
  console.log(
    `%cĐại hiệp dừng bước! Nếu có kẻ gian dụ dỗ đại hiệp dán code (Self-XSS) vào đây để nhận "Thần Binh" hay "KNB", đó là lừa đảo! Tài khoản của ngài sẽ bị đánh cắp trong nháy mắt.`,
    styles.warningText,
  );
};

// Gọi hàm in
printSignature();
