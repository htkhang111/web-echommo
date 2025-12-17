package com.echommo.config;

import com.echommo.entity.Character;
import com.echommo.entity.Item;
import com.echommo.enums.Rarity;
import com.echommo.enums.SlotType;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private ItemRepository itemRepo;
    @Autowired private CharacterRepository charRepo;

    @Override
    public void run(String... args) throws Exception {
        createMissingItems();
        healAllCharacters();
    }

    private void healAllCharacters() {
        System.out.println(">>> Checking & Healing characters...");
        List<Character> chars = charRepo.findAll();
        for (Character c : chars) {
            boolean changed = false;
            if (c.getMaxHp() == null || c.getMaxHp() == 0) { c.setMaxHp(100); changed = true; }
            if (c.getMaxEnergy() == null || c.getMaxEnergy() == 0) { c.setMaxEnergy(50); changed = true; }

            if (c.getCurrentHp() == null || c.getCurrentHp() <= 0) {
                c.setCurrentHp(c.getMaxHp());
                changed = true;
            }
            if (c.getCurrentEnergy() == null || c.getCurrentEnergy() <= 0) {
                c.setCurrentEnergy(c.getMaxEnergy());
                changed = true;
            }

            if (changed) {
                charRepo.save(c);
                System.out.println(">>> Đã hồi phục cho nhân vật: " + c.getName());
            }
        }
    }

    private void createMissingItems() {
        // [FIX QUAN TRỌNG] Gán đúng SlotType.MATERIAL cho nguyên liệu
        createIfMissing("Gỗ", "MATERIAL", Rarity.COMMON, 1, "r_go.png", SlotType.MATERIAL);
        createIfMissing("Đá", "MATERIAL", Rarity.COMMON, 1, "stone_1.png", SlotType.MATERIAL);
        createIfMissing("Quặng Đồng", "MATERIAL", Rarity.COMMON, 1, "r_copper_node.png", SlotType.MATERIAL);
        createIfMissing("Sắt", "MATERIAL", Rarity.RARE, 2, "r_silver_node.png", SlotType.MATERIAL);
        createIfMissing("Cá", "MATERIAL", Rarity.COMMON, 1, "r_sliver_coin.png", SlotType.MATERIAL);
        createIfMissing("Bạch Kim", "MATERIAL", Rarity.EPIC, 3, "r_mystrile_node.png", SlotType.MATERIAL);

        // Các loại gỗ khác
        createIfMissing("Gỗ Khô", "MATERIAL", Rarity.COMMON, 1, "r_go.png", SlotType.MATERIAL);
        createIfMissing("Gỗ Lạnh", "MATERIAL", Rarity.UNCOMMON, 2, "r_go.png", SlotType.MATERIAL);
        createIfMissing("Gỗ Hóa Thạch", "MATERIAL", Rarity.RARE, 3, "r_gohoathach.png", SlotType.MATERIAL);

        // Các loại khác
        createIfMissing("Cá Độc", "MATERIAL", Rarity.UNCOMMON, 2, "r_sliver_coin.png", SlotType.MATERIAL);
        createIfMissing("Kim Cương", "MATERIAL", Rarity.LEGENDARY, 5, "r_diamond.png", SlotType.MATERIAL);
        createIfMissing("Nguyên liệu lạ", "MATERIAL", Rarity.EPIC, 4, "r_unknown.png", SlotType.MATERIAL);

        // [FIX] Trang bị thì giữ nguyên SlotType chuẩn
        createIfMissing("Kiếm Gỗ", "WEAPON", Rarity.COMMON, 1, "s_sword_0.png", SlotType.WEAPON);
        createIfMissing("Áo Vải", "ARMOR", Rarity.COMMON, 1, "a_armor_0.png", SlotType.ARMOR);

        // [FIX QUAN TRỌNG] Gán SlotType.CONSUMABLE cho bình máu
        createIfMissing("Bình Máu Nhỏ", "CONSUMABLE", Rarity.COMMON, 1, "r_sliver_coin.png", SlotType.CONSUMABLE);
    }

    private void createIfMissing(String name, String type, Rarity rarity, int tier, String img, SlotType slot) {
        if (itemRepo.findByName(name).isPresent()) return;
        Item item = new Item();
        item.setName(name);
        item.setType(type); // Vẫn giữ field Type cũ cho an toàn
        item.setRarity(rarity);
        item.setTier(tier);
        item.setImageUrl(img);
        item.setSlotType(slot); // Lưu SlotType chuẩn vào DB
        item.setDescription("Vật phẩm hệ thống.");
        itemRepo.save(item);
        System.out.println(">>> Đã bổ sung item: " + name);
    }
}