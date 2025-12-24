USE echommo_db;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. [QUAN TRỌNG] Cắt bỏ cái khóa ngoại đang gây lỗi (Lấy đúng mã lỗi của ông)
-- Nếu lệnh này báo lỗi "Can't DROP...", hãy thử đổi tên bảng thành market_listings
ALTER TABLE market_listings DROP FOREIGN KEY FK8c2qmurc5e65pigydp8kh8uh2;

-- 2. Giờ thì sửa thoải mái
ALTER TABLE user_items
    MODIFY COLUMN user_item_id BIGINT NOT NULL AUTO_INCREMENT;

ALTER TABLE market_listings
    MODIFY COLUMN user_item_id BIGINT NOT NULL;

-- 3. Tạo lại mối quan hệ chính chủ (Tên đẹp, dễ quản lý)
ALTER TABLE market_listings
    ADD CONSTRAINT FK_Listing_UserItem_New
        FOREIGN KEY (user_item_id) REFERENCES user_items(user_item_id) ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;