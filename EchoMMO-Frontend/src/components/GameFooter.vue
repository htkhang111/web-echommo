<!-- <template>
  <footer class="game-footer">
    <div class="footer-border-top"></div>
    <div class="footer-container">
      <div class="footer-brand">
        <div class="brand-logo">
          <img :src="appLogo" alt="Logo" />
        </div>
        <div class="brand-info">
          <h2 class="team-name">Aurix Team</h2>
          <p class="slogan">Kiến tạo thế giới tiên hiệp</p>
        </div>
      </div>
      <div class="footer-nav">
        <div class="nav-col">
          <h3 class="col-title">Trò Chơi</h3>
          <ul>
            <li><router-link to="/about">Giới thiệu</router-link></li>
            <li><router-link to="/rules">Luật chơi</router-link></li>
            <li><router-link to="/updates">Nhật ký cập nhật</router-link></li>
          </ul>
        </div>
        <div class="nav-col">
          <h3 class="col-title">Cộng Đồng</h3>
          <ul>
            <li>
              <a href="https://discord.gg/mdbgEPxg" target="_blank"
                ><i class="fab fa-discord"></i> Discord</a
              >
            </li>
            <li>
              <a href="#" target="_blank"
                ><i class="fab fa-facebook"></i> Fanpage</a
              >
            </li>
          </ul>
        </div>
        <div class="nav-col">
          <h3 class="col-title">Hỗ Trợ</h3>
          <ul>
            <li><router-link to="/help">Trung tâm trợ giúp</router-link></li>
            <li><router-link to="/report">Báo lỗi</router-link></li>
            <li><router-link to="/privacy">Chính sách bảo mật</router-link></li>
          </ul>
        </div>
      </div>
    </div>
    <div class="copyright-row">
      <p>
        &copy; {{ new Date().getFullYear() }} AURIX Team. All rights reserved.
      </p>
    </div>
  </footer>
</template>

<script setup>
import { getAppLogo } from "@/utils/assetHelper"; // [FIX]
const appLogo = getAppLogo();
</script>

<style scoped>
/* ... (Giữ nguyên CSS cũ của bạn) ... */
@import url("https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Cinzel:wght@700&family=Roboto+Slab:wght@500;700&display=swap");
/* Copy CSS cũ vào đây */
:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --gold-accent: #fbc02d;
  --text-light: #d7ccc8;
}
.game-footer {
  position: relative;
  width: 100%;
  background: #3e2723;
  color: #d7ccc8;
  font-family: "Playfair Display", serif;
  border-top: 2px solid #5d4037;
  margin-top: auto;
  padding-top: 20px;
  box-shadow: 0 -4px 10px rgba(0, 0, 0, 0.5);
}
.footer-border-top {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #fbc02d, transparent);
}
.footer-container {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 0 40px 20px 40px;
  flex-wrap: wrap;
  gap: 30px;
}
.footer-brand {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 1;
  min-width: 250px;
}
.brand-logo img {
  width: 80px;
  height: auto;
  max-height: 80px;
  object-fit: contain;
  filter: drop-shadow(0 2px 5px rgba(0, 0, 0, 0.5));
}
.brand-info {
  display: flex;
  flex-direction: column;
}
.team-name {
  font-family: "Cinzel", serif;
  font-size: 1.5em;
  color: #fbc02d;
  margin: 0;
  text-shadow: 0 2px 2px rgba(0, 0, 0, 0.8);
}
.slogan {
  font-size: 0.9em;
  color: #a1887f;
  margin: 5px 0 0 0;
  font-style: italic;
}
.footer-nav {
  display: flex;
  gap: 50px;
  flex: 2;
  justify-content: flex-end;
}
.nav-col {
  display: flex;
  flex-direction: column;
}
.col-title {
  font-family: "Playfair Display", serif;
  font-weight: 700;
  font-size: 1.1em;
  color: #fbc02d;
  margin-bottom: 15px;
  border-bottom: 1px solid #5d4037;
  padding-bottom: 5px;
  display: inline-block;
  letter-spacing: 0.5px;
}
.nav-col ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.nav-col li {
  margin-bottom: 8px;
}
.nav-col a {
  text-decoration: none;
  color: #d7ccc8;
  font-size: 0.95em;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}
.nav-col a:hover {
  color: #fbc02d;
  transform: translateX(5px);
  text-shadow: 0 0 5px rgba(251, 192, 45, 0.4);
}
.copyright-row {
  background: rgba(0, 0, 0, 0.3);
  text-align: center;
  padding: 10px 0;
  font-size: 0.8em;
  color: #8d6e63;
  border-top: 1px solid #4e342e;
}
@media (max-width: 768px) {
  .footer-container {
    flex-direction: column;
    align-items: center;
    padding: 20px;
  }
  .footer-brand {
    text-align: center;
    flex-direction: column;
    margin-bottom: 20px;
  }
  .footer-nav {
    width: 100%;
    justify-content: space-between;
    gap: 20px;
  }
}
</style> -->


<template>
  <footer class="game-footer">
    <div class="footer-border-top"></div>
    <div class="footer-container">
      <div class="footer-brand">
        <div class="brand-logo">
          <img :src="appLogo" alt="Logo" />
        </div>
        <div class="brand-info">
          <h2 class="team-name">Aurix Team</h2>
          <p class="slogan">Kiến tạo thế giới tiên hiệp</p>
        </div>
      </div>
      <div class="footer-nav">
        <div class="nav-col">
          <h3 class="col-title">Trò Chơi</h3>
          <ul>
            <li><router-link to="/about">Giới thiệu</router-link></li>
            <li><router-link to="/rules">Luật chơi</router-link></li>
            <li><router-link to="/updates">Nhật ký cập nhật</router-link></li>
            <li>
              <a href="#" @click.prevent="openGiftModal" class="gift-link">
                <i class="fas fa-gift"></i> Giftcode
              </a>
            </li>
          </ul>
        </div>
        <div class="nav-col">
          <h3 class="col-title">Cộng Đồng</h3>
          <ul>
            <li>
              <a href="https://discord.gg/mdbgEPxg" target="_blank">
                <i class="fab fa-discord"></i> Discord
              </a>
            </li>
            <li>
              <a href="https://www.facebook.com/share/1APa65u86N/?mibextid=wwXIfr" target="_blank">
                <i class="fab fa-facebook"></i> Fanpage
              </a>
            </li>
          </ul>
        </div>
        <div class="nav-col">
          <h3 class="col-title">Hỗ Trợ</h3>
          <ul>
            <li><router-link to="/help">Trung tâm trợ giúp</router-link></li>
            <li><router-link to="/report">Báo lỗi</router-link></li>
            <li><router-link to="/privacy">Chính sách bảo mật</router-link></li>
          </ul>
        </div>
      </div>
    </div>
    <div class="copyright-row">
      <p>
        &copy; {{ new Date().getFullYear() }} AURIX Team. All rights reserved.
      </p>
    </div>

    <GiftCodeModal ref="giftModalRef" />
  </footer>
</template>

<script setup>
import { ref } from "vue";
import { getAppLogo } from "@/utils/assetHelper";
import GiftCodeModal from "./GiftCodeModal.vue"; // [NEW] Import Modal

const appLogo = getAppLogo();
const giftModalRef = ref(null);

// [NEW] Hàm mở Modal
const openGiftModal = () => {
  if (giftModalRef.value) {
    giftModalRef.value.open();
  }
};
</script>

<style scoped>
/* Import Font */
@import url("https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Cinzel:wght@700&family=Roboto+Slab:wght@500;700&display=swap");

:root {
  --wood-dark: #3e2723;
  --wood-light: #5d4037;
  --gold-accent: #fbc02d;
  --text-light: #d7ccc8;
}

.game-footer {
  position: relative;
  width: 100%;
  background: #3e2723;
  color: #d7ccc8;
  font-family: "Playfair Display", serif;
  border-top: 2px solid #5d4037;
  margin-top: auto;
  padding-top: 20px;
  box-shadow: 0 -4px 10px rgba(0, 0, 0, 0.5);
}

.footer-border-top {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #fbc02d, transparent);
}

.footer-container {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 0 40px 20px 40px;
  flex-wrap: wrap;
  gap: 30px;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 1;
  min-width: 250px;
}

.brand-logo img {
  width: 80px;
  height: auto;
  max-height: 80px;
  object-fit: contain;
  filter: drop-shadow(0 2px 5px rgba(0, 0, 0, 0.5));
}

.brand-info {
  display: flex;
  flex-direction: column;
}

.team-name {
  font-family: "Cinzel", serif;
  font-size: 1.5em;
  color: #fbc02d;
  margin: 0;
  text-shadow: 0 2px 2px rgba(0, 0, 0, 0.8);
}

.slogan {
  font-size: 0.9em;
  color: #a1887f;
  margin: 5px 0 0 0;
  font-style: italic;
}

.footer-nav {
  display: flex;
  gap: 50px;
  flex: 2;
  justify-content: flex-end;
}

.nav-col {
  display: flex;
  flex-direction: column;
}

.col-title {
  font-family: "Playfair Display", serif;
  font-weight: 700;
  font-size: 1.1em;
  color: #fbc02d;
  margin-bottom: 15px;
  border-bottom: 1px solid #5d4037;
  padding-bottom: 5px;
  display: inline-block;
  letter-spacing: 0.5px;
}

.nav-col ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-col li {
  margin-bottom: 8px;
}

.nav-col a {
  text-decoration: none;
  color: #d7ccc8;
  font-size: 0.95em;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-col a:hover {
  color: #fbc02d;
  transform: translateX(5px);
  text-shadow: 0 0 5px rgba(251, 192, 45, 0.4);
}

/* [NEW] Style nổi bật cho nút Giftcode */
.gift-link {
  color: #ff7043 !important; /* Màu cam đỏ nổi bật */
  font-weight: bold;
}

.gift-link:hover {
  color: #ffab91 !important;
  text-shadow: 0 0 8px rgba(255, 112, 67, 0.6) !important;
}

.copyright-row {
  background: rgba(0, 0, 0, 0.3);
  text-align: center;
  padding: 10px 0;
  font-size: 0.8em;
  color: #8d6e63;
  border-top: 1px solid #4e342e;
}

@media (max-width: 768px) {
  .footer-container {
    flex-direction: column;
    align-items: center;
    padding: 20px;
  }
  .footer-brand {
    text-align: center;
    flex-direction: column;
    margin-bottom: 20px;
  }
  .footer-nav {
    width: 100%;
    justify-content: space-between;
    gap: 20px;
  }
}
</style>