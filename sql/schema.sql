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

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE echommo_db.items;
SET FOREIGN_KEY_CHECKS = 1;
insert into echommo_db.items (attack, base_price, defense, hp, item_id, speed, tier, description, image_url, name, type, rarity, slot_type) values (null, 10, null, null, 1, null, 1, 'Vật phẩm hệ thống.', 'r_wood.png', 'Gỗ Xoài', 'MATERIAL', 'COMMON', 'MATERIAL');
insert into echommo_db.items (attack, base_price, defense, hp, item_id, speed, tier, description, image_url, name, type, rarity, slot_type) values (null, 10, null, null, 2, null, 1, 'Vật phẩm hệ thống.', 'stone_1.png', 'Đá', 'MATERIAL', 'COMMON', 'MATERIAL');
insert into echommo_db.items (attack, base_price, defense, hp, item_id, speed, tier, description, image_url, name, type, rarity, slot_type) values (null, 10, null, null, 3, null, 1, 'Vật phẩm hệ thống.', 'r_copper_node.png', 'Quặng Đồng', 'MATERIAL', 'COMMON', 'MATERIAL');
insert into echommo_db.items (attack, base_price, defense, hp, item_id, speed, tier, description, image_url, name, type, rarity, slot_type) values (null, 10, null, null, 4, null, 2, 'Vật phẩm hệ thống.', 'r_silver_node.png', 'Sắt', 'MATERIAL', 'RARE', 'MATERIAL');
insert into echommo_db.items (attack, base_price, defense, hp, item_id, speed, tier, description, image_url, name, type, rarity, slot_type) values (null, 10, null, null, 5, null, 1, 'Vật phẩm hệ thống.', 'r_fish.png', 'Cá', 'MATERIAL', 'COMMON', 'MATERIAL');
insert into echommo_db.items (attack, base_price, defense, hp, item_id, speed, tier, description, image_url, name, type, rarity, slot_type) values (null, 10, null, null, 6, null, 3, 'Vật phẩm hệ thống.', 'r_mystrile_node.png', 'Bạch Kim', 'MATERIAL', 'EPIC', 'MATERIAL');
insert into echommo_db.items (attack, base_price, defense, hp, item_id, speed, tier, description, image_url, name, type, rarity, slot_type) values (null, 10, null, null, 7, null, 1, 'Vật phẩm hệ thống.', 'r_red_wood.png', 'Gỗ Khô', 'MATERIAL', 'COMMON', 'MATERIAL');
insert into echommo_db.items (attack, base_price, defense, hp, item_id, speed, tier, description, image_url, name, type, rarity, slot_type) values (null, 10, null, null, 8, null, 2, 'Vật phẩm hệ thống.', 'r_white_wood.png', 'Gỗ Lạnh', 'MATERIAL', 'UNCOMMON', 'MATERIAL');
insert into echommo_db.items (attack, base_price, defense, hp, item_id, speed, tier, description, image_url, name, type, rarity, slot_type) values (null, 10, null, null, 9, null, 3, 'Vật phẩm hệ thống.', 'r_black_wood.png', 'Gỗ Lạ', 'MATERIAL', 'RARE', 'MATERIAL');
insert into echommo_db.items (attack, base_price, defense, hp, item_id, speed, tier, description, image_url, name, type, rarity, slot_type) values (null, 10, null, null, 10, null, 2, 'Vật phẩm hệ thống.', 'r_shark.png', 'Cá Mập', 'MATERIAL', 'UNCOMMON', 'MATERIAL');
insert into echommo_db.items (attack, base_price, defense, hp, item_id, speed, tier, description, image_url, name, type, rarity, slot_type) values (null, 10, null, null, 11, null, 5, 'Vật phẩm hệ thống.', 'r_echo_coin.png', 'Echo Coin', 'MATERIAL', 'LEGENDARY', 'MATERIAL');
insert into echommo_db.items (attack, base_price, defense, hp, item_id, speed, tier, description, image_url, name, type, rarity, slot_type) values (null, 10, null, null, 12, null, 4, 'Vật phẩm hệ thống.', 'r_unknown.png', 'Nguyên liệu lạ', 'MATERIAL', 'EPIC', 'MATERIAL');
insert into echommo_db.items (attack, base_price, defense, hp, item_id, speed, tier, description, image_url, name, type, rarity, slot_type) values (null, 10, null, null, 13, null, 1, 'Vật phẩm hệ thống.', 's_sword_0.png', 'Kiếm Gỗ', 'WEAPON', 'COMMON', 'WEAPON');
insert into echommo_db.items (attack, base_price, defense, hp, item_id, speed, tier, description, image_url, name, type, rarity, slot_type) values (null, 10, null, null, 14, null, 1, 'Vật phẩm hệ thống.', 'a_armor_0.png', 'Áo Vải', 'ARMOR', 'COMMON', 'ARMOR');
insert into echommo_db.items (attack, base_price, defense, hp, item_id, speed, tier, description, image_url, name, type, rarity, slot_type) values (null, 10, null, null, 15, null, 1, 'Vật phẩm hệ thống.', 'r_potion.png', 'Bình Máu', 'CONSUMABLE', 'COMMON', 'CONSUMABLE');


ALTER TABLE characters
    ADD COLUMN gathering_item_id INT DEFAULT NULL,
    ADD COLUMN gathering_remaining_amount INT DEFAULT 0,
    ADD COLUMN gathering_expiry DATETIME DEFAULT NULL;

SHOW COLUMNS FROM characters;

USE echommo_db;

-- 1. Sửa lại dữ liệu Items cho đúng tên cột mới
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE items;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO items (item_id, name, description, type, slot_type, tier, base_rarity, base_price, image_url, atk_bonus, def_bonus, hp_bonus)
VALUES
    (1, 'Gỗ Xoài', 'Vật phẩm hệ thống.', 'MATERIAL', 'MATERIAL', 1, 'COMMON', 10, 'r_wood.png', 0, 0, 0),
    (2, 'Đá', 'Vật phẩm hệ thống.', 'MATERIAL', 'MATERIAL', 1, 'COMMON', 10, 'stone_1.png', 0, 0, 0),
    (3, 'Quặng Đồng', 'Vật phẩm hệ thống.', 'MATERIAL', 'MATERIAL', 1, 'COMMON', 10, 'r_copper_node.png', 0, 0, 0),
    (13, 'Kiếm Gỗ', 'Vũ khí cơ bản.', 'WEAPON', 'WEAPON', 1, 'COMMON', 50, 's_sword_0.png', 5, 0, 0),
    (15, 'Bình Máu', 'Hồi phục 50 HP.', 'CONSUMABLE', 'CONSUMABLE', 1, 'COMMON', 20, 'r_potion.png', 0, 0, 0);

-- 2. Thêm dữ liệu Lời dẫn (Flavor Text) - BẮT BUỘC CÓ để không lỗi 500
INSERT INTO flavor_text (content) VALUES
                                      ('Bạn thấy một bầy chim sáo bay ngang qua bầu trời.'),
                                      ('Tiếng suối chảy róc rách khiến lòng đại hiệp thanh thản.'),
                                      ('Một ngọn gió nhẹ thổi qua, mang theo hương hoa cỏ dại.'),
                                      ('Bạn dừng chân nghỉ ngơi dưới bóng một cây đại thụ.');

-- 3. Thêm quái vật cơ bản
INSERT INTO enemies (name, level, hp, atk, def, speed, exp_reward, gold_reward, image_url) VALUES
                                                                                               ('Goblin', 1, 50, 10, 2, 8, 20, 5, 'monster_goblin.png'),
                                                                                               ('Skeleton', 3, 80, 15, 5, 10, 40, 10, 'monster_skeleton.png');