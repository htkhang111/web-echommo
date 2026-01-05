<template>
  <div class="page-container wuxia-privacy jade-theme">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
      <div class="pattern-overlay"></div>
      <div class="mist-layer"></div>
    </div>

    <div class="privacy-layout custom-scroll">
      <header class="page-header fade-in-down">
        <div class="seal-mark-bg">MẬT</div>
        <h1 class="main-title">BẢO MẬT TIÊN CƠ</h1>
        <div class="decor-line">
          <span class="jade-gem left"></span>
          <span class="gold-line"></span>
          <span class="jade-gem right"></span>
        </div>
        <p class="sub-title">"Thiên cơ bất khả lộ - Thông tin đạo hữu được bảo hộ tuyệt đối"</p>
      </header>

      <div class="privacy-scroll-container">
        <div class="scroll-ornament top">
          <div class="gem-center"><i class="fas fa-lock"></i></div>
        </div>

        <div class="scroll-body glass-panel">
          <div class="inner-border">
            
            <div 
              v-for="(policy, index) in policies" 
              :key="index"
              class="policy-item fade-in-up"
              :style="{ '--delay': `${index * 0.15}s` }"
            >
              <div class="policy-icon">
                <div class="hex-bg">
                  <i :class="policy.icon"></i>
                </div>
                <div class="line-down"></div>
              </div>

              <div class="policy-content">
                <h3 class="policy-title">{{ index + 1 }}. {{ policy.title }}</h3>
                <p class="policy-text" v-html="policy.content"></p>
              </div>
            </div>

            <div class="policy-footer">
              <p class="update-time">
                <i class="far fa-clock"></i> Cập nhật lần cuối: Giờ Tý, Ngày 01 Tháng 01 Năm 2026
              </p>
              <div class="seal-box">
                <div class="seal-inner">TUYỆT MẬT</div>
              </div>
            </div>

          </div>
        </div>

        <div class="scroll-ornament bottom"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";

const policies = [
  {
    title: "Thu Thập Linh Tức (Thông Tin)",
    icon: "fas fa-user-secret",
    content: "Bổn trang chỉ ghi lại <strong>Đạo Hiệu (Username)</strong> và <strong>Truyền Âm Phù (Email)</strong> nhằm mục đích xác định danh tính và phục vụ việc đăng nhập. Chúng tôi không thu thập thông tin về nơi ở thực tế (Địa chỉ) hay chân dung (Ảnh thật) của đạo hữu."
  },
  {
    title: "Hộ Pháp Trận Đồ (Bảo Vệ)",
    icon: "fas fa-shield-alt",
    content: "Khẩu quyết mật mã (Password) của đạo hữu được phong ấn bằng thuật toán <strong>Bcrypt</strong> tối thượng và lưu trữ sâu trong Tàng Kinh Các. Ngay cả Chưởng Môn cũng không thể nhìn thấy mật khẩu gốc của đạo hữu."
  },
  {
    title: "Tuyệt Mật Thiên Cơ (Chia Sẻ)",
    icon: "fas fa-handshake-slash",
    content: "Chúng tôi lập lời thề tâm ma: <strong>Tuyệt đối không tiết lộ</strong> thông tin của đạo hữu cho bất kỳ môn phái hay thế lực thứ ba nào, trừ khi có lệnh triệu tập từ Thiên Đình (Pháp luật)."
  },
  {
    title: "Lưu Giữ Dấu Vết (Cookies)",
    icon: "fas fa-cookie-bite",
    content: "Hệ thống sử dụng một lượng nhỏ linh lực (Cookies) để ghi nhớ trạng thái đăng nhập của đạo hữu, giúp việc hành tẩu giang hồ được thông suốt, không bị gián đoạn."
  }
];
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@400;600;700&family=Noto+Serif:wght@400;700&display=swap");

.wuxia-privacy {
  min-height: 100vh;
  background-color: #050505;
  color: #e0e0e0;
  font-family: "Noto Serif", serif;
  position: relative;
  overflow: hidden;
}

/* BACKGROUND */
.bg-layer { position: fixed; inset: 0; pointer-events: none; }
.mountain-bg { position: absolute; inset: 0; background-size: cover; background-position: center; opacity: 0.2; filter: grayscale(80%) sepia(20%); }
.pattern-overlay {
  position: absolute; inset: 0;
  background-image: url("https://www.transparenttextures.com/patterns/black-scales.png");
  opacity: 0.4;
}
.mist-layer {
  position: absolute; inset: 0;
  background: radial-gradient(circle at 50% 120%, rgba(20, 50, 40, 0.4), transparent 70%);
}

/* LAYOUT */
.privacy-layout {
  position: relative; z-index: 10;
  max-width: 900px; margin: 0 auto;
  height: 100vh; overflow-y: auto;
  padding: 40px 20px;
}
.custom-scroll::-webkit-scrollbar { width: 6px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #004d40; border-radius: 3px; }

/* HEADER */
.page-header { text-align: center; margin-bottom: 50px; position: relative; }
.seal-mark-bg {
  position: absolute; top: -30px; left: 50%; transform: translateX(-50%);
  font-size: 10rem; color: #b71c1c; 
  font-family: "Cormorant Garamond", serif;
  opacity: 0.08; font-weight: 900; pointer-events: none; z-index: -1;
}
.main-title {
  font-family: "Cormorant Garamond", serif; font-size: 3.5rem; font-weight: 700;
  color: #e0f2f1; text-transform: uppercase; margin: 0;
  text-shadow: 0 0 20px rgba(0, 230, 118, 0.3);
  letter-spacing: 3px;
}
.decor-line { display: flex; align-items: center; justify-content: center; gap: 15px; margin: 15px 0; }
.gold-line { width: 250px; height: 2px; background: linear-gradient(90deg, transparent, #ffd700, transparent); }
.jade-gem { width: 10px; height: 10px; background: #00bfa5; transform: rotate(45deg); box-shadow: 0 0 10px #00bfa5; }
.sub-title { color: #80cbc4; font-style: italic; letter-spacing: 1px; font-size: 1.1rem; }

/* SCROLL CONTAINER */
.privacy-scroll-container { margin-bottom: 50px; position: relative; }

/* Ornaments */
.scroll-ornament {
  height: 40px; background: #1a1a1a;
  border: 1px solid #004d40;
  position: relative;
  display: flex; justify-content: center; align-items: center;
  z-index: 2;
}
.scroll-ornament.top {
  border-radius: 8px 8px 0 0;
  border-bottom: none;
  background: linear-gradient(to bottom, #1a1a1a, #0a0a0a);
}
.scroll-ornament.bottom {
  border-radius: 0 0 8px 8px;
  border-top: none;
  background: linear-gradient(to top, #1a1a1a, #0a0a0a);
}
.gem-center {
  width: 50px; height: 50px; border-radius: 50%;
  background: #000; border: 2px solid #ffd700;
  display: flex; align-items: center; justify-content: center;
  color: #ffd700; font-size: 1.2rem;
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.3);
  margin-bottom: -25px; z-index: 3;
}

/* Scroll Body */
.scroll-body {
  background: rgba(10, 10, 10, 0.9);
  border-left: 2px solid #004d40;
  border-right: 2px solid #004d40;
  padding: 10px;
  backdrop-filter: blur(10px);
}
.inner-border {
  border: 1px solid rgba(255, 215, 0, 0.1);
  padding: 40px;
  background-image: url("https://www.transparenttextures.com/patterns/dark-matter.png");
}

/* POLICY ITEMS */
.policy-item { display: flex; gap: 25px; margin-bottom: 30px; animation: fadeInUp 0.6s ease backwards; animation-delay: var(--delay); }

.policy-icon {
  display: flex; flex-direction: column; align-items: center; width: 60px; flex-shrink: 0;
}
.hex-bg {
  width: 50px; height: 50px;
  background: #00251a;
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  display: flex; align-items: center; justify-content: center;
  color: #00bfa5; font-size: 1.5rem;
  transition: 0.3s;
  border: 1px solid #004d40;
}
.policy-item:hover .hex-bg { background: #004d40; color: #ffd700; transform: scale(1.1); border-color: #ffd700; }
.line-down { width: 1px; background: linear-gradient(to bottom, #004d40, transparent); flex: 1; margin-top: 10px; }
.policy-item:last-child .line-down { display: none; }

.policy-content { flex: 1; }
.policy-title {
  margin: 0 0 10px 0;
  font-family: "Cormorant Garamond", serif; font-size: 1.6rem; font-weight: 700;
  color: #ffd700; text-transform: uppercase;
  border-bottom: 1px dashed rgba(255, 215, 0, 0.2);
  padding-bottom: 5px; display: inline-block;
}
.policy-text { color: #cfd8dc; font-size: 1.1rem; line-height: 1.6; text-align: justify; }
.policy-text strong { color: #00bfa5; font-weight: bold; }

/* FOOTER */
.policy-footer {
  margin-top: 50px; text-align: center; border-top: 1px solid #333; padding-top: 30px;
  display: flex; flex-direction: column; align-items: center;
}
.update-time { font-style: italic; color: #888; font-size: 0.9rem; margin-bottom: 20px; }
.seal-box {
  width: 120px; height: 60px;
  border: 3px double #b71c1c;
  display: flex; align-items: center; justify-content: center;
  transform: rotate(-3deg);
  mask-image: url("https://www.transparenttextures.com/patterns/grunge-wall.png");
  opacity: 0.8; transition: 0.3s;
}
.seal-box:hover { opacity: 1; transform: scale(1.05) rotate(0); }
.seal-inner {
  color: #b71c1c; font-family: "Cormorant Garamond", serif;
  font-weight: 900; font-size: 1.5rem; letter-spacing: 2px;
}

/* ANIMATIONS */
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
.fade-in-down { animation: fadeInDown 0.8s ease; }
.fade-in-up { animation: fadeInUp 0.8s ease; }
@keyframes fadeInDown { from { opacity: 0; transform: translateY(-30px); } to { opacity: 1; transform: translateY(0); } }

/* RESPONSIVE */
@media (max-width: 600px) {
  .main-title { font-size: 2.2rem; }
  .policy-item { flex-direction: column; gap: 10px; }
  .policy-icon { flex-direction: row; align-items: center; gap: 10px; width: 100%; margin-bottom: 5px; }
  .line-down { display: none; }
  .hex-bg { width: 40px; height: 40px; font-size: 1.2rem; }
  .inner-border { padding: 20px; }
}
</style>