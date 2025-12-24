-- ======================================================================================
-- PHẦN 2: SEED DATA (DỮ LIỆU MẪU - ITEMS, USER, QUÁI)
-- ======================================================================================

-- 1. RESOURCES (Đã sửa tên ảnh cho đúng folder material)
INSERT INTO items (item_id, code, name, type, slot_type, base_rarity, tier, base_price, description, image_url) VALUES
                                                                                                                    (1, 'w_wood', 'Gỗ Sồi', 'MATERIAL', 'MATERIAL', 'COMMON', 1, 10, 'Gỗ thông thường.', 'w_wood.png'),
                                                                                                                    (2, 'w_wood-red', 'Gỗ Khô', 'MATERIAL', 'MATERIAL', 'COMMON', 1, 15, 'Gỗ đã qua xử lý.', 'w_wood-red.png'),
                                                                                                                    (3, 'w_wood-white', 'Gỗ Lạnh', 'MATERIAL', 'MATERIAL', 'UNCOMMON', 2, 50, 'Gỗ từ vùng băng giá.', 'w_wood-white.png'),
                                                                                                                    (4, 'w_wood-black', 'Gỗ Hắc Ám', 'MATERIAL', 'MATERIAL', 'MYTHIC', 5, 500, 'Gỗ đen tuyền, cực hiếm.', 'w_wood-black.png'),
                                                                                                                    (5, 'o_coal', 'Than', 'MATERIAL', 'MATERIAL', 'COMMON', 1, 5, 'Nhiên liệu đen.', 'o_coal.png'),
                                                                                                                    (6, 'o_copper', 'Quặng Đồng', 'MATERIAL', 'MATERIAL', 'COMMON', 1, 10, 'Nguyên liệu đúc đồng.', 'o_copper.png'),
                                                                                                                    (7, 'o_iron', 'Sắt', 'MATERIAL', 'MATERIAL', 'RARE', 2, 50, 'Kim loại cứng cáp.', 'o_iron.png'),
                                                                                                                    (8, 'o_platinum', 'Bạch Kim', 'MATERIAL', 'MATERIAL', 'EPIC', 3, 300, 'Kim loại quý hiếm.', 'o_platinum.png'),
                                                                                                                    (9, 'f_fish', 'Cá', 'MATERIAL', 'MATERIAL', 'COMMON', 1, 10, 'Cá sông tươi ngon.', 'f_fish.png'),
                                                                                                                    (10, 'f_shark', 'Cá Mập', 'MATERIAL', 'MATERIAL', 'UNCOMMON', 2, 100, 'Thịt cá mập.', 'f_shark.png'),
                                                                                                                    (11, 'r_coinEcho', 'Echo Coin', 'MATERIAL', 'MATERIAL', 'LEGENDARY', 5, 1000, 'Đồng xu quyền năng.', 'r_coin-echo.png'),
                                                                                                                    (12, 'o_strange', 'Nguyên liệu lạ', 'MATERIAL', 'MATERIAL', 'MYTHIC', 5, 1000, 'Vật phẩm bí ẩn.', 'o_strange.png'),
                                                                                                                    (13, 'f_whiteshark', 'Cá Mập Trắng', 'MATERIAL', 'MATERIAL', 'RARE', 3, 500, 'Chúa tể vùng nước ngọt.', 'f_whiteshark.png'),
                                                                                                                    (14, 'f_megalodon', 'Megalodon', 'MATERIAL', 'MATERIAL', 'LEGENDARY', 5, 2000, 'Quái vật tiền sử.', 'f_megalodon.png');

-- 2. TRANG BỊ (Giữ đúng ID để khớp với Admin)
-- SWORDS (51-55)
INSERT INTO items (item_id, code, name, description, type, slot_type, tier, base_rarity, base_price, image_url, atk_bonus, def_bonus, hp_bonus, speed_bonus) VALUES
                                                                                                                                                                 (51, 's_sword_0', 'Kiếm Gỗ', 'Tập luyện.', 'WEAPON', 'WEAPON', 1, 'COMMON', 50, 's_sword_0.png', 5, 0, 0, 0),
                                                                                                                                                                 (52, 's_sword_1', 'Kiếm Sắt', 'Sắc bén.', 'WEAPON', 'WEAPON', 1, 'RARE', 200, 's_sword_1.png', 15, 0, 0, 0),
                                                                                                                                                                 (53, 's_sword_2', 'Kiếm Hiệp Sĩ', 'Tiêu chuẩn.', 'WEAPON', 'WEAPON', 2, 'RARE', 500, 's_sword_2.png', 30, 5, 0, 0),
                                                                                                                                                                 (54, 's_sword_3', 'Kiếm Tinh Anh', 'Rất nhẹ.', 'WEAPON', 'WEAPON', 3, 'EPIC', 1000, 's_sword_3.png', 50, 5, 20, 5),
                                                                                                                                                                 (55, 's_sword_4', 'Kiếm Rồng', 'Huyền thoại.', 'WEAPON', 'WEAPON', 4, 'LEGENDARY', 5000, 's_sword_4.png', 100, 10, 50, 10);

-- ARMORS (81-85)
INSERT INTO items (item_id, code, name, description, type, slot_type, tier, base_rarity, base_price, image_url, atk_bonus, def_bonus, hp_bonus, speed_bonus) VALUES
                                                                                                                                                                 (81, 'a_armor_0', 'Áo Vải', 'Thô sơ.', 'ARMOR', 'ARMOR', 1, 'COMMON', 40, 'a_armor_0.png', 0, 5, 10, 0),
                                                                                                                                                                 (82, 'a_armor_1', 'Áo Da', 'Bền bỉ.', 'ARMOR', 'ARMOR', 1, 'RARE', 150, 'a_armor_1.png', 0, 15, 50, 2),
                                                                                                                                                                 (83, 'a_armor_2', 'Giáp Sắt', 'Cứng cáp.', 'ARMOR', 'ARMOR', 2, 'RARE', 400, 'a_armor_2.png', 0, 30, 100, -2),
                                                                                                                                                                 (84, 'a_armor_3', 'Giáp Bạc', 'Lấp lánh.', 'ARMOR', 'ARMOR', 3, 'EPIC', 900, 'a_armor_3.png', 0, 50, 200, 0),
                                                                                                                                                                 (85, 'a_armor_4', 'Giáp Rồng', 'Bất hoại.', 'ARMOR', 'ARMOR', 4, 'LEGENDARY', 4500, 'a_armor_4.png', 5, 80, 500, 5);

-- HELMETS (111-115)
INSERT INTO items (item_id, code, name, description, type, slot_type, tier, base_rarity, base_price, image_url, atk_bonus, def_bonus, hp_bonus, speed_bonus) VALUES
                                                                                                                                                                 (111, 'h_helmet_0', 'Mũ Vải', 'Che nắng.', 'ARMOR', 'HELMET', 1, 'COMMON', 30, 'h_helmet_0.png', 0, 2, 5, 0),
                                                                                                                                                                 (112, 'h_helmet_1', 'Mũ Da', 'Gọn nhẹ.', 'ARMOR', 'HELMET', 1, 'RARE', 100, 'h_helmet_1.png', 0, 8, 20, 0),
                                                                                                                                                                 (113, 'h_helmet_2', 'Mũ Sắt', 'Bảo vệ tốt.', 'ARMOR', 'HELMET', 2, 'RARE', 300, 'h_helmet_2.png', 0, 15, 50, 0),
                                                                                                                                                                 (114, 'h_helmet_3', 'Mũ Chiến Binh', 'Ngầu.', 'ARMOR', 'HELMET', 3, 'EPIC', 700, 'h_helmet_3.png', 2, 25, 100, 0),
                                                                                                                                                                 (115, 'h_helmet_4', 'Mũ Hoàng Kim', 'Vương giả.', 'ARMOR', 'HELMET', 4, 'LEGENDARY', 3000, 'h_helmet_4.png', 5, 40, 200, 0);

-- BOOTS (141-145)
INSERT INTO items (item_id, code, name, description, type, slot_type, tier, base_rarity, base_price, image_url, atk_bonus, def_bonus, hp_bonus, speed_bonus) VALUES
                                                                                                                                                                 (141, 'b_boot_0', 'Giày Cỏ', 'Đỡ đau chân.', 'ARMOR', 'BOOTS', 1, 'COMMON', 20, 'b_boot_0.png', 0, 1, 0, 2),
                                                                                                                                                                 (142, 'b_boot_1', 'Giày Da', 'Êm ái.', 'ARMOR', 'BOOTS', 1, 'RARE', 80, 'b_boot_1.png', 0, 3, 0, 5),
                                                                                                                                                                 (143, 'b_boot_2', 'Giày Sắt', 'Nặng nề.', 'ARMOR', 'BOOTS', 2, 'RARE', 250, 'b_boot_2.png', 0, 10, 20, 3),
                                                                                                                                                                 (144, 'b_boot_3', 'Giày Tốc Độ', 'Lướt gió.', 'ARMOR', 'BOOTS', 3, 'EPIC', 600, 'b_boot_3.png', 0, 5, 50, 15),
                                                                                                                                                                 (145, 'b_boot_4', 'Giày Thần', 'Như bay.', 'ARMOR', 'BOOTS', 4, 'LEGENDARY', 2500, 'b_boot_4.png', 2, 15, 100, 30);

-- RINGS (171-175)
INSERT INTO items (item_id, code, name, description, type, slot_type, tier, base_rarity, base_price, image_url, atk_bonus, def_bonus, hp_bonus, speed_bonus) VALUES
                                                                                                                                                                 (171, 'ri_ring_0', 'Nhẫn Đồng', 'Đơn giản.', 'ACCESSORY', 'RING', 1, 'COMMON', 100, 'ri_ring_0.png', 2, 0, 20, 0),
                                                                                                                                                                 (172, 'ri_ring_1', 'Nhẫn Bạc', 'Tinh xảo.', 'ACCESSORY', 'RING', 1, 'RARE', 300, 'ri_ring_1.png', 5, 0, 50, 0),
                                                                                                                                                                 (173, 'ri_ring_2', 'Nhẫn Vàng', 'Quý giá.', 'ACCESSORY', 'RING', 2, 'EPIC', 800, 'ri_ring_2.png', 10, 0, 100, 0),
                                                                                                                                                                 (174, 'ri_ring_3', 'Nhẫn Ngọc', 'Phép thuật.', 'ACCESSORY', 'RING', 3, 'LEGENDARY', 2000, 'ri_ring_3.png', 20, 5, 200, 5),
                                                                                                                                                                 (175, 'ri_ring_4', 'Nhẫn Hư Không', 'Bí ẩn.', 'ACCESSORY', 'RING', 4, 'MYTHIC', 5000, 'ri_ring_4.png', 50, 10, 500, 10);

-- NECKLACES (201-205)
INSERT INTO items (item_id, code, name, description, type, slot_type, tier, base_rarity, base_price, image_url, atk_bonus, def_bonus, hp_bonus, speed_bonus) VALUES
                                                                                                                                                                 (201, 'n_necklace_0', 'Vòng Cổ Đá', 'Thô sơ.', 'ACCESSORY', 'NECKLACE', 1, 'COMMON', 100, 'n_necklace_0.png', 0, 2, 30, 0),
                                                                                                                                                                 (202, 'n_necklace_1', 'Vòng Cổ Xương', 'Hoang dã.', 'ACCESSORY', 'NECKLACE', 1, 'RARE', 350, 'n_necklace_1.png', 2, 5, 80, 0),
                                                                                                                                                                 (203, 'n_necklace_2', 'Vòng Cổ Bạc', 'Sang trọng.', 'ACCESSORY', 'NECKLACE', 2, 'EPIC', 900, 'n_necklace_2.png', 5, 10, 150, 0),
                                                                                                                                                                 (204, 'n_necklace_3', 'Vòng Cổ Hộ Mệnh', 'Bảo vệ.', 'ACCESSORY', 'NECKLACE', 3, 'LEGENDARY', 2200, 'n_necklace_3.png', 5, 20, 300, 0),
                                                                                                                                                                 (205, 'n_necklace_4', 'Vòng Cổ Rồng', 'Uy quyền.', 'ACCESSORY', 'NECKLACE', 4, 'MYTHIC', 5500, 'n_necklace_4.png', 10, 30, 600, 5);

-- POTION (231)
INSERT INTO items (item_id, code, name, description, type, slot_type, tier, base_rarity, base_price, image_url, atk_bonus, def_bonus, hp_bonus, speed_bonus) VALUES
    (231, 'r_potion', 'Bình Máu Nhỏ', 'Hồi 50 HP', 'CONSUMABLE', 'CONSUMABLE', 1, 'COMMON', 20, 'r_potion.png', 0, 0, 0, 0);


-- 3. CÔNG CỤ (GATHERING TOOLS) - Để ID tự tăng nhưng Code phải chuẩn
-- AXE
INSERT INTO items (code, name, description, type, slot_type, tier, base_rarity, base_price, image_url, is_system_item) VALUES
                                                                                                                           ('AXE_T1', 'Rìu Thép', 'Rìu cơ bản.', 'TOOL', 'AXE', 1, 'COMMON', 100, 'tool/axe/a-0-strongaxe.png', true),
                                                                                                                           ('AXE_T2', 'Rìu Chiến', 'Sắc bén hơn.', 'TOOL', 'AXE', 2, 'UNCOMMON', 500, 'tool/axe/a-1-best axeinthegame.png', true),
                                                                                                                           ('AXE_T3', 'Thần Rìu', 'Rìu thần thánh.', 'TOOL', 'AXE', 3, 'RARE', 2000, 'tool/axe/a-2-axeofthegods.png', true),
                                                                                                                           ('AXE_T4', 'Rìu Khổng Lồ', 'Sức mạnh lớn.', 'TOOL', 'AXE', 4, 'EPIC', 10000, 'tool/axe/a-3-thiccaxe.png', true),
                                                                                                                           ('AXE_T5_HOLLOW', 'Rìu Vong Hồn', 'Lưỡi rìu linh hồn.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-axeofhollowsouls.png', true),
                                                                                                                           ('AXE_T5_PUMPKIN', 'Rìu Bí Ngô', 'Mang hương vị của lễ hội Halloween.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-axinpumpkin.png', true),
                                                                                                                           ('AXE_T5_BLOSSOM', 'Rìu Hoa Khai', 'Mỗi nhát chặt khiến cây cối nở hoa.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-blossomingaxe.png', true),
                                                                                                                           ('AXE_T5_BUNNY', 'Cưa Máy Thỏ', 'Dễ thương nhưng cực kỳ nguy hiểm.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-bunnychainsaw.png', true),
                                                                                                                           ('AXE_T5_DIM', 'Rìu Không Gian', 'Xé toạc không gian để đốn hạ thần mộc.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-dimensionalaxe.png', true),
                                                                                                                           ('AXE_T5_EDEN', 'Rìu Địa Đàng', 'Pháp bảo thất lạc từ vườn Eden.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-edensaxe.png', true),
                                                                                                                           ('AXE_T5_ERROR', 'Rìu Lỗi', 'Vật phẩm bị nguyền rủa bởi 404 Not Found.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-erroraxe.png', true),
                                                                                                                           ('AXE_T5_FAMINE', 'Rìu Đói Khát', 'Luôn thèm khát nhựa sống của cây cổ thụ.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-famine.png', true),
                                                                                                                           ('AXE_T5_FROST', 'Rìu Băng Giá', 'Đóng băng thân cây ngay khi chạm vào.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-frostbiteaxe.png', true),
                                                                                                                           ('AXE_T5_GLACIAL', 'Rìu Hàn Băng', 'Lạnh lẽo hơn cả trái tim người yêu cũ.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-glacialaxe.png', true),
                                                                                                                           ('AXE_T5_HALLOW', 'Cưa Thánh', 'Thanh tẩy rừng rậm bằng sức mạnh ánh sáng.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-hallowedchainsaw.png', true),
                                                                                                                           ('AXE_T5_CORE', 'Rìu Mộc Tâm', 'Rèn từ lõi của cây thế giới.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-heartcoreaxe.png', true),
                                                                                                                           ('AXE_T5_FOREST', 'Trái Tim Rừng', 'Nhịp đập của rừng già hòa vào lưỡi rìu.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-heartoftheforest.webp', true),
                                                                                                                           ('AXE_T5_DUCK', 'Rìu Vịt Vàng', 'Quạc quạc! Chặt cây phong cách vui nhộn.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-quackingaxe.png', true),
                                                                                                                           ('AXE_T5_RETRO', 'Rìu Cổ Điển', 'Phong cách Pixel Art hoài cổ.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-retromanicaxe.png', true),
                                                                                                                           ('AXE_T5_SPITE', 'Rìu Oán Hận', 'Chứa đầy sự phẫn nộ của thiên nhiên.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-spitefulaxe.png', true),
                                                                                                                           ('AXE_T5_SPRING', 'Rìu Mùa Xuân', 'Mang sức sống mãnh liệt của mùa xuân.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-springtimeaxe.png', true),
                                                                                                                           ('AXE_T5_EYE', 'Nhãn Rìu', 'Nó luôn dõi theo bạn... đừng nhìn lại.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-stoplookingatmyaxe.png', true),
                                                                                                                           ('AXE_T5_EYE2', 'Song Nhãn Rìu', 'Phiên bản nâng cấp với hai con mắt.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-stoplookingatmyaxe2.png', true),
                                                                                                                           ('AXE_T5_TIME', 'Rìu Thất Lạc', 'Đến từ một dòng thời gian đã bị lãng quên.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-timelostaxe.png', true),
                                                                                                                           ('AXE_T5_PINE', 'Rìu Thông', 'Chuyên trị các loại cây lá kim.', 'TOOL', 'AXE', 5, 'LEGENDARY', 50000, 'tool/axe/a-4-wontyoubepine.png', true);

-- PICKAXE
INSERT INTO items (code, name, description, type, slot_type, tier, base_rarity, base_price, image_url, is_system_item) VALUES
                                                                                                                           ('PICK_T1', 'Cúp Sắt', 'Dụng cụ đào mỏ.', 'TOOL', 'PICKAXE', 1, 'COMMON', 100, 'tool/pickaxe/p-0-strongpickaxe.png', true),
                                                                                                                           ('PICK_T2', 'Cúp Viking', 'Chiến binh phương Bắc.', 'TOOL', 'PICKAXE', 2, 'UNCOMMON', 500, 'tool/pickaxe/p-1-vikingpickaxe.png', true),
                                                                                                                           ('PICK_T3', 'Siêu Cúp', 'Đào nhanh gấp đôi.', 'TOOL', 'PICKAXE', 3, 'RARE', 2000, 'tool/pickaxe/p-2-superduperpickaxe.png', true),
                                                                                                                           ('PICK_T4', 'Tử Thần Cúp', 'Hơi thở cái chết.', 'TOOL', 'PICKAXE', 4, 'EPIC', 10000, 'tool/pickaxe/p-3-deathandaxes.png', true),
                                                                                                                           ('PICK_T5_BLOSSOM', 'Cúp Hoa Nở', 'Đá nở hoa dưới mỗi nhát cuốc.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-blossomingpick.png', true),
                                                                                                                           ('PICK_T5_CHOCO', 'Cúp Socola', 'Ngọt ngào nhưng cứng hơn thép.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-chocaholicrockaholic.png', true),
                                                                                                                           ('PICK_T5_CLOCK', 'Cúp Máy Móc', 'Vận hành bằng bánh răng và hơi nước.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-clockworkmattock.png', true),
                                                                                                                           ('PICK_T5_DIM', 'Cúp Không Gian', 'Đào xuyên qua các chiều không gian.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-dimensionalpickaxe.png', true),
                                                                                                                           ('PICK_T5_EDEN', 'Cúp Địa Đàng', 'Được dùng để khai thác đá quý trên thiên giới.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-edenspickaxe.png', true),
                                                                                                                           ('PICK_T5_FROST', 'Cúp Băng Giá', 'Làm lạnh quặng ngay khi khai thác.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-frostibepick.png', true),
                                                                                                                           ('PICK_T5_GLACIAL', 'Cúp Hàn Băng', 'Đập tan băng vĩnh cửu ngàn năm.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-glacialpickaxe.png', true),
                                                                                                                           ('PICK_T5_HALLOW', 'Cúp Thánh', 'Phát sáng trong những hang động tăm tối nhất.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-hallowedpickaxe.png', true),
                                                                                                                           ('PICK_T5_CORE', 'Cúp Huyết Tâm', 'Rung động theo nhịp đập của trái đất.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-heartcorepick.png', true),
                                                                                                                           ('PICK_T5_SANTA', 'Cúp Giáng Sinh', 'Quà tặng của ông già Noel.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-jollyoldsaintpick.png', true),
                                                                                                                           ('PICK_T5_KISS', 'Cúp Tình Yêu', 'Đào khoáng bằng sức mạnh tình yêu.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-kissthyaxe.png', true),
                                                                                                                           ('PICK_T5_HOLLOW', 'Cúp Vong Hồn', 'Tiếng kêu than vọng ra từ lòng đất.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-pickaxeofhollowsouls.png', true),
                                                                                                                           ('PICK_T5_PUMPKIN', 'Cúp Bí Ngô', 'Dụng cụ yêu thích của kỵ sĩ không đầu.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-pickinpumpkin.png', true),
                                                                                                                           ('PICK_T5_POULTRY', 'Cúp Gà Tây', 'Hoàn hảo cho lễ Tạ Ơn.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-poultringpickaxe.png', true),
                                                                                                                           ('PICK_T5_RETRO', 'Cúp Cổ Điển', 'Ký ức về những trò chơi 8-bit.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-retromanicpickaxe.png', true),
                                                                                                                           ('PICK_T5_SPRING', 'Cúp Mùa Xuân', 'Đánh thức khoáng sản đang ngủ đông.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-springtimepickaxe.png', true),
                                                                                                                           ('PICK_T5_CUTE', 'Cúp Dễ Thương', 'Màu hồng nam tính nhất thiên hạ.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-thecutestofpickaxes.png', true),
                                                                                                                           ('PICK_T5_TIME', 'Cúp Thời Gian', 'Khai quật những di tích bị lãng quên.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-timelostpickaxe.png', true),
                                                                                                                           ('PICK_T5_TOXIC', 'Cúp Độc Dược', 'Cẩn thận kẻo bị nhiễm độc.', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-toxicpickaxe.png', true),
                                                                                                                           ('PICK_T5_ROCK', 'Cúp Rock', 'Let''s Rock and Roll!', 'TOOL', 'PICKAXE', 5, 'LEGENDARY', 50000, 'tool/pickaxe/p-4-yourockmyworld.png', true);

-- SHOVEL
INSERT INTO items (code, name, description, type, slot_type, tier, base_rarity, base_price, image_url, is_system_item) VALUES
                                                                                                                           ('SHOVEL_T1', 'Xẻng Thường', 'Xẻng làm vườn cơ bản.', 'TOOL', 'SHOVEL', 1, 'COMMON', 100, 'tool/shovel/s-0-strongshovel.png', true),
                                                                                                                           ('SHOVEL_T2', 'Xẻng Sử Thi', 'Được rèn từ thép tốt.', 'TOOL', 'SHOVEL', 2, 'UNCOMMON', 500, 'tool/shovel/s-1-epicshovel.png', true),
                                                                                                                           ('SHOVEL_T3', 'Xẻng Đệ Nhị', 'Phiên bản cải tiến vượt bậc.', 'TOOL', 'SHOVEL', 3, 'RARE', 2000, 'tool/shovel/s-2-theshovelii.png', true),
                                                                                                                           ('SHOVEL_T4', 'Siêu Xẻng', 'Đào đất nhanh như chớp.', 'TOOL', 'SHOVEL', 4, 'EPIC', 10000, 'tool/shovel/s-3-supershovel.png', true),
                                                                                                                           ('SHOVEL_T5_BLOSSOM', 'Xẻng Hoa Khai', 'Biến đất cằn cỗi thành vườn hoa.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-blossomingshovel.png', true),
                                                                                                                           ('SHOVEL_T5_COSMIC', 'Xẻng Vũ Trụ', 'Chứa đựng bụi sao của thiên hà.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-cosmicshovel.png', true),
                                                                                                                           ('SHOVEL_T5_DIM', 'Xẻng Không Gian', 'Đào hố đen vũ trụ.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-dimensionaldigger.png', true),
                                                                                                                           ('SHOVEL_T5_EDEN', 'Xẻng Địa Đàng', 'Dụng cụ làm vườn của các thiên thần.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-edensshovel.png', true),
                                                                                                                           ('SHOVEL_T5_EGG', 'Xẻng Săn Trứng', 'Chuyên dụng để tìm trứng Phục Sinh.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-egghunter.png', true),
                                                                                                                           ('SHOVEL_T5_FROST', 'Xẻng Băng Giá', 'Đào đất băng vĩnh cửu.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-frostbiteshovel.png', true),
                                                                                                                           ('SHOVEL_T5_GLACIAL', 'Xẻng Hàn Băng', 'Lạnh thấu xương tủy.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-glacialshovel.png', true),
                                                                                                                           ('SHOVEL_T5_GOLD', 'Xẻng Đào Vàng', 'Tăng cực đại tỷ lệ tìm thấy vàng.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-golddigger.png', true),
                                                                                                                           ('SHOVEL_T5_GOLD2', 'Siêu Xẻng Vàng', 'Phiên bản nâng cấp của Đào Vàng.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-golddigger2.png', true),
                                                                                                                           ('SHOVEL_T5_DIVORCE', 'Xẻng Chia Ly', 'Cắt đứt mọi mối liên kết của đất.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-groundsfordivorce.png', true),
                                                                                                                           ('SHOVEL_T5_HALLOW', 'Xẻng Thánh', 'Thanh tẩy vùng đất bị ô nhiễm.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-hallowedshovel.png', true),
                                                                                                                           ('SHOVEL_T5_CORE', 'Xẻng Địa Tâm', 'Đào thẳng vào lõi trái đất.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-heartcoreshovel.png', true),
                                                                                                                           ('SHOVEL_T5_DUCK', 'Xẻng Vịt May Mắn', 'Mang lại may mắn khi đào kho báu.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-luckyduckshovel.png', true),
                                                                                                                           ('SHOVEL_T5_RETRO', 'Xẻng Cổ Điển', 'Phong cách Retro bất diệt.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-retromanicshovel.png', true),
                                                                                                                           ('SHOVEL_T5_PUMPKIN', 'Xẻng Bí Ngô', 'Thích hợp để chôn cất vào đêm Halloween.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-shovelinpumpkin.png', true),
                                                                                                                           ('SHOVEL_T5_HOLLOW', 'Xẻng Vong Hồn', 'Lưỡi xẻng lạnh lẽo như cõi âm.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-shovelofhollowsouls.png', true),
                                                                                                                           ('SHOVEL_T5_SLEIGH', 'Xẻng Tuyết', 'Dọn tuyết cho xe trượt của ông già Noel.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-sleighinit.png', true),
                                                                                                                           ('SHOVEL_T5_SPOOKY', 'Xẻng Ma Quái', 'Đào mộ cực nhanh.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-spookyspade.png', true),
                                                                                                                           ('SHOVEL_T5_SPRING', 'Xẻng Mùa Xuân', 'Đánh thức mầm sống trong lòng đất.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-springtimeshovel.png', true),
                                                                                                                           ('SHOVEL_T5_CUTE', 'Xẻng Cute', 'Xẻng đáng yêu nhất hệ mặt trời.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-thecutestofallshovels.png', true),
                                                                                                                           ('SHOVEL_T5_TIME', 'Xẻng Thời Gian', 'Đào lại những ký ức đã mất.', 'TOOL', 'SHOVEL', 5, 'LEGENDARY', 50000, 'tool/shovel/s-4-timelostdigger.png', true);

-- FISHING ROD
INSERT INTO items (code, name, description, type, slot_type, tier, base_rarity, base_price, image_url, is_system_item) VALUES
                                                                                                                           ('ROD_T1', 'Cần Tre', 'Cần câu làm từ tre đơn giản.', 'TOOL', 'FISHING_ROD', 1, 'COMMON', 100, 'tool/fishing-rod/fr-0-strongfishingrod.png', true),
                                                                                                                           ('ROD_T2', 'Cần Siêu Cấp', 'Dây câu bền chắc hơn.', 'TOOL', 'FISHING_ROD', 2, 'UNCOMMON', 500, 'tool/fishing-rod/fr-1-superfishingrod.png', true),
                                                                                                                           ('ROD_T3', 'Cần Sử Thi', 'Được các ngư dân lão luyện tin dùng.', 'TOOL', 'FISHING_ROD', 3, 'RARE', 2000, 'tool/fishing-rod/fr-2-rpicfishingrod.png', true),
                                                                                                                           ('ROD_T4', 'Chúa Tể Cần', 'Có thể câu được thủy quái.', 'TOOL', 'FISHING_ROD', 4, 'EPIC', 10000, 'tool/fishing-rod/fr-3-lordofthefishes.png', true),
                                                                                                                           ('ROD_T5_TURTLE', 'Huyền Vũ Cần', 'Mang sức mạnh bảo hộ của thần rùa Huyền Vũ.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-blacktortoisespurity.png', true),
                                                                                                                           ('ROD_T5_BLOSSOM', 'Cần Hoa Khai', 'Mặt nước nở hoa khi thả câu.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-blossomingrod.png', true),
                                                                                                                           ('ROD_T5_FEEL', 'Cần Cảm Xúc', 'Câu được cả tâm tư của loài cá.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-catchingfeelsrod.png', true),
                                                                                                                           ('ROD_T5_DIM', 'Cần Không Gian', 'Thả câu xuyên qua các chiều không gian.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-dimensionalsweepingrod.png', true),
                                                                                                                           ('ROD_T5_EDEN', 'Cần Địa Đàng', 'Dùng để câu cá ở suối nguồn tươi trẻ.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-edensfishingrod.png', true),
                                                                                                                           ('ROD_T5_EOSTRE', 'Cần Phục Sinh', 'Ban phước lành của nữ thần Eostre.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-eostresfavour.png', true),
                                                                                                                           ('ROD_T5_HOLLOW', 'Cần Vong Hồn', 'Câu được linh hồn dưới đáy sông.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-fishingrodofhollowsouls.png', true),
                                                                                                                           ('ROD_T5_PUMPKIN', 'Cần Bí Ngô', 'Cần câu kỳ dị của lễ hội ma.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-fishinpumpkin.png', true),
                                                                                                                           ('ROD_T5_FOWL', 'Cần Gà', 'Mồi câu hấp dẫn mọi loài cá ham ăn.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-fowlfishingrod.png', true),
                                                                                                                           ('ROD_T5_FROST', 'Cần Băng Giá', 'Dây câu không bao giờ đóng băng.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-frostbiterod.png', true),
                                                                                                                           ('ROD_T5_GLACIAL', 'Lao Hàn Băng', 'Phóng lao bắt cá voi băng.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-glacialharpoon.png', true),
                                                                                                                           ('ROD_T5_HALLOW', 'Lao Thánh', 'Thanh tẩy vùng nước ô nhiễm.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-hallowedharpoon.png', true),
                                                                                                                           ('ROD_T5_CORE', 'Cần Thủy Tâm', 'Kết nối với trái tim của đại dương.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-heartcorerod.png', true),
                                                                                                                           ('ROD_T5_REST', 'Cần An Nghỉ', 'Giúp loài cá ra đi thanh thản.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-restinplaice.png', true),
                                                                                                                           ('ROD_T5_RETRO', 'Cần Cổ Điển', 'Pixel Art Fishing Rod.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-retromanicrod.png', true),
                                                                                                                           ('ROD_T5_ROE', 'Cần Trứng Cá', 'Thu hút cá cái.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-roeroeroe.png', true),
                                                                                                                           ('ROD_T5_SPRING', 'Lao Mùa Xuân', 'Bắt cá đầu mùa.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-springtimeharpoon.png', true),
                                                                                                                           ('ROD_T5_CUTE', 'Cần Dễ Thương', 'Thu hút những loài cá xinh đẹp nhất.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-thecutestfishingrod.png', true),
                                                                                                                           ('ROD_T5_BAIT', 'Bậc Thầy Câu', 'Không con cá nào thoát khỏi lưỡi câu này.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-themasterbaiter.png', true),
                                                                                                                           ('ROD_T5_BAIT2', 'Đại Bậc Thầy', 'Phiên bản tối thượng của cần thủ.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-themasterbaiter2.png', true),
                                                                                                                           ('ROD_T5_TIME', 'Cần Thời Gian', 'Câu cá từ quá khứ.', 'TOOL', 'FISHING_ROD', 5, 'LEGENDARY', 50000, 'tool/fishing-rod/fr-4-timelostfishingrod.png', true);


-- 4. ADMIN & CHARACTER DATA (QUAN TRỌNG)
INSERT INTO users (user_id, username, password_hash, password, email, full_name, role, avatar_url)
VALUES (1, 'admin', '$2a$10$wPOKcn9CM0dlp.k83kEHne1UU90Y5.RL2MaLkqwJ0ZRnN3IbsRnnS', '123456', 'admin@echommo.com', 'Trương Khuynh Hàn', 'ADMIN', 'skin_yasou');

INSERT INTO wallet (user_id, gold, echo_coin, diamonds, wood, dried_wood, cold_wood, strange_wood, coal, copper_ore, iron_ore, platinum, fish, shark, unknown_material)
VALUES (1, 999999, 100.5000, 0, 1000, 100, 100, 50, 1000, 500, 500, 100, 200, 10, 1);

INSERT INTO characters (char_id, user_id, name, level, str, vit, agi, int_stat, current_hp, max_hp, base_atk, base_def)
VALUES (1, 1, 'Ghét Hải Sản', 99, 999, 999, 999, 999, 99999, 99999, 9999, 9999);

-- 5. TRANG BỊ CHO ADMIN
INSERT INTO user_items (char_id, item_id, quantity, is_equipped, rarity, enhance_level, is_mythic, mythic_level, main_stat_type, main_stat_value, original_main_stat_value, sub_stats)
VALUES
    (1, 55, 1, TRUE, 'MYTHIC', 50, TRUE, 10, 'ATK_FLAT', 9999, 5000, '[{"code": "CRIT_RATE", "value": 50.0, "is_percent": true}]'),
    (1, 85, 1, TRUE, 'LEGENDARY', 20, FALSE, 0, 'DEF_FLAT', 500, 500, NULL);


-- 6. ENEMIES (EASY MODE - MAP 1-6)
INSERT INTO enemies (name, level, hp, atk, def, speed, exp_reward, gold_reward, image_url, drop_table) VALUES
-- Map 1
('Slime Xanh', 1, 60, 8, 0, 5, 15, 5, 'slime_green.png', '[]'),
('Thỏ Điên', 3, 100, 12, 1, 8, 25, 10, 'rabbit_mad.png', '[]'),
('Sói Hoang', 5, 180, 18, 3, 12, 45, 15, 'wolf.png', '[]'),
('Goblin Trinh Sát', 8, 300, 25, 5, 15, 80, 25, 'goblin_scout.png', '[]'),
-- Map 2
('Nhện Độc', 12, 500, 40, 10, 25, 120, 40, 'spider_poison.png', '[]'),
('Gấu Xám', 15, 800, 55, 20, 10, 180, 60, 'bear_grey.png', '[]'),
-- Map 3
('Bọ Cạp Cát', 22, 1200, 80, 30, 30, 300, 100, 'scorpion_sand.png', '[]'),
('Rắn Đuôi Chuông', 25, 1100, 95, 25, 45, 350, 120, 'snake_rattle.png', '[]'),
-- Map 6 Boss
('Lich King', 70, 50000, 800, 300, 80, 20000, 5000, 'lich_king.png', '[]');


-- 7. GIFT CODES
INSERT INTO gift_codes (code, type, gold_reward, coin_reward, max_usages, is_active) VALUES
    ('TEST_GOLD', 'NORMAL', 999999999, 0, 1000000, TRUE) ON DUPLICATE KEY UPDATE gold_reward = 999999;

INSERT INTO gift_codes (code, type, gold_reward, coin_reward, max_usages, is_active) VALUES
    ('TEST_COIN', 'NORMAL', 0, 999999, 1000000, TRUE) ON DUPLICATE KEY UPDATE coin_reward = 999999;

INSERT INTO gift_codes (code, type, gold_reward, coin_reward, max_usages, is_active) VALUES
    ('TEST_ITEM', 'DEV_TOOL', 0, 0, 1000000, TRUE) ON DUPLICATE KEY UPDATE type = 'DEV_TOOL';

-- ======================================================================================
-- KẾT THÚC
-- ======================================================================================