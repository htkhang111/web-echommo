<template>
  <div class="game-page">
    <div class="section-title">
      <h2>Giới Thiệu</h2>
      <div class="title-decor"></div>
    </div>

    <div class="wuxia-panel content-scroll">
      <h3 class="story-title">Khởi Nguyên</h3>
      <p>
        Thiên địa bất nhân, dĩ vạn vật vi sô cẩu. Thế giới
        <strong>ECHO MMO</strong> được hình thành từ hỗn mang, nơi linh khí hội
        tụ, vạn vật tranh đua tu luyện để đạt đến cảnh giới trường sinh.
      </p>
      <p>
        Tại đây, đạo hữu sẽ bắt đầu từ một phàm nhân, trải qua muôn vàn kiếp
        nạn, khám phá các bí cảnh, thu thập thiên tài địa bảo, và sáng tạo ra
        con đường đạo của riêng mình.
      </p>

      <h3 class="story-title">Sứ Mệnh</h3>
      <p>
        Đội ngũ <strong>AURIX Team</strong> mong muốn kiến tạo một sân chơi nhập
        vai thuần túy, nơi kỹ năng và tư duy chiến thuật được đặt lên hàng đầu.
      </p>

      <h3 class="story-title">Đội Ngũ Phát Triển</h3>
      <div class="dev-grid">
        <div class="dev-card" v-for="(dev, index) in devTeam" :key="index">
          <div class="dev-icon"><i class="fas fa-user-ninja"></i></div>
          <div class="dev-info">
            <div class="dev-name">{{ dev.name }}</div>
            <div class="dev-alias" v-if="dev.alias">Biệt hiệu: {{ dev.alias }}</div>
          </div>
        </div>
      </div>

      <div class="signature">
        <p>Thân ái,</p>
        <p class="seal-text">AURIX Team</p>
      </div>
    </div>
  </div>
</template>

<script setup>
const devTeam = [
  { name: "Trần Quang Lương", alias: "LuNu" },
  { name: "Lê Hoàng Lịnh", alias: "LuxxMel" },
  { name: "Huỳnh Trương Khang", alias: "" },
  { name: "Trịnh Hoàng Bảo Huy", alias: "BaoHuy" },
];
</script>

<style scoped>
/* Dùng lại style chung để đồng bộ */
.game-page {
  padding: 30px;
  animation: fadeIn 0.5s ease;
  max-width: 800px;
  margin: 0 auto;
}
.section-title {
  text-align: center;
  margin-bottom: 30px;
}
.section-title h2 {
  font-family: "Cinzel", serif;
  font-size: 2em;
  color: #fbc02d;
  margin: 0;
}
.title-decor {
  width: 100px;
  height: 3px;
  background: linear-gradient(90deg, transparent, #b71c1c, transparent);
  margin: 10px auto;
}

.wuxia-panel {
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid #5d4037;
  padding: 40px;
  border-radius: 4px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.5);
  color: #d7ccc8;
  font-size: 1.1em;
  line-height: 1.8;
  text-align: justify;
}

.story-title {
  color: #fbc02d;
  font-family: "Cinzel", serif;
  border-bottom: 1px solid #3e2723;
  padding-bottom: 10px;
  margin-top: 30px;
  margin-bottom: 20px;
}
.story-title:first-child {
  margin-top: 0;
}

/* --- [NEW] Style cho Dev Team --- */
.dev-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-top: 20px;
}

.dev-card {
  background: rgba(62, 39, 35, 0.4);
  border: 1px solid #5d4037;
  padding: 15px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 15px;
  transition: all 0.3s ease;
}

.dev-card:hover {
  border-color: #fbc02d;
  background: rgba(62, 39, 35, 0.8);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.3);
}

.dev-icon {
  width: 40px;
  height: 40px;
  background: #3e2723;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fbc02d;
  border: 1px solid #795548;
}

.dev-info {
  display: flex;
  flex-direction: column;
}

.dev-name {
  font-weight: bold;
  color: #efebe9;
  font-family: "Noto Serif TC", serif;
}

.dev-alias {
  font-size: 0.85em;
  color: #a1887f;
  font-style: italic;
}

/* --- Signature --- */
.signature {
  margin-top: 50px;
  text-align: right;
}
.seal-text {
  font-family: "Cinzel", serif;
  font-weight: bold;
  color: #b71c1c;
  font-size: 1.5em;
  text-transform: uppercase;
  letter-spacing: 2px;
  border: 3px double #b71c1c;
  display: inline-block;
  padding: 5px 15px;
  transform: rotate(-5deg);
  opacity: 0.8;
}

/* Responsive cho mobile */
@media (max-width: 600px) {
  .dev-grid {
    grid-template-columns: 1fr;
  }
}
</style>