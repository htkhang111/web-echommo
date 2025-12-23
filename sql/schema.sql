DROP DATABASE IF EXISTS echommo_db;
CREATE DATABASE echommo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE echommo_db;

-- ==========================================
-- 1. USERS
-- ==========================================
CREATE TABLE users
(
    user_id              INT AUTO_INCREMENT PRIMARY KEY,
    username             VARCHAR(50)  NOT NULL UNIQUE,
    password_hash        VARCHAR(255) NOT NULL,
    password             VARCHAR(255) NOT NULL  DEFAULT '123456',
    email                VARCHAR(100) NOT NULL UNIQUE,
    full_name            VARCHAR(100),
    role                 ENUM ('USER', 'ADMIN') DEFAULT 'USER',
    is_active            BOOLEAN                DEFAULT TRUE,
    ban_reason           VARCHAR(255),
    banned_at            DATETIME,
    is_captcha_locked    BOOLEAN                DEFAULT FALSE,
    captcha_fail_count   INT                    DEFAULT 0,
    captcha_locked_until DATETIME,
    otp_code             VARCHAR(10),
    otp_expiry           DATETIME,
    avatar_url           VARCHAR(255)           DEFAULT '🐲',
    last_login           DATETIME,
    created_at           DATETIME               DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- ==========================================
-- 2. WALLET
-- ==========================================
CREATE TABLE wallet
(
    wallet_id        INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT NOT NULL UNIQUE,
    gold             DECIMAL(18, 2) DEFAULT 100.00,
    diamonds         INT            DEFAULT 0,
    echo_coin        INT            DEFAULT 0,
    wood             INT            DEFAULT 0,
    dried_wood       INT            DEFAULT 0,
    cold_wood        INT            DEFAULT 0,
    strange_wood     INT            DEFAULT 0,
    coal             INT            DEFAULT 0, -- Đã đổi từ stone -> coal
    copper_ore       INT            DEFAULT 0,
    iron_ore         INT            DEFAULT 0,
    platinum         INT            DEFAULT 0,
    fish             INT            DEFAULT 0,
    shark            INT            DEFAULT 0,
    unknown_material INT            DEFAULT 0,
    updated_at       DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_Wallet_User FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- ==========================================
-- 3. CHARACTERS
-- ==========================================
CREATE TABLE characters
(
    char_id                    INT AUTO_INCREMENT PRIMARY KEY,
    user_id                    INT         NOT NULL UNIQUE,
    name                       VARCHAR(50) NOT NULL UNIQUE,
    level                      INT          DEFAULT 1,
    current_exp                BIGINT       DEFAULT 0,
    character_class            VARCHAR(255) DEFAULT 'Nhà Thám Hiểm',
    status                     VARCHAR(20)  DEFAULT 'IDLE',
    current_location           VARCHAR(100) DEFAULT 'Làng Tân Thủ',
    total_power                INT          DEFAULT 0,
    stat_points                INT          DEFAULT 0,
    str                        INT          DEFAULT 5,
    vit                        INT          DEFAULT 5,
    agi                        INT          DEFAULT 5,
    int_stat                   INT          DEFAULT 5,
    current_hp                 INT          DEFAULT 100,
    max_hp                     INT          DEFAULT 100,
    current_energy             INT          DEFAULT 50,
    max_energy                 INT          DEFAULT 50,
    base_atk                   INT          DEFAULT 10,
    base_def                   INT          DEFAULT 5,
    base_speed                 INT          DEFAULT 10,
    base_crit_rate             INT          DEFAULT 50,
    base_crit_dmg              INT          DEFAULT 150,
    spa_start_time             DATETIME,
    spa_end_time               DATETIME,
    spa_package_type           VARCHAR(50),
    gathering_item_id          INT          DEFAULT NULL,
    gathering_remaining_amount INT          DEFAULT 0,
    gathering_expiry           DATETIME     DEFAULT NULL,
    pvp_points                 INT          DEFAULT 1000,
    pvp_wins                   INT          DEFAULT 0,
    pvp_matches_played         INT          DEFAULT 0,
    created_at                 DATETIME     DEFAULT CURRENT_TIMESTAMP,
    last_active                DATETIME     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Character_User FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- ==========================================
-- 4. ITEMS (Đã thêm cột CODE)
-- ==========================================
CREATE TABLE items
(
    item_id        INT AUTO_INCREMENT PRIMARY KEY,
    code           VARCHAR(50)  NOT NULL UNIQUE, -- CỘT MỚI: Định danh item (w_wood, s_sword_0...)
    name           VARCHAR(100) NOT NULL,
    description    TEXT,
    type           VARCHAR(20)  NOT NULL,
    slot_type      ENUM ('NONE', 'WEAPON', 'ARMOR', 'HELMET', 'BOOTS', 'RING', 'NECKLACE', 'CONSUMABLE', 'MATERIAL') DEFAULT 'NONE',
    tier           INT                                                                                               DEFAULT 1,
    base_rarity    VARCHAR(20)                                                                                       DEFAULT 'COMMON',
    base_price     DECIMAL(18, 2)                                                                                    DEFAULT 10,
    image_url      VARCHAR(255),
    is_system_item BOOLEAN                                                                                           DEFAULT FALSE,
    atk_bonus      INT                                                                                               DEFAULT 0,
    def_bonus      INT                                                                                               DEFAULT 0,
    hp_bonus       INT                                                                                               DEFAULT 0,
    speed_bonus    INT                                                                                               DEFAULT 0,
    created_at     DATETIME                                                                                          DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 5. USER ITEMS
-- ==========================================
CREATE TABLE user_items
(
    user_item_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    char_id                  INT NOT NULL,
    item_id                  INT NOT NULL,
    quantity                 INT                                                                DEFAULT 1,
    is_equipped              BOOLEAN                                                            DEFAULT FALSE,
    is_locked                BOOLEAN                                                            DEFAULT FALSE,
    rarity                   ENUM ('COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY', 'MYTHIC') DEFAULT 'COMMON',
    enhance_level            INT                                                                DEFAULT 0,
    is_mythic                BOOLEAN                                                            DEFAULT FALSE,
    mythic_level             INT                                                                DEFAULT 0,
    main_stat_type           VARCHAR(20),
    main_stat_value          DECIMAL(10, 2)                                                     DEFAULT 0,
    original_main_stat_value DECIMAL(10, 2)                                                     DEFAULT 0,
    sub_stats                TEXT,
    acquired_at              DATETIME                                                           DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_UserItem_Character FOREIGN KEY (char_id) REFERENCES characters (char_id) ON DELETE CASCADE,
    CONSTRAINT FK_UserItem_Item FOREIGN KEY (item_id) REFERENCES items (item_id) ON DELETE CASCADE,
    INDEX idx_char_equipped (char_id, is_equipped)
);

-- ==========================================
-- 6. ENEMIES
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
    exp_reward  INT DEFAULT 10,
    gold_reward INT DEFAULT 10,
    image_url   VARCHAR(255),
    drop_table  JSON
);

-- ==========================================
-- 7. GAMEPLAY & OTHERS
-- ==========================================
CREATE TABLE battle_sessions
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    char_id               INT NOT NULL UNIQUE,
    enemy_id              INT,
    enemy_name            VARCHAR(255),
    enemy_max_hp          INT,
    enemy_current_hp      INT,
    enemy_atk             INT,
    enemy_def             INT,
    enemy_speed           INT,
    player_current_hp     INT,
    player_max_hp         INT,
    player_current_energy INT,
    current_turn          INT      DEFAULT 0,
    is_qte_active         BOOLEAN  DEFAULT FALSE,
    qte_expiry_time       DATETIME,
    last_action_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
    spam_count            INT      DEFAULT 0,
    log                   TEXT,
    created_at            DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Battle_Character FOREIGN KEY (char_id) REFERENCES characters (char_id) ON DELETE CASCADE
);

CREATE TABLE daily_quests
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL,
    type         VARCHAR(50),
    description  VARCHAR(255),
    target       INT,
    progress     INT     DEFAULT 0,
    reward_gold  INT,
    reward_exp   INT,
    is_claimed   BOOLEAN DEFAULT FALSE,
    created_date DATE,
    CONSTRAINT FK_Quest_User FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE market_listings
(
    listing_id    INT AUTO_INCREMENT PRIMARY KEY,
    seller_id     INT            NOT NULL,
    user_item_id  BIGINT,
    item_id       INT            NOT NULL,
    quantity      INT         DEFAULT 1,
    price         DECIMAL(18, 2) NOT NULL,
    status        VARCHAR(20) DEFAULT 'ACTIVE',
    enhance_level INT         DEFAULT 0,
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

CREATE TABLE chat_messages
(
    message_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT  NOT NULL,
    channel     VARCHAR(20) DEFAULT 'WORLD',
    receiver_id INT,
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
CREATE TABLE skills
(
    skill_id       INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(255),
    description    VARCHAR(255),
    type           VARCHAR(255),
    mana_cost      INT,
    power          INT,
    required_level INT,
    image_url      VARCHAR(255)
);

CREATE TABLE pvp_queue
(
    queue_id  INT AUTO_INCREMENT PRIMARY KEY,
    char_id   INT NOT NULL UNIQUE,
    level     INT NOT NULL,
    power     INT         DEFAULT 0,
    status    VARCHAR(20) DEFAULT 'SEARCHING',
    joined_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Queue_Character FOREIGN KEY (char_id) REFERENCES characters (char_id) ON DELETE CASCADE
);

CREATE TABLE pvp_matches
(
    match_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    player1_id    INT NOT NULL,
    player2_id    INT NOT NULL,
    status        VARCHAR(20) DEFAULT 'PENDING',
    winner_id     INT         DEFAULT NULL,
    p1_accepted   BOOLEAN     DEFAULT FALSE,
    p2_accepted   BOOLEAN     DEFAULT FALSE,
    created_at    DATETIME    DEFAULT CURRENT_TIMESTAMP,
    turn_count    INT         DEFAULT 1,
    rps_p1_move   VARCHAR(10) DEFAULT NULL,
    rps_p2_move   VARCHAR(10) DEFAULT NULL,
    p1_current_hp INT         DEFAULT 0,
    p2_current_hp INT         DEFAULT 0,
    last_log      TEXT,
    updated_at    DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_Match_P1 FOREIGN KEY (player1_id) REFERENCES characters (char_id) ON DELETE CASCADE,
    CONSTRAINT FK_Match_P2 FOREIGN KEY (player2_id) REFERENCES characters (char_id) ON DELETE CASCADE
);


Use echommo_db;
-- 1. Bảng Users
CREATE TABLE IF NOT EXISTS users
(
    user_id     SERIAL PRIMARY KEY,
    username    VARCHAR(50) UNIQUE NOT NULL,
    password    VARCHAR(255)       NOT NULL,
    email       VARCHAR(100),
    role        VARCHAR(20) DEFAULT 'USER',
    is_locked   BOOLEAN     DEFAULT FALSE,
    lock_reason TEXT,
    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng Characters
CREATE TABLE IF NOT EXISTS characters
(
    char_id                    SERIAL PRIMARY KEY,
    user_id                    INT UNIQUE         NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    name                       VARCHAR(50) UNIQUE NOT NULL,
    level                      INT          DEFAULT 1,
    current_exp                BIGINT       DEFAULT 0,
    character_class            VARCHAR(50)  DEFAULT 'Nhà Thám Hiểm',
    status                     VARCHAR(20)  DEFAULT 'IDLE',

    -- Stats
    stat_points                INT          DEFAULT 0,
    str                        INT          DEFAULT 5,
    vit                        INT          DEFAULT 5,
    agi                        INT          DEFAULT 5,
    dex                        INT          DEFAULT 5,
    intelligence               INT          DEFAULT 5,
    luck                       INT          DEFAULT 5,

    current_hp                 INT          DEFAULT 100,
    max_hp                     INT          DEFAULT 100,
    current_energy             INT          DEFAULT 50,
    max_energy                 INT          DEFAULT 50,
    base_atk                   INT          DEFAULT 10,
    base_def                   INT          DEFAULT 2,
    base_speed                 INT          DEFAULT 10,
    base_crit_rate             INT          DEFAULT 50,
    base_crit_dmg              INT          DEFAULT 1500,

    current_map_id             VARCHAR(20)  DEFAULT 'MAP_01',
    current_location           VARCHAR(100) DEFAULT 'Làng Tân Thủ',
    monster_kills              INT          DEFAULT 0,

    -- Gathering
    gathering_item_id          INT,
    gathering_remaining_amount INT          DEFAULT 0,
    gathering_expiry           TIMESTAMP,

    -- Spa
    spa_start_time             TIMESTAMP,
    spa_end_time               TIMESTAMP,
    spa_package_type           VARCHAR(20),

    created_at                 TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    last_active                TIMESTAMP
);

-- 3. Bảng Wallet (Cập nhật Echo Coin Decimal)
CREATE TABLE IF NOT EXISTS wallets
(
    wallet_id        SERIAL PRIMARY KEY,
    user_id          INT UNIQUE NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    gold             DECIMAL(20, 2) DEFAULT 0,

    -- [NEW] Echo Coin hỗ trợ 4 số lẻ (ví dụ: 0.0015)
    echo_coin        DECIMAL(20, 4) DEFAULT 0,

    -- Storage
    wood             INT            DEFAULT 0,
    dried_wood       INT            DEFAULT 0,
    cold_wood        INT            DEFAULT 0,
    strange_wood     INT            DEFAULT 0, -- Gỗ Lạ/Đen

    stone            INT            DEFAULT 0, -- Than/Đá
    copper_ore       INT            DEFAULT 0,
    iron_ore         INT            DEFAULT 0,
    platinum         INT            DEFAULT 0,

    fish             INT            DEFAULT 0,
    shark            INT            DEFAULT 0,

    unknown_material INT            DEFAULT 0
);

-- 4. Bảng Items (Định nghĩa vật phẩm)
CREATE TABLE IF NOT EXISTS items
(
    item_id     SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(20)  NOT NULL, -- WEAPON, ARMOR, MATERIAL, CONSUMABLE
    rarity      VARCHAR(20) DEFAULT 'COMMON',
    description TEXT,
    image_path  VARCHAR(255),          -- Đường dẫn ảnh gốc
    slot_type   VARCHAR(20),
    tier        INT         DEFAULT 1,
    base_stats  TEXT,                  -- JSON base stats range
    is_tradable BOOLEAN     DEFAULT TRUE
);

-- 5. Bảng User Items (Túi đồ)
CREATE TABLE IF NOT EXISTS user_items
(
    user_item_id             SERIAL PRIMARY KEY,
    char_id                  INT NOT NULL REFERENCES characters (char_id) ON DELETE CASCADE,
    item_id                  INT NOT NULL REFERENCES items (item_id),

    is_equipped              BOOLEAN   DEFAULT FALSE,
    quantity                 INT       DEFAULT 1,
    acquired_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Cường hóa & Chỉ số riêng
    enhance_level            INT       DEFAULT 0,
    rarity                   VARCHAR(20),
    main_stat_type           VARCHAR(50),
    main_stat_value          DECIMAL(10, 2),
    sub_stats                TEXT, -- JSON array

    is_mythic                BOOLEAN   DEFAULT FALSE,
    mythic_level             INT       DEFAULT 0,
    original_main_stat_value DECIMAL(10, 2),

    -- [NEW] Biến thể hình ảnh (0-4)
    visual_variant           INT       DEFAULT 0
);

-- Các bảng phụ khác (giữ nguyên)
CREATE TABLE IF NOT EXISTS enemies
(
    enemy_id    SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    level       INT            DEFAULT 1,
    hp          INT            DEFAULT 100,
    atk         INT            DEFAULT 10,
    def         INT            DEFAULT 5,
    speed       INT            DEFAULT 10,
    exp_reward  INT            DEFAULT 10,
    gold_reward DECIMAL(10, 2) DEFAULT 0,
    image_path  VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS battle_sessions
(
    session_id            SERIAL PRIMARY KEY,
    char_id               INT UNIQUE NOT NULL REFERENCES characters (char_id) ON DELETE CASCADE,
    enemy_id              INT,
    enemy_name            VARCHAR(100),
    enemy_max_hp          INT,
    enemy_current_hp      INT,
    enemy_atk             INT,
    enemy_def             INT,
    enemy_speed           INT,
    player_max_hp         INT,
    player_current_hp     INT,
    player_current_energy INT,
    current_turn          INT         DEFAULT 0,
    log                   TEXT,
    status                VARCHAR(20) DEFAULT 'ONGOING',
    is_qte_active         BOOLEAN     DEFAULT FALSE,
    qte_expiry_time       TIMESTAMP,
    created_at            TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS market_listings
(
    listing_id   SERIAL PRIMARY KEY,
    seller_id    INT            NOT NULL REFERENCES users (user_id),
    user_item_id INT            NOT NULL REFERENCES user_items (user_item_id), -- Item cụ thể đang bán
    price        DECIMAL(20, 2) NOT NULL,
    quantity     INT         DEFAULT 1,
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    status       VARCHAR(20) DEFAULT 'ACTIVE'
);


-- 1. Bảng Users: Thêm giới hạn kho đồ
ALTER TABLE users
    ADD COLUMN inventory_slots INT DEFAULT 50;

-- 2. Bảng Characters: Thêm chỉ số Nghề & Spa
ALTER TABLE characters
    ADD COLUMN gathering_level INT DEFAULT 1;
ALTER TABLE characters
    ADD COLUMN gathering_exp BIGINT DEFAULT 0;
ALTER TABLE characters
    ADD COLUMN last_free_spa_use DATETIME NULL;
-- Theo dõi lượt Spa free

-- 3. Bảng UserItems: Thêm Sao Mythic & Độ bền
ALTER TABLE user_items
    ADD COLUMN mythic_stars INT DEFAULT 0;
ALTER TABLE user_items
    ADD COLUMN current_durability INT DEFAULT 100;
ALTER TABLE user_items
    ADD COLUMN max_durability INT DEFAULT 100;

-- 4. Bảng Items: Thêm loại công cụ & cấp độ yêu cầu (nếu chưa có)
-- (Nếu bạn dùng cột 'type' và 'tier' có sẵn thì không cần thêm, chỉ cần data seed chuẩn)

-- Cập nhật ENUM cho bảng items để chấp nhận các Tool mới
ALTER TABLE items
    MODIFY COLUMN slot_type ENUM (
        'NONE',
        'WEAPON',
        'ARMOR',
        'HELMET',
        'BOOTS',
        'RING',
        'NECKLACE',
        'CONSUMABLE',
        'MATERIAL',
        'PICKAXE',
        'AXE',
        'SHOVEL',
        'FISHING_ROD'
        );

SET SQL_SAFE_UPDATES = 0;

-- Thêm các cột chỉ số mới
ALTER TABLE items
    ADD COLUMN max_durability     INT    DEFAULT 100 COMMENT 'Độ bền tối đa',
    ADD COLUMN min_luck           INT    DEFAULT 0 COMMENT 'May mắn tối thiểu (Random)',
    ADD COLUMN max_luck           INT    DEFAULT 0 COMMENT 'May mắn tối đa (Random)',
    ADD COLUMN energy_save_chance DOUBLE DEFAULT 0.0 COMMENT 'Tỷ lệ không tốn Energy (0.01 = 1%)';

SET SQL_SAFE_UPDATES = 1;

ALTER TABLE items
    ADD COLUMN is_limited BOOLEAN DEFAULT FALSE;

-- Thêm cột theo dõi số lần dùng Spa thường trong ngày
ALTER TABLE characters
    ADD COLUMN daily_spa_usage INT DEFAULT 0;

-- Thêm cột lưu ngày cuối cùng sử dụng Spa (để reset daily)
ALTER TABLE characters
    ADD COLUMN last_spa_date DATE DEFAULT NULL;

-- 1. Bảng lưu Giftcode
CREATE TABLE IF NOT EXISTS gift_codes
(
    code            VARCHAR(50) PRIMARY KEY,
    type            VARCHAR(20) NOT NULL DEFAULT 'NORMAL', -- 'NORMAL' hoặc 'DEV_TOOL'
    gold_reward     BIGINT               DEFAULT 0,
    coin_reward     BIGINT               DEFAULT 0,
    max_usages      INT                  DEFAULT 1,        -- Số lần nhập tối đa của mã
    expiration_date DATETIME,
    is_active       BOOLEAN              DEFAULT TRUE
);

-- 2. Bảng lưu lịch sử nhập (để chặn 1 người nhập 2 lần)
CREATE TABLE IF NOT EXISTS gift_code_usage
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT         NOT NULL,
    code    VARCHAR(50) NOT NULL,
    used_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (code) REFERENCES gift_codes (code)
);

USE echommo_db;

-- 1. Thêm cột version (Bắt buộc để sửa lỗi Optimistic Locking trong code Java mới)
ALTER TABLE characters ADD COLUMN version BIGINT DEFAULT 0;

-- 2. Thêm các cột cho hệ thống Danh Vọng (Reputation)
ALTER TABLE characters ADD COLUMN win_streak INT DEFAULT 0;
ALTER TABLE characters ADD COLUMN lose_streak INT DEFAULT 0;
ALTER TABLE characters ADD COLUMN rank_title VARCHAR(50) DEFAULT 'Vô Danh';

-- 3. Xử lý điểm PvP cũ -> Thành Danh Vọng
-- Lệnh này đổi tên cột 'pvp_points' thành 'reputation' để giữ lại điểm cũ
-- Nếu bảng cũ ông chưa có cột pvp_points thì đổi lệnh này thành: ADD COLUMN reputation INT DEFAULT 0;
ALTER TABLE characters CHANGE COLUMN pvp_points reputation INT DEFAULT 0;

-- 4. Thêm Index tìm kiếm nhanh (cho mượt game)
CREATE INDEX idx_char_reputation ON characters(reputation);

-- 1. Thêm cột độ bền (nếu chưa có)
ALTER TABLE user_items ADD COLUMN IF NOT EXISTS current_durability INT DEFAULT 100;
ALTER TABLE user_items ADD COLUMN IF NOT EXISTS max_durability INT DEFAULT 100;

-- 2. Xóa các vật phẩm "ma" (Ghost Items) - tham chiếu đến item ID không còn tồn tại
DELETE FROM user_items WHERE item_id NOT IN (SELECT item_id FROM items);

-- 3. Đảm bảo dữ liệu cũ không bị NULL
UPDATE user_items SET current_durability = 100 WHERE current_durability IS NULL;
UPDATE user_items SET max_durability = 100 WHERE max_durability IS NULL;

ALTER TABLE enemies ADD COLUMN drop_table JSON;

UPDATE user_items SET is_locked = 0 WHERE is_locked IS NULL;