DROP DATABASE IF EXISTS echommo_db;
CREATE DATABASE echommo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE echommo_db;

-- USERS
CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) UNIQUE,
                       password_hash VARCHAR(255),
                       email VARCHAR(100),
                       full_name VARCHAR(100),
                       role VARCHAR(20),
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- WALLET
CREATE TABLE wallet (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT,
                        gold BIGINT DEFAULT 0,
                        diamonds BIGINT DEFAULT 0
);

-- CHARACTER
CREATE TABLE characters (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id INT,
                            name VARCHAR(50),
                            level INT,
                            str INT,
                            vit INT,
                            agi INT,
                            int_stat INT,
                            current_hp INT,
                            max_hp INT,
                            base_atk INT,
                            base_def INT
);

-- ITEMS
CREATE TABLE items (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100),
                       description VARCHAR(255),
                       type VARCHAR(30),
                       slot_type VARCHAR(30),
                       base_rarity VARCHAR(30),
                       base_price INT,
                       image_url VARCHAR(100)
);

-- USER ITEMS
CREATE TABLE user_items (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            char_id INT,
                            item_id INT,
                            is_equipped BOOLEAN,
                            rarity VARCHAR(30),
                            enhance_level INT,
                            is_mythic BOOLEAN,
                            mythic_level INT,
                            main_stat_type VARCHAR(30),
                            main_stat_value INT,
                            original_main_stat_value INT,
                            sub_stats JSON
);

-- FLAVOR
CREATE TABLE flavor_text (
                             ft_id INT AUTO_INCREMENT PRIMARY KEY,
                             content VARCHAR(255) NOT NULL
);

-- WEATHER
CREATE TABLE weather_text (
                              wt_id INT AUTO_INCREMENT PRIMARY KEY,
                              content VARCHAR(255) NOT NULL
);
