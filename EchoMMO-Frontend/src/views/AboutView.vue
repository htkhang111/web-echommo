<template>
  <div class="page-container wuxia-about jade-theme">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
      <div class="pattern-overlay"></div>
      <div class="mist-layer"></div>
    </div>

    <div class="about-layout custom-scroll">
      <header class="page-header fade-in-down">
        <div class="seal-mark-top">THIÊN</div>
        <h1 class="main-title">THIÊN PHỦ KÝ SỰ</h1>
        <div class="decor-line">
          <span class="jade-gem left"></span>
          <span class="gold-line"></span>
          <span class="jade-gem right"></span>
        </div>
        <p class="sub-title">"Bí mật về sự hình thành của càn khôn vũ trụ"</p>
      </header>

      <div class="jade-book-container fade-in-up">
        <div class="book-border">
          <div class="book-inner">
            
            <section class="story-row">
              <div class="text-col">
                <h2 class="ink-title">KHỞI NGUYÊN</h2>
                <p class="drop-cap-text">
                  <span class="first-letter">T</span>hiên địa bất nhân, dĩ vạn vật vi sô cẩu. Thế giới <strong>ECHO MMO</strong> được hình thành từ hỗn mang, 
                  nơi linh khí hội tụ, vạn vật tranh đua tu luyện để đạt đến cảnh giới trường sinh.
                </p>
                <p>
                  Tại đây, đạo hữu sẽ bắt đầu từ một phàm nhân, trải qua muôn vàn kiếp nạn, khám phá các bí cảnh, 
                  thu thập thiên tài địa bảo, và sáng tạo ra con đường đạo của riêng mình.
                </p>
              </div>
              <div class="visual-col">
                <div class="visual-circle">
                  <i class="fas fa-dungeon floating-icon"></i>
                  <div class="circle-glow"></div>
                </div>
              </div>
            </section>

            <div class="story-divider">
              <span class="ink-splash"></span>
            </div>

            <section class="story-row reverse">
              <div class="text-col">
                <h2 class="ink-title">SỨ MỆNH</h2>
                <p class="drop-cap-text">
                  <span class="first-letter">Đ</span>ội ngũ <strong>AURIX Team</strong> mang trong mình hoài bão kiến tạo một sân chơi nhập vai thuần túy. 
                  Nơi đây, sức mạnh kim tiền không thể lấn át kỹ năng và tư duy chiến thuật.
                </p>
                <p>
                  Chúng tôi tin rằng, mỗi đạo hữu đều là nhân vật chính trong câu chuyện của mình. 
                  Không có kẻ mạnh nhất vĩnh cửu, chỉ có kẻ biết nắm bắt thiên cơ mới có thể xưng bá.
                </p>
              </div>
              <div class="visual-col">
                <div class="visual-circle">
                  <i class="fas fa-scroll floating-icon delay-1"></i>
                  <div class="circle-glow"></div>
                </div>
              </div>
            </section>

          </div>
        </div>
      </div>

      <section class="team-section">
        <div class="section-heading">
          <h2>TỨ TRỤ TRIỀU ĐÌNH</h2>
          <span class="heading-decor">AURIX FOUNDERS</span>
        </div>

        <div class="dev-grid">
          <div 
            class="dev-card" 
            v-for="(dev, index) in devTeam" 
            :key="index"
            :class="'card-' + index"
            @click="openProfile(dev)"
          >
            <div class="card-inner">
              <div class="card-front">
                <div class="avatar-frame">
                  <div class="avatar-bg">
                    <i :class="dev.icon"></i>
                  </div>
                  <div class="avatar-border"></div>
                </div>
                <h3 class="dev-name">{{ dev.name }}</h3>
                <div class="dev-alias">{{ dev.alias }}</div>
                <div class="dev-role">
                  <i class="fas fa-crown"></i> {{ dev.role }}
                </div>
                <div class="tap-hint"><i class="fas fa-hand-pointer"></i> Xem Danh Thiếp</div>
              </div>
              <div class="card-glow"></div>
            </div>
          </div>
        </div>
      </section>

      <footer class="about-footer fade-in">
        <div class="stamp-box">
          <div class="stamp-inner">
            <span>AURIX</span>
            <span>STUDIO</span>
          </div>
        </div>
      </footer>
    </div>

    <transition name="modal-fade">
      <div v-if="selectedDev" class="modal-backdrop" @click.self="closeProfile">
        <div class="jade-modal">
          <button class="btn-close" @click="closeProfile"><i class="fas fa-times"></i></button>
          
          <div class="modal-header">
            <div class="modal-avatar">
              <i :class="selectedDev.icon"></i>
            </div>
            <h3 class="modal-name">{{ selectedDev.name }}</h3>
            <div class="modal-alias">Đạo Hiệu: {{ selectedDev.alias }}</div>
          </div>

          <div class="modal-body">
            <p class="connect-text">Liên hệ với {{ selectedDev.alias }} qua:</p>
            <div class="social-actions">
              <a 
                v-if="selectedDev.socials.facebook" 
                :href="selectedDev.socials.facebook" 
                target="_blank" 
                class="social-btn fb"
              >
                <div class="btn-icon"><i class="fab fa-facebook-f"></i></div>
                <span>Facebook</span>
              </a>
              
              <a 
                v-if="selectedDev.socials.github" 
                :href="selectedDev.socials.github" 
                target="_blank" 
                class="social-btn git"
              >
                <div class="btn-icon"><i class="fab fa-github"></i></div>
                <span>GitHub</span>
              </a>
            </div>
          </div>

          <div class="modal-footer">
            <div class="seal-small">AURIX</div>
          </div>
        </div>
      </div>
    </transition>

  </div>
</template>

<script setup>
import { ref } from 'vue';

const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";
const selectedDev = ref(null);

const devTeam = [
  { 
    name: "Trần Quang Lương", 
    alias: "LuNu", 
    role: "Co-Founder & Developer", 
    icon: "fas fa-dragon",
    socials: {
      facebook: "https://www.facebook.com/share/17vZth77uS/?mibextid=wwXIfr",
      github: "https://github.com/LuongNuong131"
    }
  },
  { 
    name: "Lê Hoàng Lịnh", 
    alias: "LuxxMel", 
    role: "Co-Founder & Developer", 
    icon: "fas fa-fire",
    socials: {
      facebook: "https://www.facebook.com/share/18QKhoE7fj/?mibextid=wwXIfr",
      github: "https://github.com/luxxmel"
    }
  },
  { 
    name: "Huỳnh Trương Khang", 
    alias: "K4", 
    role: "Co-Founder & Developer", 
    icon: "fas fa-code",
    socials: {
      facebook: "https://www.facebook.com/share/1Bp3AhsJou/",
      github: "https://github.com/htkhang111"
    }
  },
  { 
    name: "Trịnh Hoàng Bảo Huy", 
    alias: "Wuy", 
    role: "Co-Founder & Developer", 
    icon: "fas fa-layer-group",
    socials: {
      facebook: "https://www.facebook.com/share/17v1oRXQK7/?mibextid=wwXIfr",
      github: "https://github.com/Baohuy732k4"
    }
  },
];

const openProfile = (dev) => {
  selectedDev.value = dev;
};

const closeProfile = () => {
  selectedDev.value = null;
};
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@400;600;700&family=Noto+Serif:wght@400;700&display=swap");

.wuxia-about {
  min-height: 100vh;
  background-color: #050505;
  color: #e0e0e0;
  font-family: "Noto Serif", serif; 
  position: relative;
  overflow: hidden;
}

/* BACKGROUND */
.bg-layer { position: fixed; inset: 0; pointer-events: none; }
.mountain-bg { position: absolute; inset: 0; background-size: cover; background-position: center; opacity: 0.2; filter: grayscale(80%) sepia(30%); }
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
.about-layout {
  position: relative; z-index: 10;
  max-width: 1100px; margin: 0 auto;
  height: 100vh; overflow-y: auto;
  padding: 40px 20px;
}
.custom-scroll::-webkit-scrollbar { width: 6px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #004d40; border-radius: 3px; }

/* HEADER */
.page-header { text-align: center; margin-bottom: 60px; position: relative; }
.seal-mark-top {
  position: absolute; top: -20px; left: 50%; transform: translateX(-50%);
  font-size: 8rem; color: #b71c1c; 
  font-family: "Cormorant Garamond", serif;
  opacity: 0.1; font-weight: 900; pointer-events: none; z-index: -1;
}
.main-title {
  font-family: "Cormorant Garamond", serif;
  font-size: 3.8rem; font-weight: 700;
  color: #e0f2f1; text-transform: uppercase; margin: 0;
  text-shadow: 0 0 20px rgba(0, 230, 118, 0.3);
  letter-spacing: 3px;
}
.decor-line { display: flex; align-items: center; justify-content: center; gap: 10px; margin: 15px 0; }
.gold-line { width: 200px; height: 2px; background: linear-gradient(90deg, transparent, #ffd700, transparent); }
.jade-gem { width: 12px; height: 12px; background: #00bfa5; transform: rotate(45deg); box-shadow: 0 0 10px #00bfa5; }
.sub-title { color: #80cbc4; font-style: italic; letter-spacing: 1px; font-size: 1.2rem; }

/* JADE BOOK */
.jade-book-container { margin-bottom: 80px; perspective: 1000px; }
.book-border {
  background: #0a0a0a;
  border: 2px solid #004d40;
  border-radius: 8px;
  padding: 5px;
  box-shadow: 0 20px 50px rgba(0,0,0,0.8);
  position: relative;
}
.book-border::before {
  content: ""; position: absolute; inset: 0;
  border: 1px solid #ffd700; opacity: 0.3;
  border-radius: 6px; pointer-events: none;
}

.book-inner {
  background: linear-gradient(135deg, #1a1a1a, #0d0d0d);
  padding: 50px;
  border-radius: 4px;
  background-image: url("https://www.transparenttextures.com/patterns/dark-matter.png");
}

/* STORY ROWS */
.story-row { display: flex; align-items: center; gap: 50px; margin-bottom: 20px; }
.story-row.reverse { flex-direction: row-reverse; }

.text-col { flex: 1.5; }
.visual-col { flex: 1; display: flex; justify-content: center; }

.ink-title {
  font-family: "Cormorant Garamond", serif;
  font-size: 2.5rem; font-weight: 700; color: #ffd700;
  margin: 0 0 20px 0; border-bottom: 2px solid #004d40;
  display: inline-block; padding-bottom: 5px;
}

.drop-cap-text { font-size: 1.15rem; line-height: 1.8; color: #cfd8dc; text-align: justify; margin-bottom: 15px; }
.first-letter {
  float: left; 
  font-family: "Cormorant Garamond", serif;
  font-size: 4.5rem; font-weight: 700;
  line-height: 0.8; color: #00bfa5; margin-right: 12px; margin-top: 5px;
  text-shadow: 2px 2px 0px #000;
}

.visual-circle {
  width: 180px; height: 180px; border-radius: 50%;
  border: 2px solid #004d40; position: relative;
  display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0.3);
}
.floating-icon { font-size: 4rem; color: #00bfa5; animation: float 4s ease-in-out infinite; }
.delay-1 { animation-delay: 2s; color: #ffd700; }
.circle-glow {
  position: absolute; inset: -10px; border-radius: 50%;
  background: radial-gradient(circle, rgba(0,191,165,0.2), transparent 70%);
  animation: pulse 3s infinite;
}

.story-divider { display: flex; justify-content: center; margin: 40px 0; }
.ink-splash { width: 100px; height: 2px; background: linear-gradient(90deg, transparent, #004d40, transparent); }

/* TEAM SECTION */
.team-section { text-align: center; margin-bottom: 80px; }
.section-heading h2 { 
  font-family: "Cormorant Garamond", serif; 
  color: #e0f2f1; font-size: 3rem; font-weight: 700; margin: 0; 
  text-shadow: 0 0 10px rgba(0, 191, 165, 0.5); 
}
.heading-decor { color: #80cbc4; letter-spacing: 3px; font-size: 1rem; display: block; margin-bottom: 40px; }

.dev-grid {
  display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 30px; justify-items: center;
}

.dev-card { width: 100%; max-width: 260px; height: 320px; perspective: 1000px; cursor: pointer; }
.card-inner {
  position: relative; width: 100%; height: 100%;
  background: rgba(20, 20, 20, 0.9);
  border: 1px solid #004d40; border-radius: 12px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  transition: all 0.4s ease; overflow: hidden;
  box-shadow: 0 5px 15px rgba(0,0,0,0.5);
}
.dev-card:hover .card-inner { transform: translateY(-10px); border-color: #ffd700; box-shadow: 0 10px 30px rgba(255, 215, 0, 0.2); cursor: pointer; }

.avatar-frame {
  width: 100px; height: 100px; margin-bottom: 20px;
  position: relative; display: flex; align-items: center; justify-content: center;
}
.avatar-bg {
  width: 100%; height: 100%; border-radius: 50%;
  background: #000; display: flex; align-items: center; justify-content: center;
  font-size: 2.5rem; color: #00bfa5; transition: 0.3s;
}
.avatar-border {
  position: absolute; inset: -5px; border-radius: 50%;
  border: 2px dashed #004d40; animation: spin 15s linear infinite;
}
.dev-card:hover .avatar-bg { color: #ffd700; background: #222; }
.dev-card:hover .avatar-border { border-color: #ffd700; animation-duration: 3s; }

.dev-name { 
  font-family: "Cormorant Garamond", serif; 
  color: #e0e0e0; margin: 10px 0 5px; font-size: 1.5rem; font-weight: 700;
}
.dev-alias { color: #00bfa5; font-weight: bold; margin-bottom: 5px; font-size: 1.1rem; }
.dev-card:hover .dev-alias { color: #ffd700; }
.dev-role { 
  color: #888; font-size: 0.9rem; font-style: italic; 
  border-top: 1px solid #333; padding-top: 10px; width: 90%; 
  display: flex; align-items: center; justify-content: center; gap: 5px;
}
.dev-role i { color: #ffd700; font-size: 0.8rem; }
.tap-hint {
  margin-top: 15px; font-size: 0.8rem; color: #00bfa5; opacity: 0; transition: 0.3s;
}
.dev-card:hover .tap-hint { opacity: 1; transform: translateY(-5px); }

/* FOOTER */
.about-footer { text-align: center; padding-bottom: 40px; }
.stamp-box {
  display: inline-block;
  border: 3px double #b71c1c; border-radius: 8px;
  padding: 10px 25px;
  transform: rotate(-3deg);
  opacity: 0.8;
  mask-image: url("https://www.transparenttextures.com/patterns/grunge-wall.png");
  transition: 0.3s;
}
.stamp-box:hover { transform: rotate(0) scale(1.1); opacity: 1; }
.stamp-inner {
  color: #b71c1c; font-family: "Cormorant Garamond", serif; font-weight: 900;
  font-size: 2rem; line-height: 1; text-align: center;
  display: flex; flex-direction: column;
}

/* MODAL STYLES */
.modal-backdrop {
  position: fixed; inset: 0; z-index: 1000;
  background: rgba(0,0,0,0.85); backdrop-filter: blur(5px);
  display: flex; align-items: center; justify-content: center;
}
.jade-modal {
  background: #111;
  border: 2px solid #00bfa5;
  border-radius: 12px;
  width: 90%; max-width: 400px;
  position: relative;
  padding: 30px;
  box-shadow: 0 0 30px rgba(0, 191, 165, 0.3);
  text-align: center;
  background-image: url("https://www.transparenttextures.com/patterns/dark-matter.png");
}
.btn-close {
  position: absolute; top: 10px; right: 10px;
  background: transparent; border: none; color: #555;
  font-size: 1.5rem; cursor: pointer; transition: 0.3s;
}
.btn-close:hover { color: #b71c1c; transform: rotate(90deg); }

.modal-header { margin-bottom: 25px; }
.modal-avatar {
  width: 80px; height: 80px; margin: 0 auto 15px;
  background: #000; border: 2px solid #ffd700; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 2.5rem; color: #ffd700;
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.3);
}
.modal-name {
  font-family: "Cormorant Garamond", serif; font-size: 2rem; font-weight: 700;
  color: #e0f2f1; margin: 0;
}
.modal-alias { color: #00bfa5; font-size: 1.1rem; font-weight: bold; margin-top: 5px; }

.modal-body { margin-bottom: 25px; }
.connect-text { color: #888; font-size: 0.9rem; margin-bottom: 15px; }
.social-actions { display: flex; flex-direction: column; gap: 10px; }
.social-btn {
  display: flex; align-items: center; padding: 10px 15px;
  border-radius: 6px; text-decoration: none; font-weight: bold;
  transition: 0.3s; color: #fff;
}
.social-btn .btn-icon { width: 30px; text-align: center; margin-right: 10px; font-size: 1.2rem; }
.social-btn.fb { background: #1877f2; border: 1px solid #1877f2; }
.social-btn.fb:hover { background: #155cb0; box-shadow: 0 0 10px rgba(24, 119, 242, 0.5); }
.social-btn.git { background: #333; border: 1px solid #555; }
.social-btn.git:hover { background: #000; border-color: #fff; box-shadow: 0 0 10px rgba(255,255,255,0.3); }

.modal-footer { margin-top: 20px; border-top: 1px dashed #333; padding-top: 15px; }
.seal-small {
  display: inline-block; border: 2px solid #b71c1c; color: #b71c1c;
  padding: 2px 10px; font-weight: bold; border-radius: 4px; opacity: 0.7;
}

/* ANIMATIONS */
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-10px); } }
@keyframes pulse { 0% { transform: scale(1); opacity: 0.5; } 50% { transform: scale(1.1); opacity: 0; } 100% { transform: scale(1); opacity: 0; } }
@keyframes spin { 100% { transform: rotate(360deg); } }
.fade-in-down { animation: fadeInDown 0.8s ease; }
.fade-in-up { animation: fadeInUp 0.8s ease; }
@keyframes fadeInDown { from { opacity: 0; transform: translateY(-30px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }

/* Modal Transition */
.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.3s ease; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }
.modal-fade-enter-active .jade-modal { animation: zoomIn 0.3s ease; }
.modal-fade-leave-active .jade-modal { animation: zoomOut 0.3s ease; }
@keyframes zoomIn { from { transform: scale(0.8); opacity: 0; } to { transform: scale(1); opacity: 1; } }
@keyframes zoomOut { from { transform: scale(1); opacity: 1; } to { transform: scale(0.8); opacity: 0; } }

/* RESPONSIVE */
@media (max-width: 768px) {
  .story-row, .story-row.reverse { flex-direction: column; gap: 20px; text-align: center; }
  .text-col { order: 1; }
  .visual-col { order: 2; margin-bottom: 20px; }
  .drop-cap-text { text-align: center; }
  .first-letter { float: none; font-size: 3rem; display: block; margin: 0 auto; }
  .jade-book-container { margin: 0 10px 60px; }
  .book-inner { padding: 30px 20px; }
}
</style>