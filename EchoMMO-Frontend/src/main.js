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
  "%c SYSTEM ONLINE %c ECHO-MMO PROTOCOL V1.0 ACTIVATED ",
  "background: #00f3ff; color: #000; font-weight: bold; padding: 4px; border-radius: 3px 0 0 3px;",
  "background: #020617; color: #00f3ff; border: 1px solid #00f3ff; padding: 3px; border-radius: 0 3px 3px 0;",
);

app.mount("#app");
