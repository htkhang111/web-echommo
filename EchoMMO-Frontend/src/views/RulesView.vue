<template>
  <div class="game-page">
    <div class="section-title">
      <h2>Luật Lệ Giang Hồ</h2>
      <div class="title-decor"></div>
    </div>

    <div class="wuxia-panel">
      <ul class="rules-list">
        <li>
          <span class="num">1</span>
          <div class="content">
            <strong>Cấm Sử Dụng Tà Thuật (Hack/Cheat):</strong>
            <p>
              Mọi hành vi can thiệp vào dữ liệu trò chơi sẽ bị phế bỏ tu vi vĩnh
              viễn (Ban nick).
            </p>
          </div>
        </li>
        <li>
          <span class="num">2</span>
          <div class="content">
            <strong>Tôn Trọng Đồng Đạo:</strong>
            <p>
              Không lăng mạ, xúc phạm người chơi khác trên kênh thế giới. Giang
              hồ hiểm ác nhưng cần giữ lễ nghĩa.
            </p>
          </div>
        </li>
        <li>
          <span class="num">3</span>
          <div class="content">
            <strong>Giao Dịch Cẩn Trọng:</strong>
            <p>
              Bổn trang không chịu trách nhiệm với các giao dịch ngoài luồng
              (RMT) bị lừa đảo.
            </p>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.game-page {
  padding: 30px;
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
  padding: 30px;
  border-radius: 4px;
}

.rules-list {
  list-style: none;
  padding: 0;
}
.rules-list li {
  display: flex;
  gap: 20px;
  margin-bottom: 25px;
  border-bottom: 1px dashed rgba(93, 64, 55, 0.5);
  padding-bottom: 20px;
}
.rules-list li:last-child {
  border-bottom: none;
}

.num {
  width: 40px;
  height: 40px;
  background: #b71c1c;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-family: "Cinzel", serif;
  border: 2px solid #fbc02d;
  flex-shrink: 0;
  transform: rotate(45deg);
  box-shadow: 0 0 10px #b71c1c;
}
.content {
  flex: 1;
  color: #d7ccc8;
}
.content strong {
  color: #fbc02d;
  font-size: 1.1em;
  display: block;
  margin-bottom: 5px;
}
.content p {
  margin: 0;
  font-size: 0.95em;
}
</style>
