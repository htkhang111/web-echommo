USE echommo_db;

INSERT INTO users (username,password_hash,email,full_name,role)
VALUES ('admin','hash','admin@echommo.com','Game Master','ADMIN');

INSERT INTO wallet (user_id,gold,diamonds)
VALUES (1,9999999,999999);

INSERT INTO characters
(user_id,name,level,str,vit,agi,int_stat,current_hp,max_hp,base_atk,base_def)
VALUES
    (1,'ADMIN',99,999,999,999,999,99999,99999,9999,9999);

INSERT INTO items (name,description,type,slot_type,base_rarity,base_price,image_url) VALUES
                                                                                         ('Kiếm Gỗ','Kiếm tập luyện','WEAPON','WEAPON','COMMON',50,'sword_wood'),
                                                                                         ('Kiếm Sắt','Kiếm sắc bén','WEAPON','WEAPON','RARE',200,'sword_iron');

INSERT INTO user_items
(char_id,item_id,is_equipped,rarity,enhance_level,is_mythic,mythic_level,
 main_stat_type,main_stat_value,original_main_stat_value,sub_stats)
VALUES
    (1,2,TRUE,'MYTHIC',30,TRUE,5,'ATK_FLAT',550,500,
     '[{\"code\":\"CRIT_RATE\",\"value\":15,\"is_percent\":true},
       {\"code\":\"CRIT_DMG\",\"value\":30,\"is_percent\":true}]');

SELECT 'SEED CORE DONE' AS status;
