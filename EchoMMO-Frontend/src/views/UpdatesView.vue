<template>
  <div class="game-page">
    <div class="section-title">
      <h2>Nhật Ký Cập Nhật</h2>
      <div class="title-decor"></div>
    </div>

    <div class="timeline">
      <div class="timeline-item">
        <div class="time-marker"></div>
        <div class="update-card latest">
          <div class="card-header">
            <span class="version">v1.0.2 - Open Beta</span>
            <span class="date">08/12/2025</span>
          </div>
          <ul class="changes">
            <li>
              <i class="fas fa-plus-circle"></i> Ra mắt tính năng Bang Hội.
            </li>
            <li>
              <i class="fas fa-wrench"></i> Cân bằng sức mạnh phái Võ Đang.
            </li>
            <li><i class="fas fa-bug"></i> Sửa lỗi hiển thị Túi Đồ.</li>
          </ul>
        </div>
      </div>

      <div class="timeline-item">
        <div class="time-marker"></div>
        <div class="update-card">
          <div class="card-header">
            <span class="version">v1.0.1 - Alpha Test</span>
            <span class="date">01/11/2025</span>
          </div>
          <ul class="changes">
            <li><i class="fas fa-plus-circle"></i> Mở bản đồ Rừng Trúc.</li>
            <li>
              <i class="fas fa-plus-circle"></i> Hệ thống ngũ hành tương khắc.
            </li>
          </ul>
        </div>
      </div>
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
  margin-bottom: 40px;
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

.timeline {
  position: relative;
  padding-left: 30px;
  border-left: 2px solid #5d4037;
}

.timeline-item {
  position: relative;
  margin-bottom: 40px;
}

.time-marker {
  position: absolute;
  left: -36px;
  top: 20px;
  width: 12px;
  height: 12px;
  background: #fbc02d;
  border-radius: 50%;
  border: 2px solid #b71c1c;
  box-shadow: 0 0 10px #fbc02d;
}

.update-card {
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid #5d4037;
  padding: 20px;
  border-radius: 4px;
  position: relative;
}
.update-card.latest {
  border-color: #fbc02d;
  box-shadow: 0 0 15px rgba(251, 192, 45, 0.1);
}
.update-card::before {
  content: "";
  position: absolute;
  left: -10px;
  top: 20px;
  border-top: 10px solid transparent;
  border-bottom: 10px solid transparent;
  border-right: 10px solid #5d4037;
}

.card-header {
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #3e2723;
  padding-bottom: 10px;
  margin-bottom: 15px;
}
.version {
  font-weight: bold;
  color: #fbc02d;
  font-family: "Cinzel", serif;
}
.date {
  color: #a1887f;
  font-size: 0.9em;
}

.changes {
  list-style: none;
  padding: 0;
  margin: 0;
  color: #d7ccc8;
}
.changes li {
  margin-bottom: 8px;
}
.changes i {
  color: #81c784;
  margin-right: 8px;
  width: 20px;
  text-align: center;
}
.changes i.fa-bug {
  color: #e57373;
}
.changes i.fa-wrench {
  color: #64b5f6;
}
</style>
