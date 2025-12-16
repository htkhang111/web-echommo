DROP DATABASE IF EXISTS echommo_db;
CREATE DATABASE echommo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE echommo_db;

-- 1. BẢNG USERS
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
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
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. BẢNG WALLET
CREATE TABLE wallet (
    wallet_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    gold DECIMAL(18, 2) DEFAULT 100.00,
    diamonds INT DEFAULT 0,
    wood INT DEFAULT 0,
    stone INT DEFAULT 0,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_Wallet_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. BẢNG CHARACTERS (ĐÃ SỬA TÊN CỘT KHỚP VỚI JAVA: char_id)
CREATE TABLE characters (
    char_id INT AUTO_INCREMENT PRIMARY KEY, -- Sửa từ character_id -> char_id
    user_id INT NOT NULL,
    name VARCHAR(50) NOT NULL UNIQUE,
    level INT DEFAULT 1,
    current_exp INT DEFAULT 0,

    -- Chỉ số cơ bản
    current_hp INT DEFAULT 100,
    max_hp INT DEFAULT 100,
    current_energy INT DEFAULT 50,
    max_energy INT DEFAULT 50,

    base_atk INT DEFAULT 10,
    base_def INT DEFAULT 5,
    base_speed INT DEFAULT 10,
    base_crit_rate INT DEFAULT 5,
    base_crit_dmg INT DEFAULT 150,

    current_location VARCHAR(100) DEFAULT 'Làng Tân Thủ',
    CONSTRAINT FK_Character_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 4. BẢNG ITEMS
CREATE TABLE items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL,
    rarity VARCHAR(5) DEFAULT 'C',
    base_price DECIMAL(18, 2) DEFAULT 0,
    image_url VARCHAR(255),
    is_system_item BOOLEAN DEFAULT FALSE,
    is_equipped BOOLEAN DEFAULT FALSE, -- Thêm cột này nếu Entity có (nhưng thường user_items mới cần)

    -- Bonus Stats
    atk_bonus INT DEFAULT 0,
    def_bonus INT DEFAULT 0,
    hp_bonus INT DEFAULT 0,
    energy_bonus INT DEFAULT 0,
    speed_bonus INT DEFAULT 0,
    crit_rate_bonus INT DEFAULT 0
);

-- 5. BẢNG USER ITEMS
CREATE TABLE user_items (
    user_item_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity INT DEFAULT 1,
    is_equipped BOOLEAN DEFAULT FALSE,
    enhance_level INT DEFAULT 0,
    acquired_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_UserItem_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_UserItem_Item FOREIGN KEY (item_id) REFERENCES items(item_id) ON DELETE CASCADE
);

-- 6. BẢNG ENEMIES
CREATE TABLE enemies (
    enemy_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    level INT DEFAULT 1,
    hp INT NOT NULL,
    atk INT NOT NULL,
    def INT NOT NULL,
    exp_reward INT NOT NULL,
    gold_reward INT NOT NULL,
    image_url VARCHAR(255)
);

-- 7. BẢNG SKILLS
CREATE TABLE skills (
    skill_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    description VARCHAR(255),
    type VARCHAR(20),
    mana_cost INT DEFAULT 0,
    power INT DEFAULT 0,
    required_level INT DEFAULT 1,
    image_url VARCHAR(255)
);

-- 8. BẢNG MARKET
CREATE TABLE market_listings (
    listing_id INT AUTO_INCREMENT PRIMARY KEY,
    seller_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity INT DEFAULT 1,
    price DECIMAL(18, 2) NOT NULL,
    enhance_level INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Listing_Seller FOREIGN KEY (seller_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_Listing_Item FOREIGN KEY (item_id) REFERENCES items(item_id) ON DELETE CASCADE
);

-- 9. BẢNG DAILY QUESTS
CREATE TABLE daily_quests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type VARCHAR(50), description VARCHAR(255), target INT, progress INT DEFAULT 0,
    reward_gold INT, reward_exp INT, is_claimed BOOLEAN DEFAULT FALSE, created_date DATE,
    CONSTRAINT FK_Quest_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 10. BẢNG NOTIFICATIONS
CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(100), message TEXT, type VARCHAR(20) DEFAULT 'INFO',
    is_read BOOLEAN DEFAULT FALSE, created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Noti_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 11. BẢNG FRIENDSHIPS
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

-- 12. BẢNG PRIVATE MESSAGES
CREATE TABLE private_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 13. BẢNG GLOBAL MESSAGES
CREATE TABLE messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    content TEXT NOT NULL,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Chat_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 14. BẢNG ANNOUNCEMENTS
CREATE TABLE announcements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) DEFAULT 'UPDATE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- DATA SEEDING
INSERT INTO enemies (name, level, hp, atk, def, exp_reward, gold_reward, image_url) VALUES
('Yêu Tinh', 1, 50, 8, 2, 15, 10, '/src/assets/enemy/idle_goblin.png'),
('Nấm Độc', 2, 80, 12, 3, 25, 15, '/src/assets/enemy/idle_mushroom.png'),
('Bộ Xương', 3, 120, 18, 5, 40, 25, '/src/assets/enemy/idle_skeleton.png');

INSERT INTO items (name, description, type, rarity, base_price, image_url, is_system_item, atk_bonus, def_bonus, hp_bonus, energy_bonus, speed_bonus, crit_rate_bonus) VALUES
('Kiếm Gỗ', 'Kiếm tập luyện', 'WEAPON', 'C', 50, 'https://cdn-icons-png.flaticon.com/512/2665/2665903.png', 1, 5, 0, 0, 0, 0, 0),
('Kiếm Sắt', 'Sắc bén', 'WEAPON', 'B', 200, 'https://cdn-icons-png.flaticon.com/512/1037/1037974.png', 1, 15, 0, 0, 0, 0, 2),
('Áo Vải', 'Áo thô sơ', 'ARMOR', 'C', 40, 'https://cdn-icons-png.flaticon.com/512/2553/2553235.png', 1, 0, 5, 10, 0, 0, 0),
('Bình Máu', 'Hồi 50 HP', 'CONSUMABLE', 'C', 20, 'https://cdn-icons-png.flaticon.com/512/863/863816.png', 1, 0, 0, 50, 0, 0, 0);

-- Admin User (Password: 123456)
USE echommo_db;

-- Tạo nhân vật "Bá Đạo" cho Admin (User ID = 1)
INSERT INTO characters (user_id, name, level, current_exp, current_hp, max_hp, current_energy, max_energy, base_atk, base_def, base_speed, base_crit_rate, base_crit_dmg)
VALUES (1, 'ADMIN', 99, 0, 9999, 9999, 9999, 9999, 999, 999, 50, 100, 200);

-- Cập nhật lại ví tiền cho Admin luôn (cho giàu)
UPDATE wallet SET gold = 99999, diamonds = 9999 WHERE user_id = 1;