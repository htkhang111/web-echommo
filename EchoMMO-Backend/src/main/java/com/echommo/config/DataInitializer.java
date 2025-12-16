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
    @Autowired private CharacterRepository charRepo; // [NEW] Inject thêm Repo này

    @Override
    public void run(String... args) throws Exception {
        createMissingItems();
        healAllCharacters(); // [NEW] Chạy hàm fix lỗi nhân vật
    }

    // [FIX] Hàm tự động hồi phục cho nhân vật bị lỗi 0 máu/energy
    private void healAllCharacters() {
        System.out.println(">>> Checking & Healing characters...");
        List<Character> chars = charRepo.findAll();
        for (Character c : chars) {
            boolean changed = false;
            // Nếu Max HP/Energy chưa set hoặc bằng 0 -> Set mặc định
            if (c.getMaxHp() == null || c.getMaxHp() == 0) { c.setMaxHp(100); changed = true; }
            if (c.getMaxEnergy() == null || c.getMaxEnergy() == 0) { c.setMaxEnergy(50); changed = true; }

            // Nếu Current HP/Energy đang 0 hoặc null -> Hồi đầy
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
        // ... (Giữ nguyên code tạo item cũ của bạn ở đây) ...
        createIfMissing("Gỗ", "MATERIAL", Rarity.COMMON, 1, "r_go.png", SlotType.NONE);
        createIfMissing("Đá", "MATERIAL", Rarity.COMMON, 1, "stone_1.png", SlotType.NONE);
        createIfMissing("Quặng Đồng", "MATERIAL", Rarity.COMMON, 1, "r_copper_node.png", SlotType.NONE);
        createIfMissing("Sắt", "MATERIAL", Rarity.RARE, 2, "r_silver_node.png", SlotType.NONE);
        createIfMissing("Cá", "MATERIAL", Rarity.COMMON, 1, "r_sliver_coin.png", SlotType.NONE);
        createIfMissing("Bạch Kim", "MATERIAL", Rarity.EPIC, 3, "r_mystrile_node.png", SlotType.NONE);
        createIfMissing("Gỗ Khô", "MATERIAL", Rarity.COMMON, 1, "r_go.png", SlotType.NONE);
        createIfMissing("Gỗ Lạnh", "MATERIAL", Rarity.UNCOMMON, 2, "r_go.png", SlotType.NONE);
        createIfMissing("Gỗ Hóa Thạch", "MATERIAL", Rarity.RARE, 3, "r_gohoathach.png", SlotType.NONE);
        createIfMissing("Cá Độc", "MATERIAL", Rarity.UNCOMMON, 2, "r_sliver_coin.png", SlotType.NONE);
        createIfMissing("Kim Cương", "MATERIAL", Rarity.LEGENDARY, 5, "r_diamond.png", SlotType.NONE);
        createIfMissing("Nguyên liệu lạ", "MATERIAL", Rarity.EPIC, 4, "r_unknown.png", SlotType.NONE);
        createIfMissing("Kiếm Gỗ", "WEAPON", Rarity.COMMON, 1, "s_sword_0.png", SlotType.WEAPON);
        createIfMissing("Áo Vải", "ARMOR", Rarity.COMMON, 1, "a_armor_0.png", SlotType.ARMOR);
        createIfMissing("Bình Máu Nhỏ", "CONSUMABLE", Rarity.COMMON, 1, "r_sliver_coin.png", SlotType.NONE);
    }

    private void createIfMissing(String name, String type, Rarity rarity, int tier, String img, SlotType slot) {
        if (itemRepo.findByName(name).isPresent()) return;
        Item item = new Item();
        item.setName(name); item.setType(type); item.setRarity(rarity); item.setTier(tier);
        item.setImageUrl(img); item.setSlotType(slot); item.setDescription("Vật phẩm hệ thống.");
        itemRepo.save(item);
        System.out.println(">>> Đã bổ sung item: " + name);
    }
}