<template>
  <div class="game-page">
    <div class="section-title">
      <h2>Chính Sách Bảo Mật</h2>
      <div class="title-decor"></div>
    </div>

    <div class="wuxia-panel content-scroll">
      <p>
        <strong>1. Thu thập thông tin:</strong> Chúng tôi chỉ thu thập tên tài
        khoản, email để phục vụ việc đăng nhập và bảo mật.
      </p>
      <p>
        <strong>2. Bảo vệ dữ liệu:</strong> Mật khẩu của đạo hữu được mã hóa
        (Bcrypt) và lưu trữ an toàn trong Tàng Kinh Các.
      </p>
      <p>
        <strong>3. Chia sẻ thông tin:</strong> Chúng tôi cam kết không chia sẻ
        thông tin của đạo hữu cho bất kỳ môn phái thứ ba nào.
      </p>
      <p>
        <em
          >(Đây là văn bản mẫu, vui lòng cập nhật theo chính sách thực tế của
          bạn).</em
        >
      </p>
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
  color: #d7ccc8;
  line-height: 1.8;
}
strong {
  color: #fbc02d;
}
</style>
