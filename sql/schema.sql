-- ======================================================================================
-- PHẦN 1: KHỞI TẠO DATABASE & SCHEMA (ĐÃ ĐỒNG BỘ 100% VỚI CODE JAVA)
-- ======================================================================================
DROP DATABASE IF EXISTS echommo_db;
CREATE DATABASE echommo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE echommo_db;

-- 1. USERS
CREATE TABLE users (
                       user_id              INT AUTO_INCREMENT PRIMARY KEY,
                       username             VARCHAR(50)  NOT NULL UNIQUE,
                       password_hash        VARCHAR(255) NOT NULL,
                       password             VARCHAR(255) NOT NULL DEFAULT '123456',
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
                       inventory_slots      INT                    DEFAULT 50,
                       INDEX idx_username (username),
                       INDEX idx_email (email)
);

-- 2. WALLET
CREATE TABLE wallet (
                        wallet_id        INT AUTO_INCREMENT PRIMARY KEY,
                        user_id          INT NOT NULL UNIQUE,
                        gold             DECIMAL(18, 2) DEFAULT 100.00,
                        diamonds         INT            DEFAULT 0,
                        echo_coin        DECIMAL(20, 4) DEFAULT 0,
                        wood             INT            DEFAULT 0,
                        dried_wood       INT            DEFAULT 0,
                        cold_wood        INT            DEFAULT 0,
                        strange_wood     INT            DEFAULT 0,
                        coal             INT            DEFAULT 0,
                        copper_ore       INT            DEFAULT 0,
                        iron_ore         INT            DEFAULT 0,
                        platinum         INT            DEFAULT 0,
                        fish             INT            DEFAULT 0,
                        shark            INT            DEFAULT 0,
                        unknown_material INT            DEFAULT 0,
                        updated_at       DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT FK_Wallet_User FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- 3. CHARACTERS (Khớp Entity Character.java)
CREATE TABLE characters (
                            char_id                    INT AUTO_INCREMENT PRIMARY KEY, -- Java: Integer charId
                            user_id                    INT         NOT NULL UNIQUE,
                            name                       VARCHAR(50) NOT NULL UNIQUE,
                            level                      INT          DEFAULT 1,
                            current_exp                BIGINT       DEFAULT 0,         -- Java: Long currentExp
                            character_class            VARCHAR(255) DEFAULT 'Nhà Thám Hiểm',
                            status                     VARCHAR(20)  DEFAULT 'IDLE',
                            current_location           VARCHAR(100) DEFAULT 'Làng Tân Thủ',
                            current_map_id             VARCHAR(50)  DEFAULT 'MAP_01', -- [Added] Khớp Java
                            total_power                INT          DEFAULT 0,
                            monster_kills              INT          DEFAULT 0,        -- [Added] Khớp Java
                            avatar_url                 VARCHAR(500) DEFAULT 'https://i.imgur.com/7Y7t5Xp.png', -- [Added] Khớp Java

    -- Stats
                            stat_points                INT          DEFAULT 0,
                            str                        INT          DEFAULT 5,
                            vit                        INT          DEFAULT 5,
                            agi                        INT          DEFAULT 5,
                            int_stat                   INT          DEFAULT 5, -- Java: intelligence (mapped by name strategy usually)
                            dex                        INT          DEFAULT 5,
                            luck                       INT          DEFAULT 5,
                            intelligence               INT          DEFAULT 5, -- Thêm cột này cho chắc chắn

                            current_hp                 INT          DEFAULT 200,
                            max_hp                     INT          DEFAULT 200,
                            current_energy             INT          DEFAULT 100,
                            max_energy                 INT          DEFAULT 100,
                            base_atk                   INT          DEFAULT 10,
                            base_def                   INT          DEFAULT 5,
                            base_speed                 INT          DEFAULT 10,
                            base_crit_rate             INT          DEFAULT 50,
                            base_crit_dmg              INT          DEFAULT 150,

    -- Spa
                            spa_start_time             DATETIME,
                            spa_end_time               DATETIME,
                            spa_package_type           VARCHAR(50),
                            daily_spa_usage            INT          DEFAULT 0,
                            last_spa_date              DATE         DEFAULT NULL,
                            last_free_spa_use          DATETIME     NULL,

    -- Gathering
                            gathering_level            INT          DEFAULT 1,
                            gathering_exp              BIGINT       DEFAULT 0,
                            gathering_item_id          INT          DEFAULT NULL,
                            gathering_remaining_amount INT          DEFAULT 0,
                            gathering_expiry           DATETIME     DEFAULT NULL,

    -- PvP
                            reputation                 INT          DEFAULT 0,
                            pvp_wins                   INT          DEFAULT 0,
                            pvp_matches_played         INT          DEFAULT 0,
                            win_streak                 INT          DEFAULT 0,
                            lose_streak                INT          DEFAULT 0,
                            rank_title                 VARCHAR(50)  DEFAULT 'Vô Danh',

                            version                    BIGINT       DEFAULT 0, -- Optimistic Locking
                            created_at                 DATETIME     DEFAULT CURRENT_TIMESTAMP,
                            last_active                DATETIME     DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT FK_Character_User FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- 4. ITEMS (Khớp Entity Item.java)
CREATE TABLE items (
                       item_id            INT AUTO_INCREMENT PRIMARY KEY,
                       code               VARCHAR(50)  NOT NULL UNIQUE,
                       name               VARCHAR(100) NOT NULL,
                       description        TEXT,
                       type               VARCHAR(20)  NOT NULL,
                       slot_type          ENUM ('NONE', 'WEAPON', 'ARMOR', 'HELMET', 'BOOTS', 'RING', 'NECKLACE', 'CONSUMABLE', 'MATERIAL', 'PICKAXE', 'AXE', 'SHOVEL', 'FISHING_ROD') DEFAULT 'NONE',
                       tier               INT DEFAULT 1,
                       base_rarity        VARCHAR(20) DEFAULT 'COMMON',
                       base_price         DECIMAL(18, 2) DEFAULT 10,
                       image_url          VARCHAR(255),
                       is_system_item     BOOLEAN DEFAULT FALSE,
                       is_limited         BOOLEAN DEFAULT FALSE, -- [Added] Khớp Java
                       is_tradable        BOOLEAN DEFAULT TRUE,

                       atk_bonus          INT DEFAULT 0,
                       def_bonus          INT DEFAULT 0,
                       hp_bonus           INT DEFAULT 0,
                       speed_bonus        INT DEFAULT 0,

    -- Tool Stats (Khớp Java)
                       max_durability     INT DEFAULT 100,
                       min_luck           INT DEFAULT 0,
                       max_luck           INT DEFAULT 0,
                       energy_save_chance DOUBLE DEFAULT 0.0,

                       base_stats         TEXT,
                       created_at         DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 5. USER ITEMS (Khớp Entity UserItem.java)
CREATE TABLE user_items (
                            user_item_id             BIGINT AUTO_INCREMENT PRIMARY KEY, -- Java: Long userItemId
                            char_id                  INT NOT NULL,
                            item_id                  INT NOT NULL,
                            quantity                 INT DEFAULT 1,
                            is_equipped              BOOLEAN DEFAULT FALSE,
                            is_locked                BOOLEAN DEFAULT FALSE,
                            rarity                   ENUM ('COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY', 'MYTHIC') DEFAULT 'COMMON',
                            enhance_level            INT DEFAULT 0,
                            is_mythic                BOOLEAN DEFAULT FALSE,
                            mythic_level             INT DEFAULT 0,
                            mythic_stars             INT DEFAULT 0,
                            main_stat_type           VARCHAR(20),
                            main_stat_value          DECIMAL(10, 2) DEFAULT 0,
                            original_main_stat_value DECIMAL(10, 2) DEFAULT 0,
                            sub_stats                TEXT,
                            visual_variant           INT DEFAULT 0,
                            current_durability       INT DEFAULT 100,
                            max_durability           INT DEFAULT 100,
                            acquired_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT FK_UserItem_Character FOREIGN KEY (char_id) REFERENCES characters (char_id) ON DELETE CASCADE,
                            CONSTRAINT FK_UserItem_Item FOREIGN KEY (item_id) REFERENCES items (item_id) ON DELETE CASCADE
);

-- 6. ENEMIES
CREATE TABLE enemies (
                         enemy_id    INT AUTO_INCREMENT PRIMARY KEY,
                         name        VARCHAR(100) NOT NULL,
                         level       INT DEFAULT 1,
                         hp          INT          NOT NULL,
                         atk         INT          NOT NULL,
                         def         INT          NOT NULL,
                         speed       INT DEFAULT 10,
                         exp_reward  INT DEFAULT 10,
                         gold_reward DECIMAL(10,2) DEFAULT 10.00,
                         image_url   VARCHAR(255),
                         drop_table  JSON
);

-- 7. MARKET LISTINGS (Khớp Entity MarketListing.java)
CREATE TABLE market_listings (
                                 listing_id    BIGINT AUTO_INCREMENT PRIMARY KEY, -- Java: Long listingId
                                 seller_id     INT            NOT NULL,
                                 user_item_id  BIGINT         NOT NULL, -- Java: UserItem (OneToOne)
                                 item_id       INT            NOT NULL,
                                 quantity      INT            DEFAULT 1,
                                 price         DECIMAL(18, 2) NOT NULL, -- Java: BigDecimal
                                 status        VARCHAR(20)    DEFAULT 'ACTIVE',
                                 enhance_level INT            DEFAULT 0,
                                 currency_type VARCHAR(20)    DEFAULT 'GOLD',
                                 created_at    DATETIME       DEFAULT CURRENT_TIMESTAMP,
                                 listed_at     DATETIME       DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT FK_Listing_Seller FOREIGN KEY (seller_id) REFERENCES users (user_id) ON DELETE CASCADE,
                                 CONSTRAINT FK_Listing_UserItem FOREIGN KEY (user_item_id) REFERENCES user_items (user_item_id) ON DELETE CASCADE,
                                 CONSTRAINT FK_Listing_Item FOREIGN KEY (item_id) REFERENCES items (item_id) ON DELETE CASCADE
);

-- 8. BATTLE SESSIONS (Đã fix ID thành INT cho khớp BattleSession.java)
CREATE TABLE battle_sessions (
                                 id                    INT AUTO_INCREMENT PRIMARY KEY, -- [FIXED] Java: Integer id
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
                                 status                VARCHAR(20) DEFAULT 'ONGOING',
                                 created_at            DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT FK_Battle_Character FOREIGN KEY (char_id) REFERENCES characters (char_id) ON DELETE CASCADE
);

-- 9. MESSAGES (Đã fix tên bảng từ chat_messages -> messages cho khớp Message.java)
CREATE TABLE messages (
                          message_id  BIGINT AUTO_INCREMENT PRIMARY KEY, -- Java: Long messageId
                          user_id     INT  NOT NULL,                     -- Java: User sender
                          content     TEXT NOT NULL,
                          sent_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT FK_Message_User FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- 10. PRIVATE MESSAGES (Khớp PrivateMessage.java)
CREATE TABLE private_messages (
                                  id          INT AUTO_INCREMENT PRIMARY KEY,
                                  sender_id   INT  NOT NULL,
                                  receiver_id INT  NOT NULL,
                                  content     TEXT NOT NULL,
                                  is_read     BOOLEAN  DEFAULT FALSE,
                                  sent_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  FOREIGN KEY (sender_id) REFERENCES users (user_id) ON DELETE CASCADE,
                                  FOREIGN KEY (receiver_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- CÁC BẢNG KHÁC (GIỮ NGUYÊN VÌ KHÔNG ẢNH HƯỞNG LỖI)
CREATE TABLE daily_quests (
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

CREATE TABLE friendships (
                             id           INT AUTO_INCREMENT PRIMARY KEY,
                             requester_id INT NOT NULL,
                             addressee_id INT NOT NULL,
                             status       VARCHAR(20) DEFAULT 'PENDING',
                             created_at   DATETIME    DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT UQ_Friendship UNIQUE (requester_id, addressee_id),
                             CONSTRAINT FK_Friend_Req FOREIGN KEY (requester_id) REFERENCES users (user_id) ON DELETE CASCADE,
                             CONSTRAINT FK_Friend_Addr FOREIGN KEY (addressee_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE notifications (
                               id         INT AUTO_INCREMENT PRIMARY KEY,
                               user_id    INT NOT NULL,
                               title      VARCHAR(100),
                               message    TEXT,
                               type       VARCHAR(20) DEFAULT 'INFO',
                               is_read    BOOLEAN     DEFAULT FALSE,
                               created_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT FK_Noti_User FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE announcements (
                               id         INT AUTO_INCREMENT PRIMARY KEY,
                               title      VARCHAR(255) NOT NULL,
                               content    TEXT         NOT NULL,
                               type       VARCHAR(50) DEFAULT 'UPDATE',
                               created_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
                               is_active  BOOLEAN     DEFAULT TRUE
);

CREATE TABLE flavor_text (
                             ft_id      INT AUTO_INCREMENT PRIMARY KEY,
                             content    VARCHAR(255) NOT NULL,
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE weather_text (
                              wt_id      INT AUTO_INCREMENT PRIMARY KEY,
                              content    VARCHAR(255) NOT NULL,
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE skills (
                        skill_id       INT AUTO_INCREMENT PRIMARY KEY,
                        name           VARCHAR(255),
                        description    VARCHAR(255),
                        type           VARCHAR(255),
                        mana_cost      INT,
                        power          INT,
                        required_level INT,
                        image_url      VARCHAR(255)
);

CREATE TABLE pvp_queue (
                           queue_id  INT AUTO_INCREMENT PRIMARY KEY,
                           char_id   INT NOT NULL UNIQUE,
                           level     INT NOT NULL,
                           power     INT         DEFAULT 0,
                           status    VARCHAR(20) DEFAULT 'SEARCHING',
                           joined_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT FK_Queue_Character FOREIGN KEY (char_id) REFERENCES characters (char_id) ON DELETE CASCADE
);

CREATE TABLE pvp_matches (
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

-- Giftcodes
CREATE TABLE gift_codes (
                            code            VARCHAR(50) PRIMARY KEY,
                            type            VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
                            gold_reward     BIGINT               DEFAULT 0,
                            coin_reward     BIGINT               DEFAULT 0,
                            max_usages      INT                  DEFAULT 1,
                            expiration_date DATETIME,
                            is_active       BOOLEAN              DEFAULT TRUE
);

CREATE TABLE gift_code_usage (
                                 id      BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id INT         NOT NULL,
                                 code    VARCHAR(50) NOT NULL,
                                 used_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (user_id) REFERENCES users (user_id),
                                 FOREIGN KEY (code) REFERENCES gift_codes (code)
);