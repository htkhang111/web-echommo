<template>
  <div class="page-container wuxia-rules jade-theme">
    <div class="bg-layer">
      <div class="mountain-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
      <div class="pattern-overlay"></div>
      <div class="mist-layer"></div>
    </div>

    <div class="rules-layout custom-scroll">
      <header class="page-header fade-in-down">
        <div class="seal-mark-bg">QUY</div>
        <h1 class="main-title">THIÊN QUY GIỚI LUẬT</h1>
        <div class="decor-line">
          <span class="diamond-dot left"></span>
          <span class="gold-line"></span>
          <span class="diamond-dot right"></span>
        </div>
        <p class="sub-title">"Quốc có quốc pháp, gia có gia quy. Phạm luật tất trảm!"</p>
      </header>

      <div class="rules-container">
        <div 
          v-for="(rule, index) in rules" 
          :key="index" 
          class="rule-card fade-in-up"
          :class="rule.type"
          :style="{ '--delay': `${index * 0.15}s` }"
        >
          <div class="rule-number-col">
            <div class="hex-border">
              <span class="num">{{ index + 1 }}</span>
            </div>
            <div class="vertical-line"></div>
          </div>

          <div class="rule-content">
            <div class="rule-header">
              <div class="icon-box">
                <i :class="rule.icon"></i>
              </div>
              <h3 class="rule-title">{{ rule.title }}</h3>
            </div>
            <p class="rule-desc">{{ rule.desc }}</p>
            
            <i :class="rule.icon + ' watermark'"></i>
          </div>

          <div class="card-glow"></div>
        </div>
      </div>

      <footer class="rules-footer fade-in">
        <div class="decree-box">
          <p>Phụng Thiên Thừa Vận</p>
          <div class="seal-red">THIÊN PHỦ<br>CẤP BÁO</div>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup>
const bgImage = "https://htkhang111.github.io/background/b_doanhtrai.png";

const rules = [
  {
    title: "CẤM DÙNG TÀ THUẬT",
    desc: "Mọi hành vi sử dụng phần mềm thứ 3 (Hack/Cheat/Auto Click can thiệp sâu) làm mất cân bằng trật tự giang hồ sẽ bị Phế Bỏ Tu Vi Vĩnh Viễn (Ban IP/Account). Không có ngoại lệ.",
    icon: "fas fa-skull-crossbones",
    type: "danger" // Đỏ
  },
  {
    title: "TÔN TRỌNG ĐỒNG ĐẠO",
    desc: "Cấm lăng mạ, xúc phạm nhân phẩm, phân biệt vùng miền trên kênh thế giới. Giang hồ hiểm ác nhưng phải giữ lễ nghĩa. Vi phạm sẽ bị Cấm Ngôn (Mute) từ 1 ngày đến vĩnh viễn.",
    icon: "fas fa-praying-hands",
    type: "peace" // Xanh Ngọc
  },
  {
    title: "GIAO DỊCH CẨN TRỌNG",
    desc: "Bổn trang không chịu trách nhiệm và không hỗ trợ lấy lại tài sản đối với các giao dịch ngoài luồng (RMT - Real Money Trading) nếu bị lừa đảo. Hãy giao dịch qua Chợ (Marketplace).",
    icon: "fas fa-coins",
    type: "gold" // Vàng
  },
  {
    title: "TRỤC LỢI THIÊN CƠ",
    desc: "Cố tình lợi dụng lỗi game (Bug Exploit) để trục lợi sẽ bị thu hồi toàn bộ tài sản và khóa tài khoản. Nếu phát hiện lỗi, hãy báo ngay cho Thiên Phủ để nhận thưởng.",
    icon: "fas fa-bug",
    type: "void" // Tím
  }
];
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@400;600;700&family=Noto+Serif:wght@400;700&display=swap");

.wuxia-rules {
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
  background: radial-gradient(circle at 50% 0%, rgba(20, 50, 40, 0.4), transparent 70%);
}

/* LAYOUT */
.rules-layout {
  position: relative; z-index: 10;
  max-width: 900px; margin: 0 auto;
  height: 100vh; overflow-y: auto;
  padding: 40px 20px;
}
.custom-scroll::-webkit-scrollbar { width: 6px; }
.custom-scroll::-webkit-scrollbar-thumb { background: #5d4037; border-radius: 3px; }

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
  text-shadow: 0 0 20px rgba(183, 28, 28, 0.5); /* Red glow for rules */
  letter-spacing: 3px;
}
.decor-line { display: flex; align-items: center; justify-content: center; gap: 15px; margin: 15px 0; }
.gold-line { width: 250px; height: 2px; background: linear-gradient(90deg, transparent, #ffd700, transparent); }
.diamond-dot { width: 8px; height: 8px; background: #b71c1c; transform: rotate(45deg); box-shadow: 0 0 10px #b71c1c; }
.sub-title { color: #aaa; font-style: italic; letter-spacing: 1px; font-size: 1.1rem; }

/* RULES CONTAINER */
.rules-container { display: flex; flex-direction: column; gap: 25px; margin-bottom: 50px; }

/* RULE CARD */
.rule-card {
  display: flex; gap: 20px;
  background: rgba(20, 20, 20, 0.85);
  border: 1px solid #333;
  padding: 25px;
  border-radius: 4px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  animation: fadeInUp 0.6s ease backwards;
  animation-delay: var(--delay);
}

.rule-card:hover { transform: translateX(5px); }

/* Glow Effects based on Type */
.card-glow { position: absolute; inset: 0; opacity: 0; transition: 0.4s; pointer-events: none; z-index: 0; }
.rule-card:hover .card-glow { opacity: 1; }

/* Danger (Red) */
.rule-card.danger { border-left: 4px solid #d32f2f; }
.rule-card.danger .icon-box, .rule-card.danger .hex-border { color: #ef5350; border-color: #d32f2f; }
.rule-card.danger:hover { box-shadow: 0 5px 20px rgba(211, 47, 47, 0.2); border-color: #d32f2f; }
.rule-card.danger .card-glow { background: radial-gradient(circle at right, rgba(211, 47, 47, 0.1), transparent); }

/* Peace (Green) */
.rule-card.peace { border-left: 4px solid #00bfa5; }
.rule-card.peace .icon-box, .rule-card.peace .hex-border { color: #64ffda; border-color: #00bfa5; }
.rule-card.peace:hover { box-shadow: 0 5px 20px rgba(0, 191, 165, 0.2); border-color: #00bfa5; }
.rule-card.peace .card-glow { background: radial-gradient(circle at right, rgba(0, 191, 165, 0.1), transparent); }

/* Gold (Yellow) */
.rule-card.gold { border-left: 4px solid #ffd700; }
.rule-card.gold .icon-box, .rule-card.gold .hex-border { color: #ffd700; border-color: #ffd700; }
.rule-card.gold:hover { box-shadow: 0 5px 20px rgba(255, 215, 0, 0.2); border-color: #ffd700; }
.rule-card.gold .card-glow { background: radial-gradient(circle at right, rgba(255, 215, 0, 0.1), transparent); }

/* Void (Purple) */
.rule-card.void { border-left: 4px solid #9c27b0; }
.rule-card.void .icon-box, .rule-card.void .hex-border { color: #e040fb; border-color: #9c27b0; }
.rule-card.void:hover { box-shadow: 0 5px 20px rgba(156, 39, 176, 0.2); border-color: #9c27b0; }
.rule-card.void .card-glow { background: radial-gradient(circle at right, rgba(156, 39, 176, 0.1), transparent); }

/* Left Column (Number) */
.rule-number-col { display: flex; flex-direction: column; align-items: center; width: 50px; flex-shrink: 0; }
.hex-border {
  width: 40px; height: 40px;
  border: 2px solid #555;
  display: flex; align-items: center; justify-content: center;
  transform: rotate(45deg);
  transition: 0.3s;
  background: #111;
  position: relative; z-index: 2;
}
.num {
  transform: rotate(-45deg);
  font-family: "Cormorant Garamond", serif;
  font-weight: bold; font-size: 1.2rem;
}
.vertical-line { width: 1px; background: #333; flex: 1; margin-top: 10px; }

/* Content */
.rule-content { flex: 1; position: relative; z-index: 1; }
.rule-header { display: flex; align-items: center; gap: 15px; margin-bottom: 10px; }
.icon-box { font-size: 1.5rem; transition: 0.3s; }
.rule-title {
  margin: 0; font-family: "Cormorant Garamond", serif;
  font-size: 1.5rem; font-weight: 700; color: #fff;
  text-transform: uppercase; letter-spacing: 1px;
}
.rule-desc { margin: 0; color: #ccc; line-height: 1.6; font-size: 1rem; text-align: justify; }

.watermark {
  position: absolute; right: -10px; bottom: -20px;
  font-size: 6rem; opacity: 0.05; pointer-events: none;
  transform: rotate(-15deg);
}

/* FOOTER */
.rules-footer { text-align: center; margin-top: 20px; padding-bottom: 40px; }
.decree-box { display: inline-block; }
.decree-box p { font-family: "Cormorant Garamond", serif; font-style: italic; color: #888; margin-bottom: 10px; letter-spacing: 2px; }
.seal-red {
  width: 100px; height: 100px;
  border: 4px solid #b71c1c; border-radius: 12px;
  color: #b71c1c; font-weight: 900;
  display: flex; align-items: center; justify-content: center;
  text-align: center; font-family: "Cormorant Garamond", serif; font-size: 1.2rem;
  margin: 0 auto;
  transform: rotate(-5deg);
  mask-image: url("https://www.transparenttextures.com/patterns/grunge-wall.png");
  background: rgba(183, 28, 28, 0.1);
}

/* ANIMATIONS */
@keyframes fadeInUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }
.fade-in-down { animation: fadeInDown 0.8s ease; }
.fade-in { animation: fadeIn 1s ease; }
@keyframes fadeInDown { from { opacity: 0; transform: translateY(-30px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

/* RESPONSIVE */
@media (max-width: 600px) {
  .main-title { font-size: 2.2rem; }
  .rule-card { flex-direction: column; gap: 10px; padding: 20px; }
  .rule-number-col { flex-direction: row; align-items: center; gap: 10px; width: 100%; border-bottom: 1px dashed #333; padding-bottom: 10px; margin-bottom: 10px; }
  .vertical-line { display: none; }
  .hex-border { width: 30px; height: 30px; font-size: 0.9rem; }
  .rule-header { margin-bottom: 5px; }
  .rule-title { font-size: 1.3rem; }
}
</style>