SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE user_items;
TRUNCATE TABLE market_listings;
TRUNCATE TABLE daily_quests;
TRUNCATE TABLE battle_sessions;
TRUNCATE TABLE characters;
TRUNCATE TABLE wallet;
TRUNCATE TABLE users;
TRUNCATE TABLE items;
TRUNCATE TABLE enemies;
TRUNCATE TABLE flavor_text;
SET FOREIGN_KEY_CHECKS = 1;

-- =======================================================
-- 1. NGUYÊN LIỆU (MATERIALS)
-- =======================================================

-- GỖ
INSERT INTO items (item_id, code, name, type, base_rarity, tier, base_price, description, image_url, slot_type)
VALUES (1, 'w_wood', 'Gỗ Sồi', 'MATERIAL', 'COMMON', 1, 10, 'Gỗ thông thường.', 'w_wood.png', 'MATERIAL'),
       (2, 'w_woodred', 'Gỗ Khô', 'MATERIAL', 'COMMON', 1, 15, 'Gỗ đã qua xử lý.', 'w_woodred.png', 'MATERIAL'),
       (3, 'w_woodwhite', 'Gỗ Lạnh', 'MATERIAL', 'UNCOMMON', 2, 50, 'Gỗ từ vùng băng giá.', 'w_woodwhite.png',
        'MATERIAL'),
       (4, 'w_woodblack', 'Gỗ Hắc Ám', 'MATERIAL', 'MYTHIC', 5, 500, 'Gỗ đen tuyền.', 'w_woodblack.png', 'MATERIAL');

-- KHOÁNG SẢN
INSERT INTO items (item_id, code, name, type, base_rarity, tier, base_price, description, image_url, slot_type)
VALUES (5, 'o_coal', 'Than Đá', 'MATERIAL', 'COMMON', 1, 5, 'Nhiên liệu.', 'o_coal.png', 'MATERIAL'),
       (6, 'o_copper', 'Quặng Đồng', 'MATERIAL', 'COMMON', 1, 10, 'Đồng thô.', 'o_copper.png', 'MATERIAL'),
       (7, 'o_iron', 'Quặng Sắt', 'MATERIAL', 'RARE', 2, 50, 'Sắt cứng.', 'o_iron.png', 'MATERIAL'),
       (8, 'o_platinum', 'Bạch Kim', 'MATERIAL', 'EPIC', 3, 300, 'Kim loại quý.', 'o_platinum.png', 'MATERIAL'),
       (12, 'o_strange', 'Quặng Lạ', 'MATERIAL', 'MYTHIC', 5, 1000, 'Phát sáng kỳ bí.', 'o_strange.png', 'MATERIAL'),
       (14, 'o_gold', 'Quặng Vàng', 'MATERIAL', 'LEGENDARY', 1, 0, 'Quy đổi ra Gold.', 'o_gold.png', 'MATERIAL');

-- CÁ (DÙNG CHO HỒ ƯỚC NGUYỆN)
INSERT INTO items (item_id, code, name, type, base_rarity, tier, base_price, description, image_url, slot_type)
VALUES (9, 'f_fish', 'Cá Thường', 'MATERIAL', 'COMMON', 1, 10, 'Thả hồ cầu may.', 'f_fish.png', 'MATERIAL'),
       (10, 'f_shark', 'Cá Mập', 'MATERIAL', 'UNCOMMON', 2, 100, 'Hung dữ.', 'f_shark.png', 'MATERIAL'),
       (13, 'f_whiteshark', 'Cá Mập Trắng', 'MATERIAL', 'RARE', 3, 500, 'Hiếm gặp.', 'f_whiteshark.png', 'MATERIAL'),
       (140, 'f_megalodon', 'Megalodon', 'MATERIAL', 'MYTHIC', 5, 2000, 'Quái vật tiền sử.', 'f_megalodon.png',
        'MATERIAL');
-- ID 140 để tránh trùng quặng

-- SPECIAL
INSERT INTO items (item_id, code, name, type, base_rarity, tier, base_price, description, image_url, slot_type)
VALUES (11, 'r_coinecho', 'Echo Coin', 'MATERIAL', 'LEGENDARY', 5, 1000, 'Tiền tệ cổ xưa.', 'r_coinecho.png',
        'MATERIAL');

-- =======================================================
-- 2. TRANG BỊ (EQUIPMENT) - ID 20+
-- =======================================================

-- VŨ KHÍ
INSERT INTO items (item_id, code, name, description, type, slot_type, tier, base_rarity, base_price, image_url,
                   base_stats)
VALUES (20, 's_sword', 'Kiếm Gỗ', 'Tập sự.', 'WEAPON', 'WEAPON', 1, 'COMMON', 50, 's_sword', '{"ATK_FLAT": [5, 10]}'),
       (21, 's_sword', 'Kiếm Sắt', 'Sắc bén.', 'WEAPON', 'WEAPON', 2, 'UNCOMMON', 200, 's_sword',
        '{"ATK_FLAT": [15, 25]}'),
       (22, 's_sword', 'Kiếm Tinh Anh', 'Hoàng gia.', 'WEAPON', 'WEAPON', 3, 'RARE', 500, 's_sword',
        '{"ATK_FLAT": [30, 50]}');

-- GIÁP
INSERT INTO items (item_id, code, name, description, type, slot_type, tier, base_rarity, base_price, image_url,
                   base_stats)
VALUES (30, 'a_armor', 'Áo Vải', 'Thô sơ.', 'ARMOR', 'ARMOR', 1, 'COMMON', 40, 'a_armor', '{"DEF_FLAT": [2, 5]}'),
       (31, 'a_armor', 'Giáp Da', 'Bền bỉ.', 'ARMOR', 'ARMOR', 2, 'UNCOMMON', 150, 'a_armor', '{"DEF_FLAT": [8, 15]}');

-- CONSUMABLE
INSERT INTO items (item_id, code, name, description, type, slot_type, tier, base_rarity, base_price, image_url)
VALUES (40, 'r_potion', 'Bình Máu', 'Hồi 50 HP.', 'CONSUMABLE', 'CONSUMABLE', 1, 'COMMON', 20, 'r_potion.png');

-- =======================================================
-- 3. QUÁI VẬT (ENEMIES)
-- =======================================================
INSERT INTO enemies (name, level, hp, atk, def, speed, exp_reward, gold_reward, image_url)
VALUES ('Slime', 1, 50, 8, 2, 8, 15, 10, 'idle_mushroom.png'),
       ('Goblin', 5, 120, 15, 5, 10, 30, 20, 'idle_goblin.png'),
       ('Sói Hoang', 10, 200, 30, 10, 15, 50, 30, 'idle_skeleton.png');

-- Update ID Sequence
-- (Lệnh này quan trọng để khi insert mới không bị lỗi duplicate key)
-- Trong MySQL thì dùng AUTO_INCREMENT tự động, nhưng nếu insert cứng ID thì cần set lại.
-- PostgreSQL dùng setval, MySQL tự nhảy.