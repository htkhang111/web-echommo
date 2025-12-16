package com.echommo.config;

import com.echommo.entity.Item;
import com.echommo.enums.Rarity;
import com.echommo.enums.SlotType;
import com.echommo.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private ItemRepository itemRepo;

    @Override
    public void run(String... args) throws Exception {
        // Chỉ chạy logic tạo Item, KHÔNG tạo Admin (Admin do SQL quản lý)
        createDefaultItems();
    }

    private void createDefaultItems() {
        System.out.println(">>> Đang kiểm tra và đồng bộ vật phẩm...");

        // --- MAP 1 & 2 (Cơ bản) ---
        createItem("Gỗ", "MATERIAL", Rarity.COMMON, 1, "r_go.png", SlotType.NONE);
        createItem("Đá", "MATERIAL", Rarity.COMMON, 1, "stone_1.png", SlotType.NONE);
        createItem("Quặng Đồng", "MATERIAL", Rarity.COMMON, 1, "r_copper_node.png", SlotType.NONE);
        createItem("Sắt", "MATERIAL", Rarity.RARE, 2, "r_silver_node.png", SlotType.NONE);
        createItem("Cá", "MATERIAL", Rarity.COMMON, 1, "r_sliver_coin.png", SlotType.NONE);
        createItem("Bạch Kim", "MATERIAL", Rarity.EPIC, 3, "r_mystrile_node.png", SlotType.NONE);

        // --- MAP 3 -> 6 (Mới) ---
        createItem("Gỗ Khô", "MATERIAL", Rarity.COMMON, 1, "r_go.png", SlotType.NONE);
        createItem("Gỗ Lạnh", "MATERIAL", Rarity.UNCOMMON, 2, "r_go.png", SlotType.NONE);
        createItem("Gỗ Hóa Thạch", "MATERIAL", Rarity.RARE, 3, "r_gohoathach.png", SlotType.NONE);
        createItem("Cá Độc", "MATERIAL", Rarity.UNCOMMON, 2, "r_sliver_coin.png", SlotType.NONE); // Dùng tạm icon bạc
        createItem("Kim Cương", "MATERIAL", Rarity.LEGENDARY, 5, "r_diamond.png", SlotType.NONE);
        createItem("Nguyên liệu lạ", "MATERIAL", Rarity.EPIC, 4, "r_unknown.png", SlotType.NONE);

        // --- TRANG BỊ CƠ BẢN (Để test drop) ---
        createItem("Kiếm Gỗ", "WEAPON", Rarity.COMMON, 1, "s_sword_0.png", SlotType.WEAPON);
        createItem("Áo Vải", "ARMOR", Rarity.COMMON, 1, "a_armor_0.png", SlotType.ARMOR);
        createItem("Bình Máu Nhỏ", "CONSUMABLE", Rarity.COMMON, 1, "r_sliver_coin.png", SlotType.NONE);
    }

    private void createItem(String name, String type, Rarity rarity, int tier, String img, SlotType slot) {
        // Kiểm tra tồn tại để tránh trùng lặp
        if (itemRepo.findByName(name).isPresent()) return;

        Item item = new Item();
        item.setName(name);
        item.setType(type);
        item.setRarity(rarity);
        item.setTier(tier);
        item.setImageUrl(img);
        item.setSlotType(slot);
        item.setDescription("Vật phẩm hệ thống.");
        itemRepo.save(item);
        System.out.println(">>> Đã khởi tạo item: " + name);
    }
}