//package com.echommo.service.impl;
//
//import com.echommo.entity.*;
//import com.echommo.entity.Character;
//import com.echommo.enums.Rarity;
//import com.echommo.enums.SlotType;
//import com.echommo.repository.*;
//import com.echommo.service.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class InventoryServiceImpl implements InventoryService {
//
//    private final UserItemRepository userItemRepo;
//    private final ItemRepository itemRepo;
//    private final CharacterRepository charRepo;
//    private final WalletRepository walletRepo;
//    private final UserRepository userRepo;
//    private final EquipmentService equipmentService;
//    private final CharacterService characterService;
//    private final ItemGenerationService itemGenService;
//
//    @Override
//    public List<UserItem> getInventory(Integer charId) {
//        return userItemRepo.findByCharacter_CharIdOrderByAcquiredAtDesc(charId);
//    }
//
//    @Override
//    @Transactional
//    public void equipItem(Integer charId, Long userItemId) {
//        Character character = charRepo.findById(charId)
//                .orElseThrow(() -> new RuntimeException("Character not found"));
//
//        UserItem newItem = userItemRepo.findById(userItemId)
//                .orElseThrow(() -> new RuntimeException("Item not found"));
//
//        if (!newItem.getCharacter().getCharId().equals(charId)) {
//            throw new RuntimeException("Vật phẩm không thuộc về bạn!");
//        }
//
//        Item itemBase = newItem.getItem();
//
//        // 1. Check Loại (Gear hoặc Tool)
//        boolean isGear = List.of("WEAPON", "ARMOR", "HELMET", "BOOTS", "RING", "NECKLACE").contains(itemBase.getType());
//        boolean isTool = "TOOL".equals(itemBase.getType());
//
//        if (!isGear && !isTool) {
//            throw new RuntimeException("Vật phẩm này không thể trang bị!");
//        }
//
//        // 2. [FIX] Check Cấp Độ Yêu Cầu (Thiên Đạo Cấm Chế)
//        int requiredLv = getRequiredLevel(itemBase.getTier());
//        if (character.getLevel() < requiredLv) {
//            throw new RuntimeException("Cấp độ không đủ! Cần đạt Level " + requiredLv + " để trang bị.");
//        }
//
//        // 3. Tháo đồ cũ cùng slot
//        SlotType newSlot = itemBase.getSlotType();
//        List<UserItem> equippedItems = userItemRepo.findByCharacter_CharIdAndIsEquippedTrue(charId);
//
//        for (UserItem equipped : equippedItems) {
//            if (equipped.getItem().getSlotType() == newSlot) {
//                equipped.setIsEquipped(false);
//                userItemRepo.save(equipped);
//            }
//        }
//
//        // 4. Mặc đồ mới
//        newItem.setIsEquipped(true);
//        userItemRepo.save(newItem);
//
//        // 5. Tính lại chỉ số
//        characterService.recalculateStats(character);
//        charRepo.save(character);
//    }
//
//    // [HELPER] CÔNG THỨC TÍNH LEVEL YÊU CẦU
//    private int getRequiredLevel(Integer tier) {
//        if (tier == null || tier <= 1) return 1;
//        switch (tier) {
//            case 2: return 10;
//            case 3: return 20;
//            case 4: return 30; // Epic
//            case 5: return 50; // Legendary
//            default: return (tier - 1) * 10;
//        }
//    }
//
//    @Override
//    @Transactional
//    public void unequipItem(Integer charId, Long userItemId) {
//        Character character = charRepo.findById(charId).orElseThrow();
//        UserItem item = userItemRepo.findById(userItemId).orElseThrow();
//
//        if (!item.getCharacter().getCharId().equals(charId)) {
//            throw new RuntimeException("Vật phẩm không thuộc về bạn!");
//        }
//        if (!Boolean.TRUE.equals(item.getIsEquipped())) {
//            throw new RuntimeException("Vật phẩm này chưa được trang bị!");
//        }
//
//        item.setIsEquipped(false);
//        userItemRepo.save(item);
//
//        characterService.recalculateStats(character);
//        charRepo.save(character);
//    }
//
//    @Override
//    @Transactional
//    public UserItem enhanceItem(Integer charId, Long userItemId) {
//        return equipmentService.enhanceItem(userItemId);
//    }
//
//    @Override
//    @Transactional
//    public UserItem repairItem(User user, Long userItemId) {
//        UserItem userItem = userItemRepo.findById(userItemId)
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm!"));
//
//        if (!userItem.getUser().getUserId().equals(user.getUserId())) {
//            throw new RuntimeException("Vật phẩm không thuộc về bạn!");
//        }
//
//        Integer current = userItem.getCurrentDurability();
//        Integer max = userItem.getMaxDurability();
//
//        if (current == null) current = 0;
//        if (max == null || max <= 0) max = 100;
//
//        if (current >= max) {
//            throw new RuntimeException("Độ bền đã đầy, không cần sửa!");
//        }
//
//        int missingDurability = max - current;
//
//        // 1 Echo Coin = 10 Durability
//        double costValue = Math.ceil(missingDurability / 10.0);
//        BigDecimal cost = BigDecimal.valueOf(costValue);
//
//        if (cost.compareTo(BigDecimal.ONE) < 0) cost = BigDecimal.ONE;
//
//        Wallet wallet = user.getWallet();
//        if (wallet.getEchoCoin().compareTo(cost) < 0) {
//            throw new RuntimeException("Không đủ Echo Coin! Cần " + cost + " để sửa.");
//        }
//
//        wallet.setEchoCoin(wallet.getEchoCoin().subtract(cost));
//        walletRepo.save(wallet);
//
//        userItem.setCurrentDurability(max);
//        return userItemRepo.save(userItem);
//    }
//
//    @Override
//    @Transactional
//    public User expandInventory(User user) {
//        int currentSlots = user.getInventorySlots() != null ? user.getInventorySlots() : 50;
//        if (currentSlots >= 200) throw new RuntimeException("Kho đồ đã đạt giới hạn!");
//
//        int nextSlots = currentSlots + 5;
//        int costInt = ((currentSlots - 50) / 5) + 1;
//        BigDecimal cost = BigDecimal.valueOf(costInt);
//
//        Wallet w = user.getWallet();
//        if (w.getEchoCoin().compareTo(cost) < 0) {
//            throw new RuntimeException("Thiếu Echo Coin! Cần " + cost);
//        }
//
//        w.setEchoCoin(w.getEchoCoin().subtract(cost));
//        walletRepo.save(w);
//
//        user.setInventorySlots(nextSlots);
//        return userRepo.save(user);
//    }
//
//    @Override
//    @Transactional
//    public void addItemToInventory(User user, Integer itemId, int quantity) {
//        Character character = charRepo.findByUser(user)
//                .orElseThrow(() -> new RuntimeException("Character not found"));
//
//        Item item = itemRepo.findById(itemId)
//                .orElseThrow(() -> new RuntimeException("Item not found"));
//
//        int currentCount = userItemRepo.countByCharacter_CharId(character.getCharId());
//        int maxSlots = user.getInventorySlots() != null ? user.getInventorySlots() : 50;
//
//        boolean isStackable = List.of("MATERIAL", "CONSUMABLE").contains(item.getType());
//        if (!isStackable && currentCount + quantity > maxSlots) {
//            throw new RuntimeException("Kho đồ đã đầy!");
//        }
//
//        if (isStackable) {
//            Optional<UserItem> existingItem = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), itemId)
//                    .stream().filter(ui -> !Boolean.TRUE.equals(ui.getIsEquipped())).findFirst();
//
//            if (existingItem.isPresent()) {
//                UserItem ui = existingItem.get();
//                ui.setQuantity(ui.getQuantity() + quantity);
//                userItemRepo.save(ui);
//                return;
//            }
//        }
//
//        for (int i = 0; i < quantity; i++) {
//            UserItem ui = UserItem.builder()
//                    .character(character)
//                    .item(item)
//                    .quantity(1)
//                    .isEquipped(false)
//                    .enhanceLevel(0)
//                    .rarity(item.getRarity() != null ? item.getRarity() : Rarity.COMMON)
//                    .acquiredAt(LocalDateTime.now())
//                    .mainStatValue(BigDecimal.valueOf(item.getBaseMainStat() != null ? item.getBaseMainStat() : 0))
//                    .maxDurability(item.getMaxDurability() != null ? item.getMaxDurability() : 100)
//                    .currentDurability(item.getMaxDurability() != null ? item.getMaxDurability() : 100)
//                    .build();
//
//            if (List.of("WEAPON", "ARMOR", "TOOL").contains(item.getType())) {
//                itemGenService.randomizeNewItem(ui);
//            } else {
//                ui.setSubStats("[]");
//            }
//            userItemRepo.save(ui);
//        }
//    }
//}

//package com.echommo.service.impl;
//
//import com.echommo.entity.*;
//import com.echommo.entity.Character;
//import com.echommo.enums.Rarity;
//import com.echommo.enums.SlotType;
//import com.echommo.repository.*;
//import com.echommo.service.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class InventoryServiceImpl implements InventoryService {
//
//    private final UserItemRepository userItemRepo;
//    private final ItemRepository itemRepo;
//    private final CharacterRepository charRepo;
//    private final WalletRepository walletRepo;
//    private final UserRepository userRepo;
//    private final EquipmentService equipmentService;
//    private final CharacterService characterService;
//    private final ItemGenerationService itemGenService;
//
//    @Override
//    public List<UserItem> getInventory(Integer charId) {
//        return userItemRepo.findByCharacter_CharIdOrderByAcquiredAtDesc(charId);
//    }
//
//    @Override
//    @Transactional
//    public void equipItem(Integer charId, Long userItemId) {
//        Character character = charRepo.findById(charId)
//                .orElseThrow(() -> new RuntimeException("Character not found"));
//
//        UserItem newItem = userItemRepo.findById(userItemId)
//                .orElseThrow(() -> new RuntimeException("Item not found"));
//
//        if (!newItem.getCharacter().getCharId().equals(charId)) {
//            throw new RuntimeException("Vật phẩm không thuộc về bạn!");
//        }
//
//        Item itemBase = newItem.getItem();
//
//        boolean isGear = List.of("WEAPON", "ARMOR", "HELMET", "BOOTS", "RING", "NECKLACE").contains(itemBase.getType());
//        boolean isTool = "TOOL".equals(itemBase.getType());
//
//        if (!isGear && !isTool) {
//            throw new RuntimeException("Vật phẩm này không thể trang bị!");
//        }
//
//        int requiredLv = getRequiredLevel(itemBase.getTier());
//        if (character.getLevel() < requiredLv) {
//            throw new RuntimeException("Cấp độ không đủ! Cần đạt Level " + requiredLv + " để trang bị.");
//        }
//
//        SlotType newSlot = itemBase.getSlotType();
//        List<UserItem> equippedItems = userItemRepo.findByCharacter_CharIdAndIsEquippedTrue(charId);
//
//        for (UserItem equipped : equippedItems) {
//            if (equipped.getItem().getSlotType() == newSlot) {
//                equipped.setIsEquipped(false);
//                userItemRepo.save(equipped);
//            }
//        }
//
//        newItem.setIsEquipped(true);
//        userItemRepo.save(newItem);
//
//        characterService.recalculateStats(character);
//        charRepo.save(character);
//    }
//
//    private int getRequiredLevel(Integer tier) {
//        if (tier == null || tier <= 1) return 1;
//        switch (tier) {
//            case 2: return 10;
//            case 3: return 20;
//            case 4: return 30;
//            case 5: return 50;
//            default: return (tier - 1) * 10;
//        }
//    }
//
//    @Override
//    @Transactional
//    public void unequipItem(Integer charId, Long userItemId) {
//        Character character = charRepo.findById(charId).orElseThrow();
//        UserItem item = userItemRepo.findById(userItemId).orElseThrow();
//
//        if (!item.getCharacter().getCharId().equals(charId)) {
//            throw new RuntimeException("Vật phẩm không thuộc về bạn!");
//        }
//        if (!Boolean.TRUE.equals(item.getIsEquipped())) {
//            throw new RuntimeException("Vật phẩm này chưa được trang bị!");
//        }
//
//        item.setIsEquipped(false);
//        userItemRepo.save(item);
//
//        characterService.recalculateStats(character);
//        charRepo.save(character);
//    }
//
//    @Override
//    @Transactional
//    public UserItem enhanceItem(Integer charId, Long userItemId) {
//        return equipmentService.enhanceItem(userItemId);
//    }
//
//    @Override
//    @Transactional
//    public UserItem repairItem(User user, Long userItemId) {
//        UserItem userItem = userItemRepo.findById(userItemId)
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm!"));
//
//        if (!userItem.getUser().getUserId().equals(user.getUserId())) {
//            throw new RuntimeException("Vật phẩm không thuộc về bạn!");
//        }
//
//        Integer current = userItem.getCurrentDurability();
//        Integer max = userItem.getMaxDurability();
//
//        if (current == null) current = 0;
//        if (max == null || max <= 0) max = 100;
//
//        if (current >= max) {
//            throw new RuntimeException("Độ bền đã đầy, không cần sửa!");
//        }
//
//        int missingDurability = max - current;
//        double costValue = Math.ceil(missingDurability / 10.0);
//        BigDecimal cost = BigDecimal.valueOf(costValue);
//
//        if (cost.compareTo(BigDecimal.ONE) < 0) cost = BigDecimal.ONE;
//
//        Wallet wallet = user.getWallet();
//        if (wallet.getEchoCoin().compareTo(cost) < 0) {
//            throw new RuntimeException("Không đủ Echo Coin! Cần " + cost + " để sửa.");
//        }
//
//        wallet.setEchoCoin(wallet.getEchoCoin().subtract(cost));
//        walletRepo.save(wallet);
//
//        userItem.setCurrentDurability(max);
//        return userItemRepo.save(userItem);
//    }
//
//    @Override
//    @Transactional
//    public User expandInventory(User user) {
//        // [LOGIC MỚI] Hệ số 7
//        int currentSlots = user.getInventorySlots() != null ? user.getInventorySlots() : 49;
//
//        if (currentSlots >= 210) throw new RuntimeException("Kho đồ đã đạt giới hạn tối đa!");
//
//        int nextSlots = currentSlots + 7;
//
//        // Phí: ((current - 49) / 7) + 1
//        int costInt = ((currentSlots - 49) / 7) + 1;
//        if (costInt < 1) costInt = 1;
//
//        BigDecimal cost = BigDecimal.valueOf(costInt);
//
//        Wallet w = user.getWallet();
//        if (w.getEchoCoin().compareTo(cost) < 0) {
//            throw new RuntimeException("Thiếu Echo Coin! Cần " + cost);
//        }
//
//        w.setEchoCoin(w.getEchoCoin().subtract(cost));
//        walletRepo.save(w);
//
//        user.setInventorySlots(nextSlots);
//        return userRepo.save(user);
//    }
//
//    @Override
//    @Transactional
//    public void addItemToInventory(User user, Integer itemId, int quantity) {
//        Character character = charRepo.findByUser(user)
//                .orElseThrow(() -> new RuntimeException("Character not found"));
//
//        Item item = itemRepo.findById(itemId)
//                .orElseThrow(() -> new RuntimeException("Item not found"));
//
//        int currentCount = userItemRepo.countByCharacter_CharId(character.getCharId());
//
//        // [FIX] Mặc định check theo 49 ô
//        int maxSlots = user.getInventorySlots() != null ? user.getInventorySlots() : 49;
//
//        boolean isStackable = List.of("MATERIAL", "CONSUMABLE").contains(item.getType());
//        if (!isStackable && currentCount + quantity > maxSlots) {
//            throw new RuntimeException("Kho đồ đã đầy!");
//        }
//
//        if (isStackable) {
//            Optional<UserItem> existingItem = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), itemId)
//                    .stream().filter(ui -> !Boolean.TRUE.equals(ui.getIsEquipped())).findFirst();
//
//            if (existingItem.isPresent()) {
//                UserItem ui = existingItem.get();
//                ui.setQuantity(ui.getQuantity() + quantity);
//                userItemRepo.save(ui);
//                return;
//            }
//        }
//
//        for (int i = 0; i < quantity; i++) {
//            // [LOGIC MỚI] Lấy loại chỉ số chuẩn từ helper (Dữ liệu thật)
//            String defaultStatType = getDefaultStatType(item.getType());
//
//            UserItem ui = UserItem.builder()
//                    .character(character)
//                    .item(item)
//                    .quantity(1)
//                    .isEquipped(false)
//                    .enhanceLevel(0)
//                    .rarity(item.getRarity() != null ? item.getRarity() : Rarity.COMMON)
//                    .acquiredAt(LocalDateTime.now())
//                    // Gán giá trị chỉ số chính
//                    .mainStatValue(BigDecimal.valueOf(item.getBaseMainStat() != null ? item.getBaseMainStat() : 0))
//                    // Gán loại chỉ số chính (quan trọng để hiển thị đúng ở Frontend)
//                    .mainStatType(defaultStatType)
//
//                    .maxDurability(item.getMaxDurability() != null ? item.getMaxDurability() : 100)
//                    .currentDurability(item.getMaxDurability() != null ? item.getMaxDurability() : 100)
//                    .build();
//
//            // Nếu là trang bị, có thể random thêm
//            if (List.of("WEAPON", "ARMOR", "TOOL", "HELMET", "BOOTS", "RING", "NECKLACE").contains(item.getType())) {
//                itemGenService.randomizeNewItem(ui);
//            } else {
//                ui.setSubStats("[]");
//            }
//            userItemRepo.save(ui);
//        }
//    }
//
//    // Hàm helper xác định chỉ số mặc định (Dữ liệu thật)
//    private String getDefaultStatType(String itemType) {
//        if (itemType == null) return null;
//        switch (itemType) {
//            case "WEAPON": return "ATK_FLAT";      // Kiếm -> Công Lực
//            case "ARMOR":                          // Giáp -> Hộ Thể
//            case "HELMET":
//            case "BOOTS": return "DEF_FLAT";
//            case "RING": return "CRIT_RATE";       // Nhẫn -> Bạo Kích
//            case "NECKLACE": return "HP_FLAT";     // Dây Chuyền -> Sinh Lực
//            case "TOOL": return "DURABILITY";      // Công cụ -> Độ Bền
//            default: return null;
//        }
//    }
//}


package com.echommo.service.impl;

import com.echommo.entity.*;
import com.echommo.entity.Character;
import com.echommo.enums.Rarity;
import com.echommo.enums.SlotType;
import com.echommo.repository.*;
import com.echommo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final UserItemRepository userItemRepo;
    private final ItemRepository itemRepo;
    private final CharacterRepository charRepo;
    private final WalletRepository walletRepo;
    private final UserRepository userRepo;
    private final EquipmentService equipmentService;
    private final CharacterService characterService;
    private final ItemGenerationService itemGenService;

    @Override
    public List<UserItem> getInventory(Integer charId) {
        return userItemRepo.findByCharacter_CharIdOrderByAcquiredAtDesc(charId);
    }

    @Override
    @Transactional
    public void equipItem(Integer charId, Long userItemId) {
        Character character = charRepo.findById(charId)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        UserItem newItem = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!newItem.getCharacter().getCharId().equals(charId)) {
            throw new RuntimeException("Vật phẩm không thuộc về bạn!");
        }

        Item itemBase = newItem.getItem();

        boolean isGear = List.of("WEAPON", "ARMOR", "HELMET", "BOOTS", "RING", "NECKLACE").contains(itemBase.getType());
        boolean isTool = "TOOL".equals(itemBase.getType());

        if (!isGear && !isTool) {
            throw new RuntimeException("Vật phẩm này không thể trang bị!");
        }

        int requiredLv = getRequiredLevel(itemBase.getTier());
        if (character.getLevel() < requiredLv) {
            throw new RuntimeException("Cấp độ không đủ! Cần đạt Level " + requiredLv + " để trang bị.");
        }

        SlotType newSlot = itemBase.getSlotType();
        List<UserItem> equippedItems = userItemRepo.findByCharacter_CharIdAndIsEquippedTrue(charId);

        for (UserItem equipped : equippedItems) {
            if (equipped.getItem().getSlotType() == newSlot) {
                equipped.setIsEquipped(false);
                userItemRepo.save(equipped);
            }
        }

        newItem.setIsEquipped(true);
        userItemRepo.save(newItem);

        characterService.recalculateStats(character);
        charRepo.save(character);
    }

    private int getRequiredLevel(Integer tier) {
        if (tier == null || tier <= 1) return 1;
        switch (tier) {
            case 2: return 10;
            case 3: return 20;
            case 4: return 30;
            case 5: return 50;
            default: return (tier - 1) * 10;
        }
    }

    @Override
    @Transactional
    public void unequipItem(Integer charId, Long userItemId) {
        Character character = charRepo.findById(charId).orElseThrow();
        UserItem item = userItemRepo.findById(userItemId).orElseThrow();

        if (!item.getCharacter().getCharId().equals(charId)) {
            throw new RuntimeException("Vật phẩm không thuộc về bạn!");
        }
        if (!Boolean.TRUE.equals(item.getIsEquipped())) {
            throw new RuntimeException("Vật phẩm này chưa được trang bị!");
        }

        item.setIsEquipped(false);
        userItemRepo.save(item);

        characterService.recalculateStats(character);
        charRepo.save(character);
    }

    @Override
    @Transactional
    public UserItem enhanceItem(Integer charId, Long userItemId) {
        return equipmentService.enhanceItem(userItemId);
    }

    @Override
    @Transactional
    public UserItem repairItem(User user, Long userItemId) {
        UserItem userItem = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm!"));

        // Check sở hữu
        if (!userItem.getCharacter().getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Vật phẩm không thuộc về bạn!");
        }

        Integer current = userItem.getCurrentDurability();
        Integer max = userItem.getMaxDurability();

        if (current == null) current = 0;
        if (max == null || max <= 0) {
            throw new RuntimeException("Vật phẩm này không có độ bền để sửa!");
        }

        if (current >= max) {
            throw new RuntimeException("Độ bền đã đầy, không cần sửa!");
        }

        int missing = max - current;

        // --- [FIXED] TÍNH PHÍ SỬA CHỮA ---
        long goldCostPerPoint = 10;
        BigDecimal coinCostPerPoint = BigDecimal.ZERO;

        // Lấy phẩm chất (Ưu tiên UserItem -> Item gốc)
        Rarity rarity = userItem.getRarity();
        if (rarity == null) {
            rarity = userItem.getItem().getRarity();
        }
        if (rarity == null) rarity = Rarity.COMMON;

        // Nếu là Mythic -> Tính giá Tiên Phẩm
        if (Boolean.TRUE.equals(userItem.getIsMythic())) {
            rarity = Rarity.MYTHIC;
        }

        switch (rarity) {
            case COMMON:
            case UNCOMMON:
                // Phàm Phẩm: 10 Vàng
                goldCostPerPoint = 10;
                coinCostPerPoint = BigDecimal.ZERO;
                break;
            case RARE:
                // Lương Phẩm: 50 Vàng
                goldCostPerPoint = 50;
                coinCostPerPoint = BigDecimal.ZERO;
                break;
            case EPIC:
                // Linh Phẩm: 200 Vàng + 0.1 Coin
                goldCostPerPoint = 200;
                coinCostPerPoint = BigDecimal.valueOf(0.1);
                break;
            case LEGENDARY:
            case MYTHIC:
                // Tiên Phẩm: 1000 Vàng + 1.0 Coin
                goldCostPerPoint = 1000;
                coinCostPerPoint = BigDecimal.valueOf(1.0);
                break;
            default:
                goldCostPerPoint = 10;
        }

        // [FIXED] Chuyển đổi sang BigDecimal để tính toán tiền chính xác
        BigDecimal totalGoldCost = BigDecimal.valueOf(missing * goldCostPerPoint);
        BigDecimal totalCoinCost = coinCostPerPoint.multiply(BigDecimal.valueOf(missing));

        // [FIXED] Lấy ví từ User entity (tránh lỗi findByUserId không tồn tại)
        Wallet wallet = user.getWallet();
        if (wallet == null) {
            // Fallback: Tìm lại ví nếu user object chưa load đủ (phòng hờ)
            wallet = walletRepo.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ví tiền!"));
        }

        // [FIXED] So sánh BigDecimal chuẩn
        if (wallet.getGold().compareTo(totalGoldCost) < 0) {
            throw new RuntimeException("Không đủ Vàng để sửa! Cần: " + totalGoldCost);
        }
        if (wallet.getEchoCoin().compareTo(totalCoinCost) < 0) {
            throw new RuntimeException("Không đủ Echo Coin để sửa! Cần: " + totalCoinCost);
        }

        // [FIXED] Trừ tiền
        wallet.setGold(wallet.getGold().subtract(totalGoldCost));
        wallet.setEchoCoin(wallet.getEchoCoin().subtract(totalCoinCost));
        walletRepo.save(wallet);

        // Hồi phục độ bền
        userItem.setCurrentDurability(max);
        return userItemRepo.save(userItem);
    }

    @Override
    @Transactional
    public User expandInventory(User user) {
        int currentSlots = user.getInventorySlots() != null ? user.getInventorySlots() : 49;
        if (currentSlots >= 210) throw new RuntimeException("Kho đồ đã đạt giới hạn tối đa!");

        int nextSlots = currentSlots + 7;

        // Phí: ((current - 49) / 7) + 1
        int costInt = ((currentSlots - 49) / 7) + 1;
        if (costInt < 1) costInt = 1;
        BigDecimal cost = BigDecimal.valueOf(costInt);

        Wallet w = user.getWallet();
        if (w.getEchoCoin().compareTo(cost) < 0) {
            throw new RuntimeException("Thiếu Echo Coin! Cần " + cost);
        }

        w.setEchoCoin(w.getEchoCoin().subtract(cost));
        walletRepo.save(w);

        user.setInventorySlots(nextSlots);
        return userRepo.save(user);
    }

    @Override
    @Transactional
    public void addItemToInventory(User user, Integer itemId, int quantity) {
        Character character = charRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        Item item = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        int currentCount = userItemRepo.countByCharacter_CharId(character.getCharId());
        int maxSlots = user.getInventorySlots() != null ? user.getInventorySlots() : 49;

        boolean isStackable = List.of("MATERIAL", "CONSUMABLE").contains(item.getType());
        if (!isStackable && currentCount + quantity > maxSlots) {
            throw new RuntimeException("Kho đồ đã đầy!");
        }

        if (isStackable) {
            Optional<UserItem> existingItem = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), itemId)
                    .stream().filter(ui -> !Boolean.TRUE.equals(ui.getIsEquipped())).findFirst();

            if (existingItem.isPresent()) {
                UserItem ui = existingItem.get();
                ui.setQuantity(ui.getQuantity() + quantity);
                userItemRepo.save(ui);
                return;
            }
        }

        for (int i = 0; i < quantity; i++) {
            String defaultStatType = getDefaultStatType(item.getType());

            UserItem ui = UserItem.builder()
                    .character(character)
                    .item(item)
                    .quantity(1)
                    .isEquipped(false)
                    .enhanceLevel(0)
                    .rarity(item.getRarity() != null ? item.getRarity() : Rarity.COMMON)
                    .acquiredAt(LocalDateTime.now())
                    .mainStatValue(BigDecimal.valueOf(item.getBaseMainStat() != null ? item.getBaseMainStat() : 0))
                    .mainStatType(defaultStatType)
                    .maxDurability(item.getMaxDurability() != null ? item.getMaxDurability() : 100)
                    .currentDurability(item.getMaxDurability() != null ? item.getMaxDurability() : 100)
                    .build();

            if (List.of("WEAPON", "ARMOR", "TOOL", "HELMET", "BOOTS", "RING", "NECKLACE").contains(item.getType())) {
                itemGenService.randomizeNewItem(ui);
            } else {
                ui.setSubStats("[]");
            }
            userItemRepo.save(ui);
        }
    }

    private String getDefaultStatType(String itemType) {
        if (itemType == null) return null;
        switch (itemType) {
            case "WEAPON": return "ATK_FLAT";
            case "ARMOR":
            case "HELMET":
            case "BOOTS": return "DEF_FLAT";
            case "RING": return "CRIT_RATE";
            case "NECKLACE": return "HP_FLAT";
            case "TOOL": return "DURABILITY";
            default: return null;
        }
    }
}


