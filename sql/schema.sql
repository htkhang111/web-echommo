DROP DATABASE IF EXISTS echommo_db;
CREATE DATABASE echommo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE echommo_db;

-- ==========================================
-- 1. USERS & AUTHENTICATION
-- ==========================================
CREATE TABLE users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL DEFAULT '123456', -- Lưu plain text để debug (production nên bỏ)
                       email VARCHAR(100) NOT NULL UNIQUE,
                       full_name VARCHAR(100),
                       role ENUM('USER', 'ADMIN') DEFAULT 'USER',

    -- Security & Status
                       is_active BOOLEAN DEFAULT TRUE,
                       ban_reason VARCHAR(255),
                       banned_at DATETIME,
                       is_captcha_locked BOOLEAN DEFAULT FALSE,
                       captcha_fail_count INT DEFAULT 0,
                       captcha_locked_until DATETIME,
                       otp_code VARCHAR(10),
                       otp_expiry DATETIME,

    -- Info
                       avatar_url VARCHAR(255) DEFAULT '🐲',
                       last_login DATETIME,
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                       INDEX idx_username (username),
                       INDEX idx_email (email)
);

-- ==========================================
-- 2. ECONOMY (WALLET)
-- ==========================================
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

-- ==========================================
-- 3. CHARACTER SYSTEM
-- ==========================================
CREATE TABLE characters (
                            char_id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id INT NOT NULL UNIQUE,
                            name VARCHAR(50) NOT NULL UNIQUE,

    -- Level & Class
                            level INT DEFAULT 1,
                            current_exp BIGINT DEFAULT 0,
                            character_class VARCHAR(255) DEFAULT 'Nhà Thám Hiểm',
                            status VARCHAR(20) DEFAULT 'IDLE',
                            current_location VARCHAR(100) DEFAULT 'Làng Tân Thủ',

    -- Stats cơ bản
                            total_power INT DEFAULT 0,
                            stat_points INT DEFAULT 0,
                            str INT DEFAULT 5,
                            vit INT DEFAULT 5,
                            agi INT DEFAULT 5,
                            int_stat INT DEFAULT 5, -- Chỉ số mới: Trí tuệ

    -- Stats tính toán
                            current_hp INT DEFAULT 100,
                            max_hp INT DEFAULT 100,
                            current_energy INT DEFAULT 50,
                            max_energy INT DEFAULT 50,
                            base_atk INT DEFAULT 10,
                            base_def INT DEFAULT 5,
                            base_speed INT DEFAULT 10,
                            base_crit_rate INT DEFAULT 50,  -- 5.0%
                            base_crit_dmg INT DEFAULT 150,  -- 150%

    -- Spa System
                            spa_start_time DATETIME,
                            spa_end_time DATETIME,
                            spa_package_type VARCHAR(50),

                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                            last_active DATETIME DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT FK_Character_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ==========================================
-- 4. ITEMS & INVENTORY
-- ==========================================
-- Bảng định nghĩa Item gốc
CREATE TABLE items (
                       item_id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       description TEXT,
                       type VARCHAR(20) NOT NULL, -- MATERIAL, WEAPON, ARMOR...
                       slot_type ENUM('WEAPON', 'HELMET', 'ARMOR', 'BOOTS', 'RING', 'NECKLACE', 'NONE') DEFAULT 'NONE',
                       tier INT DEFAULT 1,
                       base_rarity VARCHAR(20) DEFAULT 'COMMON',
                       base_price DECIMAL(18, 2) DEFAULT 10,
                       image_url VARCHAR(255),
                       is_system_item BOOLEAN DEFAULT FALSE,

    -- Base stats cho item
                       atk_bonus INT DEFAULT 0,
                       def_bonus INT DEFAULT 0,
                       hp_bonus INT DEFAULT 0,
                       speed_bonus INT DEFAULT 0,

                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Bảng Inventory (Item của người chơi) - LIÊN KẾT VỚI CHARACTER (Updated)
CREATE TABLE user_items (
                            user_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            char_id INT NOT NULL, -- Đã đổi từ user_id sang char_id
                            item_id INT NOT NULL,

                            quantity INT DEFAULT 1,
                            is_equipped BOOLEAN DEFAULT FALSE,
                            is_locked BOOLEAN DEFAULT FALSE,

    -- Stats riêng của item này (Cường hóa/Random)
                            rarity ENUM('COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY', 'MYTHIC') DEFAULT 'COMMON',
                            enhance_level INT DEFAULT 0,

    -- Mythic System
                            is_mythic BOOLEAN DEFAULT FALSE,
                            mythic_level INT DEFAULT 0,

    -- Main Stat
                            main_stat_type VARCHAR(20),
                            main_stat_value DECIMAL(10, 2) DEFAULT 0,
                            original_main_stat_value DECIMAL(10, 2) DEFAULT 0,

    -- Sub Stats (Lưu JSON)
                            sub_stats TEXT, -- Dùng TEXT an toàn hơn JSON trên một số DB cũ

                            acquired_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT FK_UserItem_Character FOREIGN KEY (char_id) REFERENCES characters(char_id) ON DELETE CASCADE,
                            CONSTRAINT FK_UserItem_Item FOREIGN KEY (item_id) REFERENCES items(item_id) ON DELETE CASCADE,
                            INDEX idx_char_equipped (char_id, is_equipped)
);

-- ==========================================
-- 5. GAMEPLAY (ENEMIES, BATTLE, QUESTS)
-- ==========================================
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
                         drop_table JSON -- Danh sách vật phẩm có thể rơi
);

CREATE TABLE battle_sessions (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 char_id INT NOT NULL UNIQUE, -- Liên kết với Character

    -- Enemy Info Snapshot
                                 enemy_id INT,
                                 enemy_name VARCHAR(255),
                                 enemy_max_hp INT,
                                 enemy_current_hp INT,
                                 enemy_atk INT,
                                 enemy_def INT,
                                 enemy_speed INT,

    -- Player Status Snapshot
                                 player_current_hp INT,
                                 player_max_hp INT,
                                 player_current_energy INT,

    -- Turn Info
                                 current_turn INT DEFAULT 0,
                                 is_qte_active BOOLEAN DEFAULT FALSE,
                                 qte_expiry_time DATETIME,
                                 last_action_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 spam_count INT DEFAULT 0,

                                 log TEXT, -- Battle Log
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT FK_Battle_Character FOREIGN KEY (char_id) REFERENCES characters(char_id) ON DELETE CASCADE
);

CREATE TABLE daily_quests (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              user_id INT NOT NULL,
                              type VARCHAR(50), -- KILL_MONSTER, GATHER...
                              description VARCHAR(255),
                              target INT,
                              progress INT DEFAULT 0,
                              reward_gold INT,
                              reward_exp INT,
                              is_claimed BOOLEAN DEFAULT FALSE,
                              created_date DATE,
                              CONSTRAINT FK_Quest_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ==========================================
-- 6. SOCIAL & MARKET
-- ==========================================
CREATE TABLE market_listings (
                                 listing_id INT AUTO_INCREMENT PRIMARY KEY,
                                 seller_id INT NOT NULL,
                                 user_item_id BIGINT,
                                 item_id INT NOT NULL,

                                 quantity INT DEFAULT 1,
                                 price DECIMAL(18, 2) NOT NULL,
                                 status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, SOLD, CANCELLED
                                 enhance_level INT DEFAULT 0, -- Cache lại để hiển thị nhanh

                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT FK_Listing_Seller FOREIGN KEY (seller_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                 CONSTRAINT FK_Listing_UserItem FOREIGN KEY (user_item_id) REFERENCES user_items(user_item_id) ON DELETE SET NULL,
                                 CONSTRAINT FK_Listing_Item FOREIGN KEY (item_id) REFERENCES items(item_id) ON DELETE CASCADE
);

CREATE TABLE friendships (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             requester_id INT NOT NULL,
                             addressee_id INT NOT NULL,
                             status VARCHAR(20) DEFAULT 'PENDING',
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT UQ_Friendship UNIQUE (requester_id, addressee_id),
                             CONSTRAINT FK_Friend_Req FOREIGN KEY (requester_id) REFERENCES users(user_id) ON DELETE CASCADE,
                             CONSTRAINT FK_Friend_Addr FOREIGN KEY (addressee_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ==========================================
-- 7. COMMUNICATION (CHAT & NOTIFICATIONS)
-- ==========================================
CREATE TABLE chat_messages (
                               message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id INT NOT NULL,
                               channel VARCHAR(20) DEFAULT 'WORLD', -- WORLD, CLAN, PRIVATE
                               receiver_id INT, -- Nếu là private
                               content TEXT NOT NULL,
                               sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT FK_Chat_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

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

CREATE TABLE notifications (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               user_id INT NOT NULL,
                               title VARCHAR(100),
                               message TEXT,
                               type VARCHAR(20) DEFAULT 'INFO',
                               is_read BOOLEAN DEFAULT FALSE,
                               created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT FK_Noti_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE announcements (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               title VARCHAR(255) NOT NULL,
                               content TEXT NOT NULL,
                               type VARCHAR(50) DEFAULT 'UPDATE',
                               created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                               is_active BOOLEAN DEFAULT TRUE
);

-- ==========================================
-- 8. FLAVOR TEXT (LORE)
-- ==========================================
CREATE TABLE flavor_text (
                             ft_id INT AUTO_INCREMENT PRIMARY KEY,
                             content VARCHAR(255) NOT NULL,
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE weather_text (
                              wt_id INT AUTO_INCREMENT PRIMARY KEY,
                              content VARCHAR(255) NOT NULL,
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

USE echommo_db;

ALTER TABLE items
    MODIFY COLUMN slot_type ENUM('NONE', 'WEAPON', 'ARMOR', 'HELMET', 'BOOTS', 'RING', 'NECKLACE', 'CONSUMABLE', 'MATERIAL') DEFAULT 'NONE';

USE echommo_db;

ALTER TABLE characters
    ADD COLUMN gathering_item_id INT DEFAULT NULL,
    ADD COLUMN gathering_remaining_amount INT DEFAULT 0,
    ADD COLUMN gathering_expiry DATETIME DEFAULT NULL;


USE echommo_db;

-- =============================================
-- 1. SETUP TABLE ITEMS (RE-INDEXED IDs)
-- =============================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE echommo_db.items;
SET FOREIGN_KEY_CHECKS = 1;

-- --- GROUP 1: GỖ (WOODS) ---
-- ID chạy từ 1 -> 4
INSERT INTO echommo_db.items (item_id, name, type, rarity, tier, base_price, description, image_url, slot_type) VALUES
                                                                                                                    (1, 'Gỗ Sồi',     'MATERIAL', 'COMMON',   1, 10, 'Vật phẩm hệ thống.', 'r_wood.png',       'MATERIAL'),
                                                                                                                    (2, 'Gỗ Khô',     'MATERIAL', 'COMMON',   1, 10, 'Vật phẩm hệ thống.', 'r_red_wood.png',   'MATERIAL'),
                                                                                                                    (3, 'Gỗ Lạnh',    'MATERIAL', 'UNCOMMON', 2, 10, 'Vật phẩm hệ thống.', 'r_white_wood.png', 'MATERIAL'),
                                                                                                                    (4, 'Gỗ Lạ',      'MATERIAL', 'RARE',     3, 10, 'Vật phẩm hệ thống.', 'r_black_wood.png', 'MATERIAL');

-- --- GROUP 2: KHOÁNG SẢN (MINERALS) ---
-- ID chạy từ 5 -> 8
INSERT INTO echommo_db.items (item_id, name, type, rarity, tier, base_price, description, image_url, slot_type) VALUES
                                                                                                                    (5, 'Đá',         'MATERIAL', 'COMMON',   1, 10, 'Vật phẩm hệ thống.', 'stone_1.png',      'MATERIAL'),
                                                                                                                    (6, 'Quặng Đồng', 'MATERIAL', 'COMMON',   1, 10, 'Vật phẩm hệ thống.', 'r_copper_node.png','MATERIAL'),
                                                                                                                    (7, 'Sắt',        'MATERIAL', 'RARE',     2, 10, 'Vật phẩm hệ thống.', 'r_silver_node.png','MATERIAL'),
                                                                                                                    (8, 'Bạch Kim',   'MATERIAL', 'EPIC',     3, 10, 'Vật phẩm hệ thống.', 'r_mystrile_node.png','MATERIAL');

-- --- GROUP 3: THỰC PHẨM (FOOD/FISH) ---
-- ID chạy từ 9 -> 10
INSERT INTO echommo_db.items (item_id, name, type, rarity, tier, base_price, description, image_url, slot_type) VALUES
                                                                                                                    (9, 'Cá',         'MATERIAL', 'COMMON',   1, 10, 'Vật phẩm hệ thống.', 'r_fish.png',       'MATERIAL'),
                                                                                                                    (10, 'Cá Mập',    'MATERIAL', 'UNCOMMON', 2, 10, 'Vật phẩm hệ thống.', 'r_shark.png',      'MATERIAL');

-- --- GROUP 4: ĐẶC BIỆT (SPECIAL) ---
-- ID chạy từ 11 -> 12
INSERT INTO echommo_db.items (item_id, name, type, rarity, tier, base_price, description, image_url, slot_type) VALUES
                                                                                                                    (11, 'Echo Coin',      'MATERIAL', 'LEGENDARY', 5, 10, 'Vật phẩm hệ thống.', 'r_echo_coin.png', 'MATERIAL'),
                                                                                                                    (12, 'Nguyên liệu lạ', 'MATERIAL', 'EPIC',      4, 10, 'Vật phẩm hệ thống.', 'r_unknown.png',   'MATERIAL');

-- --- GROUP 5: TRANG BỊ & TIÊU HAO ---
-- ID chạy tiếp từ 13 -> 15
INSERT INTO echommo_db.items (item_id, name, type, rarity, tier, base_price, description, image_url, slot_type) VALUES
                                                                                                                    (13, 'Kiếm Gỗ',   'WEAPON',     'COMMON', 1, 10, 'Vật phẩm hệ thống.', 's_sword_0.png',  'WEAPON'),
                                                                                                                    (14, 'Áo Vải',    'ARMOR',      'COMMON', 1, 10, 'Vật phẩm hệ thống.', 'a_armor_0.png',  'ARMOR'),
                                                                                                                    (15, 'Bình Máu',  'CONSUMABLE', 'COMMON', 1, 10, 'Vật phẩm hệ thống.', 'r_potion.png',   'CONSUMABLE');


-- =============================================
-- 2. SETUP TABLE WALLET (CẤU TRÚC KHỚP ID)
-- =============================================
DROP TABLE IF EXISTS wallet;

CREATE TABLE wallet (
                        wallet_id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT NOT NULL UNIQUE,

    -- Currency
                        gold DECIMAL(18, 2) DEFAULT 100.00,
                        diamonds INT DEFAULT 0,

    -- --- GROUP 1: GỖ (ID 1-4) ---
                        wood INT DEFAULT 0,           -- Gỗ Sồi (ID 1)
                        dried_wood INT DEFAULT 0,     -- Gỗ Khô (ID 2)
                        cold_wood INT DEFAULT 0,      -- Gỗ Lạnh (ID 3)
                        strange_wood INT DEFAULT 0,   -- Gỗ Lạ  (ID 4)

    -- --- GROUP 2: KHOÁNG SẢN (ID 5-8) ---
                        stone INT DEFAULT 0,          -- Đá (ID 5)
                        copper_ore INT DEFAULT 0,     -- Đồng (ID 6)
                        iron_ore INT DEFAULT 0,       -- Sắt (ID 7)
                        platinum INT DEFAULT 0,       -- Bạch Kim (ID 8)

    -- --- GROUP 3: THỰC PHẨM (ID 9-10) ---
                        fish INT DEFAULT 0,           -- Cá (ID 9)
                        shark INT DEFAULT 0,          -- Cá Mập (ID 10)

    -- --- GROUP 4: ĐẶC BIỆT (ID 11-12) ---
                        echo_coin INT DEFAULT 0,      -- Echo Coin (ID 11)
                        unknown_material INT DEFAULT 0, -- Nguyên liệu lạ (ID 12)

                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT FK_Wallet_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- =============================================
-- 3. SEED DATA WALLET (ADMIN)
-- =============================================
INSERT INTO wallet (
    user_id, gold, diamonds,
    wood, dried_wood, cold_wood, strange_wood,
    stone, copper_ore, iron_ore, platinum,
    fish, shark,
    echo_coin, unknown_material
)
VALUES (
           1, 999999, 99999,
           1000, 100, 100, 50,    -- Gỗ
           1000, 500, 500, 100,   -- Khoáng
           200, 10,               -- Thực phẩm
           5, 1                   -- Đặc biệt
       );

USE echommo_db;

-- Cập nhật cân bằng dựa trên Level của quái vật
UPDATE enemies
SET
    -- Công thức Gold: (Level * 10) + Random từ 5 đến 15
    gold_reward = (level * 10) + FLOOR(5 + (RAND() * 11)),

    -- Công thức EXP: (Level * 15) + Random từ 10 đến 20
    exp_reward = (level * 15) + FLOOR(10 + (RAND() * 11))
WHERE level > 0;

-- Tinh chỉnh riêng cho các quái vật cụ thể để tạo đặc trưng (nếu muốn)
-- Ví dụ: Yêu Tinh Lv1 sẽ yếu nhưng nhiều vàng hơn chút
UPDATE enemies SET gold_reward = 15, exp_reward = 20 WHERE name = 'Yêu Tinh';
-- Nấm Độc Lv2
UPDATE enemies SET gold_reward = 28, exp_reward = 45 WHERE name = 'Nấm Độc';
-- Bộ Xương Lv3
UPDATE enemies SET gold_reward = 45, exp_reward = 75 WHERE name = 'Bộ Xương';

-- Kiểm tra lại kết quả
SELECT enemy_id, name, level, gold_reward, exp_reward FROM enemies;