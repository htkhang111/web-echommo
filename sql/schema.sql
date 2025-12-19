DROP DATABASE IF EXISTS echommo_db;
CREATE DATABASE echommo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE echommo_db;

-- ==========================================
-- 1. USERS & AUTHENTICATION
-- ==========================================
CREATE TABLE users
(
    user_id              INT AUTO_INCREMENT PRIMARY KEY,
    username             VARCHAR(50)  NOT NULL UNIQUE,
    password_hash        VARCHAR(255) NOT NULL,
    password             VARCHAR(255) NOT NULL  DEFAULT '123456', -- Lưu plain text để debug (production nên bỏ)
    email                VARCHAR(100) NOT NULL UNIQUE,
    full_name            VARCHAR(100),
    role                 ENUM ('USER', 'ADMIN') DEFAULT 'USER',

    -- Security & Status
    is_active            BOOLEAN                DEFAULT TRUE,
    ban_reason           VARCHAR(255),
    banned_at            DATETIME,
    is_captcha_locked    BOOLEAN                DEFAULT FALSE,
    captcha_fail_count   INT                    DEFAULT 0,
    captcha_locked_until DATETIME,
    otp_code             VARCHAR(10),
    otp_expiry           DATETIME,

    -- Info
    avatar_url           VARCHAR(255)           DEFAULT '🐲',
    last_login           DATETIME,
    created_at           DATETIME               DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- ==========================================
-- 2. ECONOMY (WALLET)
-- ==========================================
CREATE TABLE wallet
(
    wallet_id  INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL UNIQUE,
    gold       DECIMAL(18, 2) DEFAULT 100.00,
    diamonds   INT            DEFAULT 0,
    wood       INT            DEFAULT 0,
    stone      INT            DEFAULT 0,
    iron_ore   INT            DEFAULT 0,
    platinum   INT            DEFAULT 0,
    updated_at DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_Wallet_User FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- ==========================================
-- 3. CHARACTER SYSTEM
-- ==========================================
CREATE TABLE characters
(
    char_id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT         NOT NULL UNIQUE,
    name             VARCHAR(50) NOT NULL UNIQUE,

    -- Level & Class
    level            INT          DEFAULT 1,
    current_exp      BIGINT       DEFAULT 0,
    character_class  VARCHAR(255) DEFAULT 'Nhà Thám Hiểm',
    status           VARCHAR(20)  DEFAULT 'IDLE',
    current_location VARCHAR(100) DEFAULT 'Làng Tân Thủ',

    -- Stats cơ bản
    total_power      INT          DEFAULT 0,
    stat_points      INT          DEFAULT 0,
    str              INT          DEFAULT 5,
    vit              INT          DEFAULT 5,
    agi              INT          DEFAULT 5,
    int_stat         INT          DEFAULT 5,   -- Chỉ số mới: Trí tuệ

    -- Stats tính toán
    current_hp       INT          DEFAULT 100,
    max_hp           INT          DEFAULT 100,
    current_energy   INT          DEFAULT 50,
    max_energy       INT          DEFAULT 50,
    base_atk         INT          DEFAULT 10,
    base_def         INT          DEFAULT 5,
    base_speed       INT          DEFAULT 10,
    base_crit_rate   INT          DEFAULT 50,  -- 5.0%
    base_crit_dmg    INT          DEFAULT 150, -- 150%

    -- Spa System
    spa_start_time   DATETIME,
    spa_end_time     DATETIME,
    spa_package_type VARCHAR(50),

    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    last_active      DATETIME     DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT FK_Character_User FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- ==========================================
-- 4. ITEMS & INVENTORY
-- ==========================================
-- Bảng định nghĩa Item gốc
CREATE TABLE items
(
    item_id        INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    description    TEXT,
    type           VARCHAR(20)  NOT NULL, -- MATERIAL, WEAPON, ARMOR...
    slot_type      ENUM ('WEAPON', 'HELMET', 'ARMOR', 'BOOTS', 'RING', 'NECKLACE', 'NONE') DEFAULT 'NONE',
    tier           INT                                                                     DEFAULT 1,
    base_rarity    VARCHAR(20)                                                             DEFAULT 'COMMON',
    base_price     DECIMAL(18, 2)                                                          DEFAULT 10,
    image_url      VARCHAR(255),
    is_system_item BOOLEAN                                                                 DEFAULT FALSE,

    -- Base stats cho item
    atk_bonus      INT                                                                     DEFAULT 0,
    def_bonus      INT                                                                     DEFAULT 0,
    hp_bonus       INT                                                                     DEFAULT 0,
    speed_bonus    INT                                                                     DEFAULT 0,

    created_at     DATETIME                                                                DEFAULT CURRENT_TIMESTAMP
);

-- Bảng Inventory (Item của người chơi) - LIÊN KẾT VỚI CHARACTER (Updated)
CREATE TABLE user_items
(
    user_item_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    char_id                  INT NOT NULL, -- Đã đổi từ user_id sang char_id
    item_id                  INT NOT NULL,

    quantity                 INT                                                                DEFAULT 1,
    is_equipped              BOOLEAN                                                            DEFAULT FALSE,
    is_locked                BOOLEAN                                                            DEFAULT FALSE,

    -- Stats riêng của item này (Cường hóa/Random)
    rarity                   ENUM ('COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY', 'MYTHIC') DEFAULT 'COMMON',
    enhance_level            INT                                                                DEFAULT 0,

    -- Mythic System
    is_mythic                BOOLEAN                                                            DEFAULT FALSE,
    mythic_level             INT                                                                DEFAULT 0,

    -- Main Stat
    main_stat_type           VARCHAR(20),
    main_stat_value          DECIMAL(10, 2)                                                     DEFAULT 0,
    original_main_stat_value DECIMAL(10, 2)                                                     DEFAULT 0,

    -- Sub Stats (Lưu JSON)
    sub_stats                TEXT,         -- Dùng TEXT an toàn hơn JSON trên một số DB cũ

    acquired_at              DATETIME                                                           DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT FK_UserItem_Character FOREIGN KEY (char_id) REFERENCES characters (char_id) ON DELETE CASCADE,
    CONSTRAINT FK_UserItem_Item FOREIGN KEY (item_id) REFERENCES items (item_id) ON DELETE CASCADE,
    INDEX idx_char_equipped (char_id, is_equipped)
);

-- ==========================================
-- 5. GAMEPLAY (ENEMIES, BATTLE, QUESTS)
-- ==========================================
CREATE TABLE enemies
(
    enemy_id    INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    level       INT DEFAULT 1,
    hp          INT          NOT NULL,
    atk         INT          NOT NULL,
    def         INT          NOT NULL,
    speed       INT DEFAULT 10,
    exp_reward  INT          NOT NULL,
    gold_reward INT          NOT NULL,
    image_url   VARCHAR(255),
    drop_table  JSON -- Danh sách vật phẩm có thể rơi
);

CREATE TABLE battle_sessions
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    char_id               INT NOT NULL UNIQUE, -- Liên kết với Character

    -- Enemy Info Snapshot
    enemy_id              INT,
    enemy_name            VARCHAR(255),
    enemy_max_hp          INT,
    enemy_current_hp      INT,
    enemy_atk             INT,
    enemy_def             INT,
    enemy_speed           INT,

    -- Player Status Snapshot
    player_current_hp     INT,
    player_max_hp         INT,
    player_current_energy INT,

    -- Turn Info
    current_turn          INT      DEFAULT 0,
    is_qte_active         BOOLEAN  DEFAULT FALSE,
    qte_expiry_time       DATETIME,
    last_action_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
    spam_count            INT      DEFAULT 0,

    log                   TEXT,                -- Battle Log
    created_at            DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT FK_Battle_Character FOREIGN KEY (char_id) REFERENCES characters (char_id) ON DELETE CASCADE
);

CREATE TABLE daily_quests
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL,
    type         VARCHAR(50), -- KILL_MONSTER, GATHER...
    description  VARCHAR(255),
    target       INT,
    progress     INT     DEFAULT 0,
    reward_gold  INT,
    reward_exp   INT,
    is_claimed   BOOLEAN DEFAULT FALSE,
    created_date DATE,
    CONSTRAINT FK_Quest_User FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- ==========================================
-- 6. SOCIAL & MARKET
-- ==========================================
CREATE TABLE market_listings
(
    listing_id    INT AUTO_INCREMENT PRIMARY KEY,
    seller_id     INT            NOT NULL,
    user_item_id  BIGINT,
    item_id       INT            NOT NULL,

    quantity      INT         DEFAULT 1,
    price         DECIMAL(18, 2) NOT NULL,
    status        VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, SOLD, CANCELLED
    enhance_level INT         DEFAULT 0,        -- Cache lại để hiển thị nhanh

    created_at    DATETIME    DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT FK_Listing_Seller FOREIGN KEY (seller_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT FK_Listing_UserItem FOREIGN KEY (user_item_id) REFERENCES user_items (user_item_id) ON DELETE SET NULL,
    CONSTRAINT FK_Listing_Item FOREIGN KEY (item_id) REFERENCES items (item_id) ON DELETE CASCADE
);

CREATE TABLE friendships
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    requester_id INT NOT NULL,
    addressee_id INT NOT NULL,
    status       VARCHAR(20) DEFAULT 'PENDING',
    created_at   DATETIME    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT UQ_Friendship UNIQUE (requester_id, addressee_id),
    CONSTRAINT FK_Friend_Req FOREIGN KEY (requester_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT FK_Friend_Addr FOREIGN KEY (addressee_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- ==========================================
-- 7. COMMUNICATION (CHAT & NOTIFICATIONS)
-- ==========================================
CREATE TABLE chat_messages
(
    message_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT  NOT NULL,
    channel     VARCHAR(20) DEFAULT 'WORLD', -- WORLD, CLAN, PRIVATE
    receiver_id INT,                         -- Nếu là private
    content     TEXT NOT NULL,
    sent_at     DATETIME    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Chat_User FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE private_messages
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    sender_id   INT  NOT NULL,
    receiver_id INT  NOT NULL,
    content     TEXT NOT NULL,
    is_read     BOOLEAN  DEFAULT FALSE,
    sent_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE notifications
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    title      VARCHAR(100),
    message    TEXT,
    type       VARCHAR(20) DEFAULT 'INFO',
    is_read    BOOLEAN     DEFAULT FALSE,
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Noti_User FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE announcements
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL,
    type       VARCHAR(50) DEFAULT 'UPDATE',
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    is_active  BOOLEAN     DEFAULT TRUE
);

-- ==========================================
-- 8. FLAVOR TEXT (LORE)
-- ==========================================
CREATE TABLE flavor_text
(
    ft_id      INT AUTO_INCREMENT PRIMARY KEY,
    content    VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE weather_text
(
    wt_id      INT AUTO_INCREMENT PRIMARY KEY,
    content    VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

USE echommo_db;

ALTER TABLE items
    MODIFY COLUMN slot_type ENUM ('NONE', 'WEAPON', 'ARMOR', 'HELMET', 'BOOTS', 'RING', 'NECKLACE', 'CONSUMABLE', 'MATERIAL') DEFAULT 'NONE';

USE echommo_db;

ALTER TABLE characters
    ADD COLUMN gathering_item_id          INT      DEFAULT NULL,
    ADD COLUMN gathering_remaining_amount INT      DEFAULT 0,
    ADD COLUMN gathering_expiry           DATETIME DEFAULT NULL;


USE echommo_db;

-- =============================================
-- 1. SETUP TABLE ITEMS (RE-INDEXED IDs)
-- =============================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE echommo_db.items;
SET FOREIGN_KEY_CHECKS = 1;

-- --- GROUP 1: GỖ (WOODS) ---
-- ID chạy từ 1 -> 4
INSERT INTO echommo_db.items (item_id, name, type, rarity, tier, base_price, description, image_url, slot_type)
VALUES (1, 'Gỗ Sồi', 'MATERIAL', 'COMMON', 1, 10, 'Vật phẩm hệ thống.', 'r_wood.png', 'MATERIAL'),
       (2, 'Gỗ Khô', 'MATERIAL', 'COMMON', 1, 10, 'Vật phẩm hệ thống.', 'r_red_wood.png', 'MATERIAL'),
       (3, 'Gỗ Lạnh', 'MATERIAL', 'UNCOMMON', 2, 10, 'Vật phẩm hệ thống.', 'r_white_wood.png', 'MATERIAL'),
       (4, 'Gỗ Lạ', 'MATERIAL', 'RARE', 3, 10, 'Vật phẩm hệ thống.', 'r_black_wood.png', 'MATERIAL');

-- --- GROUP 2: KHOÁNG SẢN (MINERALS) ---
-- ID chạy từ 5 -> 8
INSERT INTO echommo_db.items (item_id, name, type, rarity, tier, base_price, description, image_url, slot_type)
VALUES (5, 'Đá', 'MATERIAL', 'COMMON', 1, 10, 'Vật phẩm hệ thống.', 'stone_1.png', 'MATERIAL'),
       (6, 'Quặng Đồng', 'MATERIAL', 'COMMON', 1, 10, 'Vật phẩm hệ thống.', 'r_copper_node.png', 'MATERIAL'),
       (7, 'Sắt', 'MATERIAL', 'RARE', 2, 10, 'Vật phẩm hệ thống.', 'r_silver_node.png', 'MATERIAL'),
       (8, 'Bạch Kim', 'MATERIAL', 'EPIC', 3, 10, 'Vật phẩm hệ thống.', 'r_mystrile_node.png', 'MATERIAL');

-- --- GROUP 3: THỰC PHẨM (FOOD/FISH) ---
-- ID chạy từ 9 -> 10
INSERT INTO echommo_db.items (item_id, name, type, rarity, tier, base_price, description, image_url, slot_type)
VALUES (9, 'Cá', 'MATERIAL', 'COMMON', 1, 10, 'Vật phẩm hệ thống.', 'r_fish.png', 'MATERIAL'),
       (10, 'Cá Mập', 'MATERIAL', 'UNCOMMON', 2, 10, 'Vật phẩm hệ thống.', 'r_shark.png', 'MATERIAL');

-- --- GROUP 4: ĐẶC BIỆT (SPECIAL) ---
-- ID chạy từ 11 -> 12
INSERT INTO echommo_db.items (item_id, name, type, rarity, tier, base_price, description, image_url, slot_type)
VALUES (11, 'Echo Coin', 'MATERIAL', 'LEGENDARY', 5, 10, 'Vật phẩm hệ thống.', 'r_echo_coin.png', 'MATERIAL'),
       (12, 'Nguyên liệu lạ', 'MATERIAL', 'EPIC', 4, 10, 'Vật phẩm hệ thống.', 'r_unknown.png', 'MATERIAL');

-- --- GROUP 5: TRANG BỊ & TIÊU HAO ---
-- ID chạy tiếp từ 13 -> 15
INSERT INTO echommo_db.items (item_id, name, type, rarity, tier, base_price, description, image_url, slot_type)
VALUES (13, 'Kiếm Gỗ', 'WEAPON', 'COMMON', 1, 10, 'Vật phẩm hệ thống.', 's_sword_0.png', 'WEAPON'),
       (14, 'Áo Vải', 'ARMOR', 'COMMON', 1, 10, 'Vật phẩm hệ thống.', 'a_armor_0.png', 'ARMOR'),
       (15, 'Bình Máu', 'CONSUMABLE', 'COMMON', 1, 10, 'Vật phẩm hệ thống.', 'r_potion.png', 'CONSUMABLE');


-- =============================================
-- 2. SETUP TABLE WALLET (CẤU TRÚC KHỚP ID)
-- =============================================
DROP TABLE IF EXISTS wallet;

CREATE TABLE wallet
(
    wallet_id        INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT NOT NULL UNIQUE,

    -- Currency
    gold             DECIMAL(18, 2) DEFAULT 100.00,
    diamonds         INT            DEFAULT 0,

    -- --- GROUP 1: GỖ (ID 1-4) ---
    wood             INT            DEFAULT 0, -- Gỗ Sồi (ID 1)
    dried_wood       INT            DEFAULT 0, -- Gỗ Khô (ID 2)
    cold_wood        INT            DEFAULT 0, -- Gỗ Lạnh (ID 3)
    strange_wood     INT            DEFAULT 0, -- Gỗ Lạ  (ID 4)

    -- --- GROUP 2: KHOÁNG SẢN (ID 5-8) ---
    stone            INT            DEFAULT 0, -- Đá (ID 5)
    copper_ore       INT            DEFAULT 0, -- Đồng (ID 6)
    iron_ore         INT            DEFAULT 0, -- Sắt (ID 7)
    platinum         INT            DEFAULT 0, -- Bạch Kim (ID 8)

    -- --- GROUP 3: THỰC PHẨM (ID 9-10) ---
    fish             INT            DEFAULT 0, -- Cá (ID 9)
    shark            INT            DEFAULT 0, -- Cá Mập (ID 10)

    -- --- GROUP 4: ĐẶC BIỆT (ID 11-12) ---
    echo_coin        INT            DEFAULT 0, -- Echo Coin (ID 11)
    unknown_material INT            DEFAULT 0, -- Nguyên liệu lạ (ID 12)

    updated_at       DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_Wallet_User FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- =============================================
-- 3. SEED DATA WALLET (ADMIN)
-- =============================================
INSERT INTO wallet (user_id, gold, diamonds,
                    wood, dried_wood, cold_wood, strange_wood,
                    stone, copper_ore, iron_ore, platinum,
                    fish, shark,
                    echo_coin, unknown_material)
VALUES (1, 999999, 99999,
        1000, 100, 100, 50, -- Gỗ
        1000, 500, 500, 100, -- Khoáng
        200, 10, -- Thực phẩm
        5, 1 -- Đặc biệt
       );

USE echommo_db;

-- Cập nhật cân bằng dựa trên Level của quái vật
UPDATE enemies
SET
    -- Công thức Gold: (Level * 10) + Random từ 5 đến 15
    gold_reward = (level * 10) + FLOOR(5 + (RAND() * 11)),

    -- Công thức EXP: (Level * 15) + Random từ 10 đến 20
    exp_reward  = (level * 15) + FLOOR(10 + (RAND() * 11))
WHERE level > 0;

-- Tinh chỉnh riêng cho các quái vật cụ thể để tạo đặc trưng (nếu muốn)
-- Ví dụ: Yêu Tinh Lv1 sẽ yếu nhưng nhiều vàng hơn chút
UPDATE enemies
SET gold_reward = 15,
    exp_reward  = 20
WHERE name = 'Yêu Tinh';
-- Nấm Độc Lv2
UPDATE enemies
SET gold_reward = 28,
    exp_reward  = 45
WHERE name = 'Nấm Độc';
-- Bộ Xương Lv3
UPDATE enemies
SET gold_reward = 45,
    exp_reward  = 75
WHERE name = 'Bộ Xương';

-- Kiểm tra lại kết quả
SELECT enemy_id, name, level, gold_reward, exp_reward
FROM enemies;

USE echommo_db;
TRUNCATE TABLE battle_sessions;
UPDATE characters
SET current_hp     = max_hp,
    current_energy = max_energy;

USE echommo_db;

-- MAP 1: ĐỒNG BẰNG (Lv 1-19)
INSERT INTO enemies (name, hp, atk, def, speed, exp_reward, gold_reward)
VALUES ('Slime Đồng Cỏ', 100, 15, 2, 5, 20, 10),
       ('Sói Non', 150, 25, 5, 12, 35, 15),
       ('Chuột Đồng', 80, 20, 0, 15, 15, 5);

-- MAP 2: RỪNG RẬM (Lv 20-30)
INSERT INTO enemies (name, hp, atk, def, speed, exp_reward, gold_reward)
VALUES ('Người Rừng', 300, 40, 10, 8, 60, 30),
       ('Gấu Hoang', 500, 50, 20, 5, 100, 50),
       ('Nhện Rừng', 250, 45, 5, 20, 55, 25);

-- MAP 3: SA MẠC (Lv 30-40)
INSERT INTO enemies (name, hp, atk, def, speed, exp_reward, gold_reward)
VALUES ('Bọ Cát', 400, 60, 30, 10, 120, 60),
       ('Xác Ướp Lang Thang', 600, 70, 25, 5, 150, 80),
       ('Bọ Hung Khổng Lồ', 800, 80, 50, 2, 200, 100);

-- MAP 4: NÚI CAO (Lv 40-50)
INSERT INTO enemies (name, hp, atk, def, speed, exp_reward, gold_reward)
VALUES ('Golem Đá', 1500, 100, 100, 1, 300, 150),
       ('Đại Bàng Núi', 700, 120, 20, 30, 250, 120),
       ('Người Lùn Đào Mỏ', 900, 90, 40, 10, 280, 140);

-- MAP 5: BĂNG ĐẢO (Lv 50-60)
INSERT INTO enemies (name, hp, atk, def, speed, exp_reward, gold_reward)
VALUES ('Gấu Băng', 2000, 150, 80, 5, 400, 200),
       ('Người Tuyết', 1800, 130, 60, 10, 380, 180),
       ('Tinh Linh Băng', 1200, 180, 30, 25, 450, 220);

-- MAP 6: ĐẦM LẦY (Lv 60-70)
INSERT INTO enemies (name, hp, atk, def, speed, exp_reward, gold_reward)
VALUES ('Quái Đầm Lầy', 3000, 200, 100, 5, 600, 300),
       ('Rắn Độc Khổng Lồ', 2500, 250, 50, 35, 700, 350),
       ('Linh Hồn Sa Lầy', 2000, 300, 10, 40, 800, 400);

USE echommo_db;

-- 1. TẠO TÀI KHOẢN ADMIN
INSERT INTO users (username, password_hash, password, email, full_name, role, avatar_url)
VALUES ('admin', '$2a$10$wPOKcn9CM0dlp.k83kEHne1UU90Y5.RL2MaLkqwJ0ZRnN3IbsRnnS', '123456', 'admin@echommo.com', 'Game Master',
        'ADMIN', '🐲');

-- 2. TẠO VÍ ADMIN
INSERT INTO wallet (user_id, gold, diamonds, wood, stone, iron_ore, platinum)
VALUES (1, 999999, 99999, 1000, 1000, 1000, 100);

-- 3. TẠO NHÂN VẬT ADMIN
INSERT INTO characters (user_id, name, level, str, vit, agi, int_stat, current_hp, max_hp, base_atk, base_def)
VALUES (1, 'ADMIN', 99, 999, 999, 999, 999, 99999, 99999, 9999, 9999);

-- 4. KHỞI TẠO DANH SÁCH QUÁI VẬT (ENEMIES)
INSERT INTO enemies (name, level, hp, atk, def, speed, exp_reward, gold_reward, image_url)
VALUES ('Yêu Tinh', 1, 50, 8, 2, 8, 15, 10, 'idle_goblin'),
       ('Nấm Độc', 2, 80, 12, 3, 5, 25, 15, 'idle_mushroom'),
       ('Bộ Xương', 3, 120, 18, 5, 9, 40, 25, 'idle_skeleton');

-- 5. KHỞI TẠO DANH SÁCH ITEM HỆ THỐNG
-- LƯU Ý: Insert ID cụ thể để khớp với GameConstants.java

-- A. NGUYÊN LIỆU (IDs 1 - 20)
INSERT INTO items (item_id, name, description, type, slot_type, tier, base_rarity, base_price, image_url)
VALUES (1, 'Gỗ Sồi', 'Gỗ thông thường dùng để chế tác sơ cấp', 'MATERIAL', 'MATERIAL', 1, 'COMMON', 5, 'r_wood.png'),
       (2, 'Gỗ Khô', 'Gỗ đã qua xử lý, cứng hơn gỗ thường', 'MATERIAL', 'MATERIAL', 1, 'COMMON', 15, 'r_red_wood.png'),
       (3, 'Gỗ Lạnh', 'Gỗ từ vùng băng giá, rất bền', 'MATERIAL', 'MATERIAL', 2, 'UNCOMMON', 50, 'r_white_wood.png'),
       (4, 'Gỗ Lạ', 'Gỗ có vân kỳ quái, ẩn chứa sức mạnh', 'MATERIAL', 'MATERIAL', 3, 'RARE', 200, 'r_black_wood.png'),
       (5, 'Đá', 'Đá vụn ven đường, dùng rèn vũ khí thô', 'MATERIAL', 'MATERIAL', 1, 'COMMON', 2, 'stone_1.png'),
       (6, 'Quặng Đồng', 'Quặng thô sơ, nguyên liệu cơ bản', 'MATERIAL', 'MATERIAL', 1, 'COMMON', 10,
        'r_copper_node.png'),
       (7, 'Sắt', 'Kim loại cứng cáp dùng cho trang bị trung cấp', 'MATERIAL', 'MATERIAL', 2, 'RARE', 50,
        'r_silver_bar.png'),
       (8, 'Bạch Kim', 'Kim loại quý hiếm, rất đắt tiền', 'MATERIAL', 'MATERIAL', 3, 'EPIC', 300, 'r_mystrile_bar.png'),
       (11, 'Echo Coin', 'Đồng xu cổ xưa mang năng lượng bí ẩn', 'MATERIAL', 'MATERIAL', 5, 'LEGENDARY', 1000,
        'r_echo_coin.png'),
       (12, 'Nguyên liệu lạ', 'Không ai biết nguồn gốc, dùng để đột phá', 'MATERIAL', 'MATERIAL', 4, 'EPIC', 500,
        'r_mystrile_node.png');

-- B. TRANG BỊ (IDs bắt đầu từ 21 trở đi)
INSERT INTO items (item_id, name, description, type, slot_type, tier, base_rarity, base_price, image_url)
VALUES (21, 'Kiếm Gỗ', 'Kiếm tập luyện cho người mới', 'WEAPON', 'WEAPON', 1, 'COMMON', 50, 's_sword_0'),
       (22, 'Kiếm Sắt', 'Sắc bén hơn kiếm gỗ', 'WEAPON', 'WEAPON', 1, 'RARE', 200, 's_sword_1'),
       (23, 'Kiếm Hiệp Sĩ', 'Kiếm tiêu chuẩn của hiệp sĩ', 'WEAPON', 'WEAPON', 2, 'RARE', 500, 's_sword_2'),
       (24, 'Áo Vải', 'Áo thô sơ che thân', 'ARMOR', 'ARMOR', 1, 'COMMON', 40, 'a_armor_0'),
       (25, 'Áo Da', 'Làm từ da thú cứng', 'ARMOR', 'ARMOR', 1, 'RARE', 150, 'a_armor_1'),
       (26, 'Mũ Da', 'Bảo vệ đầu cơ bản', 'ARMOR', 'HELMET', 1, 'COMMON', 45, 'h_helmet_0'),
       (27, 'Mũ Sắt', 'Mũ bảo vệ cứng cáp', 'ARMOR', 'HELMET', 1, 'RARE', 160, 'h_helmet_1'),
       (28, 'Giày Cỏ', 'Giúp đi lại đỡ đau chân', 'ARMOR', 'BOOTS', 1, 'COMMON', 30, 'b_boot_0'),
       (29, 'Giày Da', 'Di chuyển nhanh nhẹn hơn', 'ARMOR', 'BOOTS', 1, 'RARE', 120, 'b_boot_1'),
       (30, 'Nhẫn Đồng', 'Nhẫn trang sức đơn giản', 'ACCESSORY', 'RING', 1, 'COMMON', 100, 'ri_ring_0'),
       (31, 'Nhẫn Bạc', 'Tăng nhẹ sức mạnh phép thuật', 'ACCESSORY', 'RING', 1, 'RARE', 300, 'ri_ring_1'),
       (32, 'Vòng Cổ Đá', 'Vòng cổ làm từ đá thô', 'ACCESSORY', 'NECKLACE', 1, 'COMMON', 100, 'n_neck_0'),
       (33, 'Vòng Cổ Ngọc', 'Phát ra ánh sáng nhẹ', 'ACCESSORY', 'NECKLACE', 1, 'RARE', 350, 'n_neck_1'),
       (34, 'Bình Máu Nhỏ', 'Hồi 50 HP', 'CONSUMABLE', 'CONSUMABLE', 1, 'COMMON', 20,
        'https://cdn-icons-png.flaticon.com/512/863/863816.png');

-- 6. TẠO ITEM CHO ADMIN (Sử dụng char_id = 1)
-- Lưu ý: Cập nhật lại item_id cho đúng với danh sách mới ở trên (ID cũ + 20)
INSERT INTO user_items (char_id, item_id, quantity, is_equipped, rarity, enhance_level, is_mythic, mythic_level,
                        main_stat_type, main_stat_value, original_main_stat_value, sub_stats)
VALUES
-- Item 1: Kiếm Hiệp Sĩ Mythic (ID cũ là 3, nay là 23)
(1, 23, 1, TRUE, 'MYTHIC', 30, TRUE, 5, 'ATK_FLAT', 550, 500,
 '[{"code": "CRIT_RATE", "value": 15.0, "is_percent": true}, {"code": "CRIT_DMG", "value": 25.0, "is_percent": true}, {"code": "SPEED", "value": 12, "is_percent": false}, {"code": "HP_PERCENT", "value": 8.0, "is_percent": true}]'),
-- Item 2: Kiếm gỗ thường trong túi (ID cũ là 1, nay là 21)
(1, 21, 1, FALSE, 'COMMON', 0, FALSE, 0, 'ATK_FLAT', 10, 10, NULL);

SELECT 'SEED CORE COMPLETED' AS status;