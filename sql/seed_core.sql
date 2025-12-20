USE echommo_db;

-- Xóa dữ liệu cũ để tránh trùng lặp
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
SET FOREIGN_KEY_CHECKS = 1;

-- RESOURCES
INSERT INTO items (item_id, code, name, type, base_rarity, tier, base_price, description, image_url, slot_type)
VALUES (1, 'w_wood', 'Gỗ Sồi', 'MATERIAL', 'COMMON', 1, 10, 'Gỗ thông thường.', 'w_wood.png', 'MATERIAL'),
       (2, 'w_woodRed', 'Gỗ Khô', 'MATERIAL', 'COMMON', 1, 15, 'Gỗ đã qua xử lý.', 'w_woodRed.png', 'MATERIAL'),
       (3, 'w_woodWhite', 'Gỗ Lạnh', 'MATERIAL', 'UNCOMMON', 2, 50, 'Gỗ từ vùng băng giá.', 'w_woodWhite.png',
        'MATERIAL'),
       (4, 'w_woodBlack', 'Gỗ Hắc Ám', 'MATERIAL', 'MYTHIC', 5, 500, 'Gỗ đen tuyền, có tỷ lệ rơi Echo.',
        'w_woodBlack.png', 'MATERIAL'),
       (5, 'o_coalOre', 'Than', 'MATERIAL', 'COMMON', 1, 5, 'Nhiên liệu đen.', 'o_coalOre.png', 'MATERIAL'),
       (6, 'o_copperOre', 'Quặng Đồng', 'MATERIAL', 'COMMON', 1, 10, 'Nguyên liệu đúc đồng.', 'o_copperOre.png',
        'MATERIAL'),
       (7, 'o_ironOre', 'Sắt', 'MATERIAL', 'RARE', 2, 50, 'Kim loại cứng cáp.', 'o_ironOre.png', 'MATERIAL'),
       (8, 'o_platinumOre', 'Bạch Kim', 'MATERIAL', 'EPIC', 3, 300, 'Kim loại quý hiếm.', 'o_platinumOre.png',
        'MATERIAL'),
       (12, 'o_strangeOre', 'Nguyên liệu lạ', 'MATERIAL', 'MYTHIC', 5, 1000, 'Vật phẩm bí ẩn.', 'o_strangeOre.png',
        'MATERIAL'),
       (14, 'o_goldOre', 'Mỏ Vàng', 'MATERIAL', 'LEGENDARY', 1, 0, 'Chứa đầy vàng ròng.', 'o_goldOre.png', 'MATERIAL'),
       (9, 'f_fish', 'Cá', 'MATERIAL', 'COMMON', 1, 10, 'Cá sông tươi ngon.', 'r_fish.png', 'MATERIAL'),
       (10, 'f_fishShark', 'Cá Mập', 'MATERIAL', 'UNCOMMON', 2, 100, 'Thịt cá mập.', 'r_fishShark.png', 'MATERIAL'),
       (13, 'f_fishMegalodon', 'Megalodon', 'MATERIAL', 'MYTHIC', 5, 2000, 'Quái vật tiền sử.', 'f_fishMegalodon.png',
        'MATERIAL'),
       (11, 'r_coinEcho', 'Echo Coin', 'MATERIAL', 'LEGENDARY', 5, 1000, 'Đồng xu quyền năng.', 'r_coinEcho.png',
        'MATERIAL');

-- EQUIPMENT (Swords, Armors...)
INSERT INTO items (item_id, code, name, description, type, slot_type, tier, base_rarity, base_price, image_url,
                   atk_bonus, def_bonus, hp_bonus, speed_bonus)
VALUES (51, 's_sword_0', 'Kiếm Gỗ', 'Tập luyện.', 'WEAPON', 'WEAPON', 1, 'COMMON', 50, 's_sword_0.png', 5, 0, 0, 0),
       (52, 's_sword_1', 'Kiếm Sắt', 'Sắc bén.', 'WEAPON', 'WEAPON', 1, 'RARE', 200, 's_sword_1.png', 15, 0, 0, 0),
       (53, 's_sword_2', 'Kiếm Hiệp Sĩ', 'Tiêu chuẩn.', 'WEAPON', 'WEAPON', 2, 'RARE', 500, 's_sword_2.png', 30, 5, 0,
        0),
       (54, 's_sword_3', 'Kiếm Tinh Anh', 'Rất nhẹ.', 'WEAPON', 'WEAPON', 3, 'EPIC', 1000, 's_sword_3.png', 50, 5, 20,
        5),
       (55, 's_sword_4', 'Kiếm Rồng', 'Huyền thoại.', 'WEAPON', 'WEAPON', 4, 'LEGENDARY', 5000, 's_sword_4.png', 100,
        10, 50, 10),

       (81, 'a_armor_0', 'Áo Vải', 'Thô sơ.', 'ARMOR', 'ARMOR', 1, 'COMMON', 40, 'a_armor_0.png', 0, 5, 10, 0),
       (82, 'a_armor_1', 'Áo Da', 'Bền bỉ.', 'ARMOR', 'ARMOR', 1, 'RARE', 150, 'a_armor_1.png', 0, 15, 50, 2),
       (83, 'a_armor_2', 'Giáp Sắt', 'Cứng cáp.', 'ARMOR', 'ARMOR', 2, 'RARE', 400, 'a_armor_2.png', 0, 30, 100, -2),
       (84, 'a_armor_3', 'Giáp Bạc', 'Lấp lánh.', 'ARMOR', 'ARMOR', 3, 'EPIC', 900, 'a_armor_3.png', 0, 50, 200, 0),
       (85, 'a_armor_4', 'Giáp Rồng', 'Bất hoại.', 'ARMOR', 'ARMOR', 4, 'LEGENDARY', 4500, 'a_armor_4.png', 5, 80, 500,
        5),

       (111, 'h_helmet_0', 'Mũ Vải', 'Che nắng.', 'ARMOR', 'HELMET', 1, 'COMMON', 30, 'h_helmet_0.png', 0, 2, 5, 0),
       (112, 'h_helmet_1', 'Mũ Da', 'Gọn nhẹ.', 'ARMOR', 'HELMET', 1, 'RARE', 100, 'h_helmet_1.png', 0, 8, 20, 0),
       (113, 'h_helmet_2', 'Mũ Sắt', 'Bảo vệ tốt.', 'ARMOR', 'HELMET', 2, 'RARE', 300, 'h_helmet_2.png', 0, 15, 50, 0),
       (114, 'h_helmet_3', 'Mũ Chiến Binh', 'Ngầu.', 'ARMOR', 'HELMET', 3, 'EPIC', 700, 'h_helmet_3.png', 2, 25, 100,
        0),
       (115, 'h_helmet_4', 'Mũ Hoàng Kim', 'Vương giả.', 'ARMOR', 'HELMET', 4, 'LEGENDARY', 3000, 'h_helmet_4.png', 5,
        40, 200, 0),

       (141, 'b_boot_0', 'Giày Cỏ', 'Đỡ đau chân.', 'ARMOR', 'BOOTS', 1, 'COMMON', 20, 'b_boot_0.png', 0, 1, 0, 2),
       (142, 'b_boot_1', 'Giày Da', 'Êm ái.', 'ARMOR', 'BOOTS', 1, 'RARE', 80, 'b_boot_1.png', 0, 3, 0, 5),
       (143, 'b_boot_2', 'Giày Sắt', 'Nặng nề.', 'ARMOR', 'BOOTS', 2, 'RARE', 250, 'b_boot_2.png', 0, 10, 20, 3),
       (144, 'b_boot_3', 'Giày Tốc Độ', 'Lướt gió.', 'ARMOR', 'BOOTS', 3, 'EPIC', 600, 'b_boot_3.png', 0, 5, 50, 15),
       (145, 'b_boot_4', 'Giày Thần', 'Như bay.', 'ARMOR', 'BOOTS', 4, 'LEGENDARY', 2500, 'b_boot_4.png', 2, 15, 100,
        30),

       (171, 'ri_ring_0', 'Nhẫn Đồng', 'Đơn giản.', 'ACCESSORY', 'RING', 1, 'COMMON', 100, 'ri_ring_0.png', 2, 0, 20,
        0),
       (172, 'ri_ring_1', 'Nhẫn Bạc', 'Tinh xảo.', 'ACCESSORY', 'RING', 1, 'RARE', 300, 'ri_ring_1.png', 5, 0, 50, 0),
       (173, 'ri_ring_2', 'Nhẫn Vàng', 'Quý giá.', 'ACCESSORY', 'RING', 2, 'EPIC', 800, 'ri_ring_2.png', 10, 0, 100, 0),
       (174, 'ri_ring_3', 'Nhẫn Ngọc', 'Phép thuật.', 'ACCESSORY', 'RING', 3, 'LEGENDARY', 2000, 'ri_ring_3.png', 20, 5,
        200, 5),
       (175, 'ri_ring_4', 'Nhẫn Hư Không', 'Bí ẩn.', 'ACCESSORY', 'RING', 4, 'MYTHIC', 5000, 'ri_ring_4.png', 50, 10,
        500, 10),

       (201, 'n_necklace_0', 'Vòng Cổ Đá', 'Thô sơ.', 'ACCESSORY', 'NECKLACE', 1, 'COMMON', 100, 'n_necklace_0.png', 0,
        2, 30, 0),
       (202, 'n_necklace_1', 'Vòng Cổ Xương', 'Hoang dã.', 'ACCESSORY', 'NECKLACE', 1, 'RARE', 350, 'n_necklace_1.png',
        2, 5, 80, 0),
       (203, 'n_necklace_2', 'Vòng Cổ Bạc', 'Sang trọng.', 'ACCESSORY', 'NECKLACE', 2, 'EPIC', 900, 'n_necklace_2.png',
        5, 10, 150, 0),
       (204, 'n_necklace_3', 'Vòng Cổ Hộ Mệnh', 'Bảo vệ.', 'ACCESSORY', 'NECKLACE', 3, 'LEGENDARY', 2200,
        'n_necklace_3.png', 5, 20, 300, 0),
       (205, 'n_necklace_4', 'Vòng Cổ Rồng', 'Uy quyền.', 'ACCESSORY', 'NECKLACE', 4, 'MYTHIC', 5500,
        'n_necklace_4.png', 10, 30, 600, 5),

       (231, 'r_potion', 'Bình Máu Nhỏ', 'Hồi 50 HP', 'CONSUMABLE', 'CONSUMABLE', 1, 'COMMON', 20, 'r_potion.png', 0, 0,
        0, 0);

-- ENEMIES
INSERT INTO enemies (name, level, hp, atk, def, speed, exp_reward, gold_reward, image_url)
VALUES ('Yêu Tinh', 1, 50, 8, 2, 8, 15, 10, 'idle_goblin.png'),
       ('Nấm Độc', 2, 80, 12, 3, 5, 25, 15, 'idle_mushroom.png'),
       ('Bộ Xương', 3, 120, 18, 5, 9, 40, 25, 'idle_skeleton.png'),
       ('Chuột Đồng', 4, 100, 20, 2, 12, 30, 12, 'idle_goblin.png'),
       ('Ác Quỷ Rừng', 10, 300, 40, 10, 15, 60, 30, 'idle_demon1.png'),
       ('Lãng Khách', 15, 500, 60, 20, 20, 100, 50, 'idle_langkhach1.png'),
       ('Kiếm Sĩ', 25, 800, 100, 30, 25, 200, 100, 'idle_yasou.png');

-- ADMIN DATA
INSERT INTO users (username, password_hash, password, email, full_name, role, avatar_url)
VALUES ('admin', '$2a$10$wPOKcn9CM0dlp.k83kEHne1UU90Y5.RL2MaLkqwJ0ZRnN3IbsRnnS', '123456', 'admin@echommo.com',
        'Trương Khuynh Hàn', 'ADMIN', 'skin_yasou');

INSERT INTO wallet (user_id, gold, echo_coin, diamonds, wood, dried_wood, cold_wood, strange_wood, coal, copper_ore,
                    iron_ore, platinum, fish, shark, unknown_material)
VALUES (1, 999999, 100.5000, 0, 1000, 100, 100, 50, 1000, 500, 500, 100, 200, 10, 1);

INSERT INTO characters (user_id, name, level, str, vit, agi, int_stat, current_hp, max_hp, base_atk, base_def)
VALUES (1, 'Ghét Hải Sản', 99, 999, 999, 999, 999, 99999, 99999, 9999, 9999);

INSERT INTO user_items (char_id, item_id, quantity, is_equipped, rarity, enhance_level, is_mythic, mythic_level,
                        main_stat_type, main_stat_value, original_main_stat_value, sub_stats)
VALUES (1, 55, 1, TRUE, 'MYTHIC', 50, TRUE, 10, 'ATK_FLAT', 9999, 5000,
        '[{"code": "CRIT_RATE", "value": 50.0, "is_percent": true}]'),
       (1, 85, 1, TRUE, 'LEGENDARY', 20, FALSE, 0, 'DEF_FLAT', 500, 500, NULL);