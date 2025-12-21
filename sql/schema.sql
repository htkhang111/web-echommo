DROP DATABASE IF EXISTS echommo_db;
CREATE DATABASE echommo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE echommo_db;

-- ==========================================
-- 1. USERS & AUTH
-- ==========================================
CREATE TABLE users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL DEFAULT '123456', -- Dev only
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
                       avatar_url VARCHAR(255) DEFAULT 'idle_yasou.png',
                       last_login DATETIME,
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 2. WALLET (VÍ TIỀN & KHO)
-- ==========================================
CREATE TABLE wallet (
                        wallet_id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT NOT NULL UNIQUE,

    -- Tiền tệ
                        gold DECIMAL(18, 2) DEFAULT 100.00,
                        diamonds INT DEFAULT 0,
                        echo_coin DECIMAL(20, 4) DEFAULT 0, -- [UPDATE] Hỗ trợ số lẻ (VD: 0.0015)

    -- Nguyên liệu Gỗ
                        wood INT DEFAULT 0,          -- Gỗ Sồi
                        dried_wood INT DEFAULT 0,    -- Gỗ Khô
                        cold_wood INT DEFAULT 0,     -- Gỗ Lạnh
                        strange_wood INT DEFAULT 0,  -- Gỗ Lạ/Đen

    -- Nguyên liệu Khoáng
                        coal INT DEFAULT 0,          -- Than/Đá (stone cũ)
                        copper_ore INT DEFAULT 0,
                        iron_ore INT DEFAULT 0,
                        platinum INT DEFAULT 0,
                        unknown_material INT DEFAULT 0, -- Quặng Lạ

    -- Thực phẩm (Cá)
                        fish INT DEFAULT 0,
                        shark INT DEFAULT 0,

                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT FK_Wallet_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ==========================================
-- 3. CHARACTERS (NHÂN VẬT)
-- ==========================================
CREATE TABLE characters (
                            char_id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id INT NOT NULL UNIQUE,
                            name VARCHAR(50) NOT NULL UNIQUE,
                            level INT DEFAULT 1,
                            current_exp BIGINT DEFAULT 0,
                            character_class VARCHAR(255) DEFAULT 'Nhà Thám Hiểm',
                            status VARCHAR(20) DEFAULT 'IDLE',

    -- Stats Chính
                            stat_points INT DEFAULT 0,
                            str INT DEFAULT 5,
                            vit INT DEFAULT 5,
                            agi INT DEFAULT 5,
                            int_stat INT DEFAULT 5, -- Intelligence
                            luck INT DEFAULT 5,     -- [NEW] Luck

    -- Stats Chiến đấu
                            current_hp INT DEFAULT 100,
                            max_hp INT DEFAULT 100,
                            current_energy INT DEFAULT 50,
                            max_energy INT DEFAULT 50,
                            base_atk INT DEFAULT 10,
                            base_def INT DEFAULT 2,
                            base_speed INT DEFAULT 10,
                            base_crit_rate INT DEFAULT 50,  -- 5%
                            base_crit_dmg INT DEFAULT 1500, -- 150%

    -- Vị trí & Gathering
                            current_map_id VARCHAR(20) DEFAULT 'MAP_01',
                            current_location VARCHAR(100) DEFAULT 'Làng Tân Thủ',
                            gathering_item_id INT DEFAULT NULL,
                            gathering_remaining_amount INT DEFAULT 0,
                            gathering_expiry DATETIME DEFAULT NULL,

    -- PvP Info
                            pvp_points INT DEFAULT 1000,
                            pvp_wins INT DEFAULT 0,
                            pvp_matches_played INT DEFAULT 0,

    -- Spa Info
                            spa_start_time DATETIME,
                            spa_end_time DATETIME,
                            spa_package_type VARCHAR(50),

                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                            last_active DATETIME DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT FK_Character_User FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ==========================================
-- 4. ITEMS (ĐỊNH NGHĨA VẬT PHẨM)
-- ==========================================
CREATE TABLE items (
                       item_id INT AUTO_INCREMENT PRIMARY KEY,
                       code VARCHAR(50), -- Code định danh (w_wood, s_sword...)
                       name VARCHAR(100) NOT NULL,
                       description TEXT,
                       type VARCHAR(20) NOT NULL, -- WEAPON, ARMOR, MATERIAL...
                       slot_type ENUM('NONE', 'WEAPON', 'ARMOR', 'HELMET', 'BOOTS', 'RING', 'NECKLACE', 'CONSUMABLE', 'MATERIAL') DEFAULT 'NONE',
                       tier INT DEFAULT 1,
                       base_rarity VARCHAR(20) DEFAULT 'COMMON',
                       base_price DECIMAL(18, 2) DEFAULT 10,
                       image_url VARCHAR(255), -- Tên file ảnh (lowercase)

    -- Chỉ số cơ bản (cho trang bị)
                       base_stats TEXT, -- JSON range

                       is_tradable BOOLEAN DEFAULT TRUE,
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 5. USER ITEMS (TÚI ĐỒ)
-- ==========================================
CREATE TABLE user_items (
                            user_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            char_id INT NOT NULL,
                            item_id INT NOT NULL,
                            quantity INT DEFAULT 1,
                            is_equipped BOOLEAN DEFAULT FALSE,
                            is_locked BOOLEAN DEFAULT FALSE,

    -- Cường hóa & Chỉ số
                            enhance_level INT DEFAULT 0,
                            rarity ENUM('COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY', 'MYTHIC') DEFAULT 'COMMON',
                            main_stat_type VARCHAR(50),
                            main_stat_value DECIMAL(10, 2) DEFAULT 0,
                            sub_stats TEXT, -- JSON

    -- Mythic
                            is_mythic BOOLEAN DEFAULT FALSE,
                            mythic_level INT DEFAULT 0,
                            original_main_stat_value DECIMAL(10, 2) DEFAULT 0,

    -- [UPDATE] Ngoại hình Random (0-4)
                            visual_variant INT DEFAULT 0,

                            acquired_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT FK_UserItem_Character FOREIGN KEY (char_id) REFERENCES characters(char_id) ON DELETE CASCADE,
                            CONSTRAINT FK_UserItem_Item FOREIGN KEY (item_id) REFERENCES items(item_id) ON DELETE CASCADE
);

-- ==========================================
-- 6. ENEMIES (QUÁI VẬT)
-- ==========================================
CREATE TABLE enemies (
                         enemy_id INT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         level INT DEFAULT 1,
                         hp INT NOT NULL,
                         atk INT NOT NULL,
                         def INT NOT NULL,
                         speed INT DEFAULT 10,
                         exp_reward INT DEFAULT 10,
                         gold_reward INT DEFAULT 10,
                         image_url VARCHAR(255),
                         drop_table JSON -- Cấu hình drop riêng nếu cần
);

-- ==========================================
-- 7. CÁC BẢNG TÍNH NĂNG KHÁC
-- ==========================================

-- Battle
CREATE TABLE battle_sessions (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 char_id INT NOT NULL UNIQUE,
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
                                 log TEXT,
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT FK_Battle_Character FOREIGN KEY (char_id) REFERENCES characters(char_id) ON DELETE CASCADE
);

-- Quest
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

-- Market
CREATE TABLE market_listings (
                                 listing_id INT AUTO_INCREMENT PRIMARY KEY,
                                 seller_id INT NOT NULL,
                                 user_item_id BIGINT,
                                 item_id INT NOT NULL,
                                 quantity INT DEFAULT 1,
                                 price DECIMAL(18, 2) NOT NULL,
                                 status VARCHAR(20) DEFAULT 'ACTIVE',
                                 enhance_level INT DEFAULT 0,
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT FK_Listing_Seller FOREIGN KEY (seller_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                 CONSTRAINT FK_Listing_UserItem FOREIGN KEY (user_item_id) REFERENCES user_items(user_item_id) ON DELETE SET NULL,
                                 CONSTRAINT FK_Listing_Item FOREIGN KEY (item_id) REFERENCES items(item_id) ON DELETE CASCADE
);

-- Social
CREATE TABLE friendships (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             requester_id INT NOT NULL,
                             addressee_id INT NOT NULL,
                             status VARCHAR(20) DEFAULT 'PENDING',
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT UQ_Friendship UNIQUE (requester_id, addressee_id)
);

CREATE TABLE chat_messages (
                               message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id INT NOT NULL,
                               channel VARCHAR(20) DEFAULT 'WORLD',
                               receiver_id INT,
                               content TEXT NOT NULL,
                               sent_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE private_messages (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  sender_id INT NOT NULL,
                                  receiver_id INT NOT NULL,
                                  content TEXT NOT NULL,
                                  is_read BOOLEAN DEFAULT FALSE,
                                  sent_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               user_id INT NOT NULL,
                               title VARCHAR(100),
                               message TEXT,
                               type VARCHAR(20) DEFAULT 'INFO',
                               is_read BOOLEAN DEFAULT FALSE,
                               created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE announcements (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               title VARCHAR(255) NOT NULL,
                               content TEXT NOT NULL,
                               type VARCHAR(50) DEFAULT 'UPDATE',
                               is_active BOOLEAN DEFAULT TRUE,
                               created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Flavor / Utils
CREATE TABLE flavor_text (
                             ft_id INT AUTO_INCREMENT PRIMARY KEY,
                             content VARCHAR(255) NOT NULL,
                             condition_type VARCHAR(50),
                             weight INT DEFAULT 1,
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE weather_text (
                              wt_id INT AUTO_INCREMENT PRIMARY KEY,
                              content VARCHAR(255) NOT NULL,
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE skills (
                        skill_id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255),
                        description VARCHAR(255),
                        type VARCHAR(255),
                        mana_cost INT,
                        power INT,
                        required_level INT,
                        image_url VARCHAR(255)
);

-- PvP
CREATE TABLE pvp_queue (
                           queue_id INT AUTO_INCREMENT PRIMARY KEY,
                           char_id INT NOT NULL UNIQUE,
                           level INT NOT NULL,
                           power INT DEFAULT 0,
                           status VARCHAR(20) DEFAULT 'SEARCHING',
                           joined_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pvp_matches (
                             match_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             player1_id INT NOT NULL,
                             player2_id INT NOT NULL,
                             status VARCHAR(20) DEFAULT 'PENDING',
                             winner_id INT DEFAULT NULL,
                             turn_count INT DEFAULT 1,
                             p1_current_hp INT DEFAULT 0,
                             p2_current_hp INT DEFAULT 0,
                             last_log TEXT,
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);