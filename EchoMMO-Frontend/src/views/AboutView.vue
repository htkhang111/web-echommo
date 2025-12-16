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

      <div class="signature">
        <p>Thân ái,</p>
        <p class="seal-text">AURIX Team</p>
      </div>
    </div>
  </div>
</template>

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
}
.story-title:first-child {
  margin-top: 0;
}

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
</style>
