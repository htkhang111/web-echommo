-- =========================================================
-- ECHOMMO DATABASE COMPLETE SETUP
-- Full restructured database with organized APIs
-- Compatible with EchoMMO v2.0
-- =========================================================

DROP DATABASE IF EXISTS echommo_db;
CREATE DATABASE echommo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE echommo_db;

-- 1. BẢNG USERS (Người chơi)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL DEFAULT '123456',
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100),
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    is_active BOOLEAN DEFAULT TRUE,
    ban_reason VARCHAR(255),
    banned_at DATETIME,
    is_captcha_locked BOOLEAN DEFAULT FALSE,
    captcha_fail_count INT DEFAULT 0,
    captcha_locked_until DATETIME,
    otp_code VARCHAR(10),
    otp_expiry DATETIME,
    avatar_url VARCHAR(255) DEFAULT '🐲',
    last_login DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- 2. BẢNG WALLET (Ví vàng & tài nguyên)
CREATE TABLE wallet (
    wallet_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    gold DECIMAL(18, 2) DEFAULT 100.00,
    diamonds INT DEFAULT 0,
    wood INT DEFAULT 0,
    stone INT DEFAULT 0,
    iron_ore INT DEFAULT 0,
    platinum INT DEFAULT 0,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_Wallet_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. BẢNG CHARACTERS (Nhân vật)
CREATE TABLE characters (
    char_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL UNIQUE,
    level INT DEFAULT 1,
    current_exp BIGINT DEFAULT 0,
    character_class VARCHAR(255) DEFAULT 'Nhà Thám Hiểm',
    status VARCHAR(20) DEFAULT 'IDLE',
    total_power INT DEFAULT 0,
    stat_points INT DEFAULT 0,
    str INT DEFAULT 5,
    vit INT DEFAULT 5,
    agi INT DEFAULT 5,
    int_stat INT DEFAULT 5,
    current_hp INT DEFAULT 100,
    max_hp INT DEFAULT 100,
    current_energy INT DEFAULT 50,
    max_energy INT DEFAULT 50,
    base_atk INT DEFAULT 10,
    base_def INT DEFAULT 5,
    base_speed INT DEFAULT 10,
    base_crit_rate INT DEFAULT 50,
    base_crit_dmg INT DEFAULT 150,
    current_location VARCHAR(100) DEFAULT 'Làng Tân Thủ',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Character_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
);

-- 4. BẢNG ITEMS (Kho vật phẩm hệ thống)
CREATE TABLE items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL,
    slot_type ENUM('WEAPON', 'HELMET', 'ARMOR', 'BOOTS', 'RING', 'NECKLACE', 'NONE') DEFAULT 'NONE',
    tier INT DEFAULT 1,
    base_rarity VARCHAR(20) DEFAULT 'COMMON',
    base_price DECIMAL(18, 2) DEFAULT 10,
    image_url VARCHAR(255),
    is_system_item BOOLEAN DEFAULT FALSE,
    atk_bonus INT DEFAULT 0,
    def_bonus INT DEFAULT 0,
    hp_bonus INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_type (type)
);

-- 5. BẢNG USER_ITEMS (Vật phẩm của người chơi)
CREATE TABLE user_items (
    user_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity INT DEFAULT 1,
    is_equipped BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    rarity ENUM('COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY', 'MYTHIC') DEFAULT 'COMMON',
    enhance_level INT DEFAULT 0,
    is_mythic BOOLEAN DEFAULT FALSE,
    mythic_level INT DEFAULT 0,
    main_stat_type VARCHAR(20),
    main_stat_value DECIMAL(10, 2) DEFAULT 0,
    original_main_stat_value DECIMAL(10, 2) DEFAULT 0,
    sub_stats JSON,
    acquired_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_UserItem_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_UserItem_Item FOREIGN KEY (item_id) REFERENCES items(item_id) ON DELETE CASCADE,
    INDEX idx_user_equipped (user_id, is_equipped),
    INDEX idx_user_id (user_id)
);

-- 6. BẢNG ENEMIES (Quái vật)
CREATE TABLE enemies (
    enemy_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    level INT DEFAULT 1,
    hp INT NOT NULL,
    atk INT NOT NULL,
    def INT NOT NULL,
    speed INT DEFAULT 10,
    exp_reward INT NOT NULL,
    gold_reward INT NOT NULL,
    image_url VARCHAR(255),
    drop_table JSON,
    INDEX idx_level (level)
);

-- 7. BẢNG SKILLS (Kỹ năng)
CREATE TABLE skills (
    skill_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    description VARCHAR(255),
    type VARCHAR(20),
    mana_cost INT DEFAULT 0,
    power INT DEFAULT 0,
    cooldown INT DEFAULT 0,
    required_level INT DEFAULT 1,
    image_url VARCHAR(255)
);

-- 8. BẢNG MARKET_LISTINGS (Danh sách bán của người chơi)
CREATE TABLE market_listings (
    listing_id INT AUTO_INCREMENT PRIMARY KEY,
    seller_id INT NOT NULL,
    user_item_id BIGINT,
    item_id INT NOT NULL,
    quantity INT DEFAULT 1,
    price DECIMAL(18, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Listing_Seller FOREIGN KEY (seller_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_Listing_UserItem FOREIGN KEY (user_item_id) REFERENCES user_items(user_item_id) ON DELETE SET NULL,
    CONSTRAINT FK_Listing_Item FOREIGN KEY (item_id) REFERENCES items(item_id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_item (item_id),
    INDEX idx_price (price)
);

-- 9. BẢNG DAILY_QUESTS (Nhiệm vụ hàng ngày)
CREATE TABLE daily_quests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type VARCHAR(50),
    description VARCHAR(255),
    target INT,
    progress INT DEFAULT 0,
    reward_gold INT,
    reward_exp INT,
    is_claimed BOOLEAN DEFAULT FALSE,
    created_date DATE,
    CONSTRAINT FK_Quest_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 10. BẢNG NOTIFICATIONS (Thông báo)
CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(100),
    message TEXT,
    type VARCHAR(20) DEFAULT 'INFO',
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Noti_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_read (user_id, is_read)
);

-- 11. BẢNG FRIENDSHIPS (Bạn bè)
CREATE TABLE friendships (
    id INT AUTO_INCREMENT PRIMARY KEY,
    requester_id INT NOT NULL,
    addressee_id INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT UQ_Friendship UNIQUE (requester_id, addressee_id),
    CONSTRAINT FK_Req FOREIGN KEY (requester_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_Addr FOREIGN KEY (addressee_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 12. BẢNG PRIVATE_MESSAGES (Tin nhắn riêng)
CREATE TABLE private_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_receiver (receiver_id),
    INDEX idx_sender (sender_id)
);

-- 13. BẢNG MESSAGES (Chat công cộng)
CREATE TABLE messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    content TEXT NOT NULL,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Chat_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 14. BẢNG CHAT_MESSAGES (Chat cải thiện)
CREATE TABLE chat_messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    channel VARCHAR(20) DEFAULT 'WORLD',
    receiver_id INT,
    content TEXT NOT NULL,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_ChatMsg_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_channel (channel),
    INDEX idx_receiver (receiver_id)
);

-- 15. BẢNG ANNOUNCEMENTS (Thông báo hệ thống)
CREATE TABLE announcements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) DEFAULT 'UPDATE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- 16. BẢNG BATTLE_SESSIONS (Phiên chiến đấu)
CREATE TABLE battle_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    enemy_id INT,
    enemy_name VARCHAR(255),
    enemy_max_hp INT,
    enemy_current_hp INT,
    enemy_atk INT,
    enemy_def INT,
    enemy_speed INT,
    player_current_hp INT,
    player_max_hp INT,
    player_current_energy INT,
    current_turn INT DEFAULT 0,
    is_qte_active BOOLEAN DEFAULT FALSE,
    qte_expiry_time DATETIME,
    last_action_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    spam_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 17. BẢNG FLAVOR_TEXT (Lời thoại hệ thống)
CREATE TABLE flavor_text (
    ft_id INT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 18. BẢNG WEATHER_TEXT (Mô tả thời tiết)
CREATE TABLE weather_text (
    wt_id INT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- DỮ LIỆU KHỞI TẠO (SEEDING)
-- =========================================================

-- Danh sách quái vật
INSERT INTO enemies (name, level, hp, atk, def, speed, exp_reward, gold_reward, image_url) VALUES
('Yêu Tinh', 1, 50, 8, 2, 8, 15, 10, 'idle_goblin'),
('Nấm Độc', 2, 80, 12, 3, 5, 25, 15, 'idle_mushroom'),
('Bộ Xương', 3, 120, 18, 5, 9, 40, 25, 'idle_skeleton');

-- Danh sách vật phẩm hệ thống
INSERT INTO items (name, description, type, slot_type, tier, base_rarity, base_price, image_url) VALUES
('Kiếm Gỗ', 'Kiếm tập luyện cho người mới', 'WEAPON', 'WEAPON', 1, 'COMMON', 50, 's_sword_0'),
('Kiếm Sắt', 'Sắc bén hơn kiếm gỗ', 'WEAPON', 'WEAPON', 1, 'RARE', 200, 's_sword_1'),
('Kiếm Hiệp Sĩ', 'Kiếm tiêu chuẩn của hiệp sĩ', 'WEAPON', 'WEAPON', 2, 'RARE', 500, 's_sword_2'),
('Áo Vải', 'Áo thô sơ che thân', 'ARMOR', 'ARMOR', 1, 'COMMON', 40, 'a_armor_0'),
('Áo Da', 'Làm từ da thú cứng', 'ARMOR', 'ARMOR', 1, 'RARE', 150, 'a_armor_1'),
('Mũ Da', 'Bảo vệ đầu cơ bản', 'ARMOR', 'HELMET', 1, 'COMMON', 45, 'h_helmet_0'),
('Mũ Sắt', 'Mũ bảo vệ cứng cáp', 'ARMOR', 'HELMET', 1, 'RARE', 160, 'h_helmet_1'),
('Giày Cỏ', 'Giúp đi lại đỡ đau chân', 'ARMOR', 'BOOTS', 1, 'COMMON', 30, 'b_boot_0'),
('Giày Da', 'Di chuyển nhanh nhẹn hơn', 'ARMOR', 'BOOTS', 1, 'RARE', 120, 'b_boot_1'),
('Nhẫn Đồng', 'Nhẫn trang sức đơn giản', 'ACCESSORY', 'RING', 1, 'COMMON', 100, 'ri_ring_0'),
('Nhẫn Bạc', 'Tăng nhẹ sức mạnh phép thuật', 'ACCESSORY', 'RING', 1, 'RARE', 300, 'ri_ring_1'),
('Vòng Cổ Đá', 'Vòng cổ làm từ đá thô', 'ACCESSORY', 'NECKLACE', 1, 'COMMON', 100, 'n_neck_0'),
('Vòng Cổ Ngọc', 'Phát ra ánh sáng nhẹ', 'ACCESSORY', 'NECKLACE', 1, 'RARE', 350, 'n_neck_1'),
('Bình Máu Nhỏ', 'Hồi 50 HP', 'CONSUMABLE', 'NONE', 1, 'COMMON', 20, 'https://cdn-icons-png.flaticon.com/512/863/863816.png');

-- Admin Account
INSERT INTO users (username, password_hash, password, email, full_name, role)
VALUES ('admin', '$2a$10$wW/i.b/w.w/wW/i.b/w.w/wW/i.b/w.w/wW/i.b/w.w/wW/i.b/w.w', '123456', 'admin@echommo.com', 'Game Master', 'ADMIN');

INSERT INTO characters (user_id, name, level, str, vit, agi, int_stat, current_hp, max_hp, base_atk, base_def)
VALUES (1, 'ADMIN', 99, 999, 999, 999, 999, 99999, 99999, 9999, 9999);

INSERT INTO wallet (user_id, gold, diamonds) VALUES (1, 999999, 99999);

INSERT INTO user_items (user_id, item_id, quantity, is_equipped, rarity, enhance_level, is_mythic, mythic_level, main_stat_type, main_stat_value, original_main_stat_value, sub_stats)
VALUES (1, 3, 1, TRUE, 'MYTHIC', 30, TRUE, 5, 'ATK_FLAT', 550, 500,
'[{"code": "CRIT_RATE", "value": 15.0, "is_percent": true}, {"code": "CRIT_DMG", "value": 25.0, "is_percent": true}, {"code": "SPEED", "value": 12, "is_percent": false}, {"code": "HP_PERCENT", "value": 8.0, "is_percent": true}]');

-- =========================================================
-- FLAVOR TEXT (Lời thoại)
-- =========================================================
INSERT INTO flavor_text (content) VALUES
('Giang hồ hiểm ác, người cười ta quá ngốc, ta cười người nhìn không thấu.'),
('Một kiếm xuất vỏ, trời đất đổi thay.'),
('Trong chén rượu nhạt, chứa cả phong vân càn khôn.'),
('Kẻ mạnh không bao giờ giải thích.'),
('Gió thổi mây tan, anh hùng lộ diện.');

-- =========================================================
-- WEATHER TEXT (Thời tiết)
-- =========================================================
INSERT INTO weather_text (content) VALUES
('Gió sớm thổi qua thung lũng, mang theo hơi lạnh của ngàn núi xa xăm.'),
('Mây trắng lững lờ, trôi như quên cả thời gian.'),
('Trời cao xanh lộng, chim bay không để lại dấu.'),
('Sương mờ phủ kín, che giấu mọi dấu vết của nhân gian.'),
('Hoàng hôn đỏ rực, nhuộm cả chân trời bằng sắc máu.');

-- =========================================================
-- ENDPOINTS MAPPING REFERENCE
-- =========================================================
-- Authentication APIs
-- POST   /api/auth/login              → Đăng nhập
-- POST   /api/auth/register           → Đăng ký
-- POST   /api/auth/forgot-password    → Quên mật khẩu
-- POST   /api/auth/reset-password     → Đặt lại mật khẩu

-- Game APIs
-- GET    /api/game/character          → Lấy nhân vật
-- POST   /api/game/explore            → Khám phá

-- Equipment APIs
-- GET    /api/equipment/inventory     → Lấy túi đồ
-- POST   /api/equipment/equip/{id}    → Mặc đồ
-- POST   /api/equipment/unequip/{id}  → Tháo đồ
-- POST   /api/equipment/enhance/{id}  → Cường hóa
-- POST   /api/equipment/upgrade/{id}  → Nâng cấp
-- POST   /api/equipment/evolve-mythic/{id} → Tiến hóa Mythic
-- POST   /api/equipment/upgrade-mythic/{id} → Nâng cấp Mythic

-- Shop APIs
-- GET    /api/shop/items              → Lấy danh sách shop
-- POST   /api/shop/buy                → Mua từ shop
-- POST   /api/shop/sell               → Bán cho shop

-- Player Market APIs
-- GET    /api/player-market/listings  → Danh sách bán của player
-- GET    /api/player-market/my-listings → Danh sách bán của tôi
-- POST   /api/player-market/create    → Tạo listing
-- POST   /api/player-market/buy/{id}  → Mua listing
-- POST   /api/player-market/cancel/{id} → Hủy listing

SELECT 'Database setup completed!' AS status;

INSERT INTO user_items (user_id, item_id, is_equipped, quantity, acquired_at)
VALUES (1, 1, false, 1, NOW());
