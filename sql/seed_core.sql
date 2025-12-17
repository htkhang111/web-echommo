USE echommo_db;

-- 1. TẠO TÀI KHOẢN ADMIN
INSERT INTO users (username, password_hash, password, email, full_name, role, avatar_url)
VALUES ('admin', '$2a$10$wW/i.b/w.w/wW/i.b/w.w/wW/i.b/w.w/wW/i.b/w.w', '123456', 'admin@echommo.com', 'Game Master', 'ADMIN', '🐲');

-- 2. TẠO VÍ ADMIN
INSERT INTO wallet (user_id, gold, diamonds, wood, stone, iron_ore, platinum)
VALUES (1, 999999, 99999, 1000, 1000, 1000, 100);

-- 3. TẠO NHÂN VẬT ADMIN
INSERT INTO characters (user_id, name, level, str, vit, agi, int_stat, current_hp, max_hp, base_atk, base_def)
VALUES (1, 'ADMIN', 99, 999, 999, 999, 999, 99999, 99999, 9999, 9999);

-- 4. KHỞI TẠO DANH SÁCH QUÁI VẬT (ENEMIES)
INSERT INTO enemies (name, level, hp, atk, def, speed, exp_reward, gold_reward, image_url) VALUES
                                                                                               ('Yêu Tinh', 1, 50, 8, 2, 8, 15, 10, 'idle_goblin'),
                                                                                               ('Nấm Độc', 2, 80, 12, 3, 5, 25, 15, 'idle_mushroom'),
                                                                                               ('Bộ Xương', 3, 120, 18, 5, 9, 40, 25, 'idle_skeleton');

-- 5. KHỞI TẠO DANH SÁCH ITEM HỆ THỐNG
INSERT INTO items (name, description, type, slot_type, tier, base_rarity, base_price, image_url) VALUES
                                                                                                     ('Kiếm Gỗ', 'Kiếm tập luyện cho người mới', 'WEAPON', 'WEAPON', 1, 'COMMON', 50, 's_sword_0'),
                                                                                                     ('Kiếm Sắt', 'Sắc bén hơn kiếm gỗ', 'WEAPON', 'WEAPON', 1, 'RARE', 200, 's_sword_1'),
                                                                                                     ('Kiếm Hiệp Sĩ', 'Kiếm tiêu chuẩn của hiệp sĩ', 'WEAPON', 'WEAPON', 2, 'RARE', 500, 's_sword_2'),
                                                                                                     ('Áo Vải', 'Áo thô sơ che thân', 'ARMOR', 'ARMOR', 1, 'COMMON', 40, 'a_armor_0'),
                                                                                                     ('Áo Da', 'Làm từ da thú cứng', 'ARMOR', 'ARMOR', 1, 'RARE', 150, 'a_armor_1'),
                                                                                                     ('Mũ Da', 'Bảo vệ đầu cơ bản', 'ARMOR', 'HELMET', 1, 'COMMON', 45, 'h_helmet_0'),
                                                                                                     ('Mũ Sắt', 'Mũ bảo vệ cứng cáp', 'ARMOR', 'HELMET', 1, 'RARE', 160, 'h_helmet_1'),
                                                                                                     ('Giày Cỏ', 'Giúp đi lại đỡ đau chân', 'ARMOR', 'BOOTS', 1, 'COMMON', 30, 'b_boot_0'),
                                                                                                     ('Giày Da', 'Di chuyển nhanh nhẹn hơn', 'ARMOR', 'BOOTS', 1, 'RARE', 120, 'b_boot_1'),
                                                                                                     ('Nhẫn Đồng', 'Nhẫn trang sức đơn giản', 'ACCESSORY', 'RING', 1, 'COMMON', 100, 'ri_ring_0'),
                                                                                                     ('Nhẫn Bạc', 'Tăng nhẹ sức mạnh phép thuật', 'ACCESSORY', 'RING', 1, 'RARE', 300, 'ri_ring_1'),
                                                                                                     ('Vòng Cổ Đá', 'Vòng cổ làm từ đá thô', 'ACCESSORY', 'NECKLACE', 1, 'COMMON', 100, 'n_neck_0'),
                                                                                                     ('Vòng Cổ Ngọc', 'Phát ra ánh sáng nhẹ', 'ACCESSORY', 'NECKLACE', 1, 'RARE', 350, 'n_neck_1'),
                                                                                                     ('Bình Máu Nhỏ', 'Hồi 50 HP', 'CONSUMABLE', 'NONE', 1, 'COMMON', 20, 'https://cdn-icons-png.flaticon.com/512/863/863816.png');

-- 6. TẠO ITEM CHO ADMIN (Sử dụng char_id = 1)
INSERT INTO user_items (char_id, item_id, quantity, is_equipped, rarity, enhance_level, is_mythic, mythic_level, main_stat_type, main_stat_value, original_main_stat_value, sub_stats)
VALUES
-- Item 1: Kiếm Hiệp Sĩ Mythic
(1, 3, 1, TRUE, 'MYTHIC', 30, TRUE, 5, 'ATK_FLAT', 550, 500,
 '[{"code": "CRIT_RATE", "value": 15.0, "is_percent": true}, {"code": "CRIT_DMG", "value": 25.0, "is_percent": true}, {"code": "SPEED", "value": 12, "is_percent": false}, {"code": "HP_PERCENT", "value": 8.0, "is_percent": true}]'),
-- Item 2: Kiếm gỗ thường trong túi
(1, 1, 1, FALSE, 'COMMON', 0, FALSE, 0, 'ATK_FLAT', 10, 10, NULL);

SELECT 'SEED CORE COMPLETED' AS status;