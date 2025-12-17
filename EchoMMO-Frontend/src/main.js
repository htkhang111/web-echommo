import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import i18n from "./i18n"; // Import i18n đa ngôn ngữ
import "./assets/main.css"; // Import giao diện Cyberpunk Global

const app = createApp(App);

app.use(createPinia());
app.use(router);
app.use(i18n);

// --- CYBERPUNK BOOT LOG ---
// Cái này để khi F12 lên nhìn cho chất chơi
console.log(
  "%c ECHO-MMO %c GIANG HỒ DẬY SÓNG ",
  "background: #3e2723; color: #fbc02d; font-size: 24px; font-weight: 900; padding: 10px 15px; border-left: 5px solid #fbc02d; border-radius: 4px 0 0 4px; font-family: 'Times New Roman', serif;",
  "background: #b71c1c; color: #fff; font-size: 24px; font-weight: 700; padding: 10px 15px; border-radius: 0 4px 4px 0; font-family: 'Times New Roman', serif; text-shadow: 2px 2px 0px #000;"
);

console.log(
  "%c ➤ Phiên bản: 1.0.0 %c ➤ Trạng thái: Sẵn sàng hành tẩu ",
  "color: #795548; font-weight: bold; margin-top: 5px;",
  "color: #2e7d32; font-weight: bold;"
);
app.mount("#app");
