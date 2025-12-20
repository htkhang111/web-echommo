//package com.echommo.service;
//
//import com.echommo.config.GameConstants;
//import com.echommo.dto.SubStatDTO;
//import com.echommo.entity.UserItem;
//import com.echommo.entity.Wallet;
//import com.echommo.enums.Rarity;
//import com.echommo.enums.SlotType;
//import com.echommo.repository.UserItemRepository;
//import com.echommo.repository.WalletRepository;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//
//@Service
//@RequiredArgsConstructor
//public class EquipmentService {
//
//    private final UserItemRepository userItemRepo;
//    private final WalletRepository walletRepo; // Inject Wallet để trừ tiền
//    private final ItemGenerationService itemGenService;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    private final Random random = new Random();
//
//    // --- HELPER: KIỂM TRA & TRỪ TÀI NGUYÊN ---
//    private void checkAndConsumeResources(UserItem targetItem, Map<Integer, Integer> materialCosts, int goldCost) {
//        Integer userId = targetItem.getCharacter().getUser().getUserId();
//        Integer charId = targetItem.getCharacter().getCharId();
//
//        // 1. Kiểm tra Vàng
//        Wallet wallet = walletRepo.findByUser_UserId(userId)
//                .orElseThrow(() -> new RuntimeException("Lỗi dữ liệu: Không tìm thấy ví tiền!"));
//
//        if (wallet.getGold().compareTo(BigDecimal.valueOf(goldCost)) < 0) {
//            throw new RuntimeException("Không đủ vàng! Cần " + goldCost + " G nhưng bạn chỉ có " + wallet.getGold() + " G.");
//        }
//
//        // 2. Kiểm tra Nguyên liệu (Chỉ check trước, chưa trừ)
//        Map<Integer, UserItem> foundItems = new HashMap<>();
//        for (Map.Entry<Integer, Integer> entry : materialCosts.entrySet()) {
//            int matId = entry.getKey();
//            int qtyNeeded = entry.getValue();
//
//            // Tìm trong kho đồ của nhân vật
//            UserItem userMat = userItemRepo.findByCharacter_CharIdAndItem_ItemId(charId, matId)
//                    .orElse(null);
//
//            if (userMat == null || userMat.getQuantity() < qtyNeeded) {
//                String currentQty = (userMat == null) ? "0" : String.valueOf(userMat.getQuantity());
//                throw new RuntimeException("Thiếu nguyên liệu (ID: " + matId + ")! Cần: " + qtyNeeded + ", Có: " + currentQty);
//            }
//            foundItems.put(matId, userMat);
//        }
//
//        // 3. Thực hiện trừ (Sau khi đã validate đủ hết)
//        // Trừ tiền
//        wallet.setGold(wallet.getGold().subtract(BigDecimal.valueOf(goldCost)));
//        walletRepo.save(wallet);
//
//        // Trừ nguyên liệu
//        for (Map.Entry<Integer, Integer> entry : materialCosts.entrySet()) {
//            int matId = entry.getKey();
//            int qtyNeeded = entry.getValue();
//            UserItem itemToReduce = foundItems.get(matId);
//
//            itemToReduce.setQuantity(itemToReduce.getQuantity() - qtyNeeded);
//
//            // Nếu hết thì xóa khỏi DB để sạch rác
//            if (itemToReduce.getQuantity() <= 0) {
//                userItemRepo.delete(itemToReduce);
//            } else {
//                userItemRepo.save(itemToReduce);
//            }
//        }
//    }
//
//    // --- 1. CƯỜNG HÓA THƯỜNG (Lv 1 -> 30) ---
//    @Transactional
//    public UserItem enhanceItem(Long userItemId) {
//        UserItem item = userItemRepo.findById(userItemId)
//                .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại"));
//
//        if (item.isMythic()) {
//            throw new RuntimeException("Vật phẩm này đã là Mythic, hãy dùng chức năng Nâng Cấp Mythic!");
//        }
//        if (item.getEnhanceLevel() >= 30) {
//            throw new RuntimeException("Đã đạt cấp tối đa (+30). Hãy Tiến Hóa lên Mythic!");
//        }
//
//        int nextLv = item.getEnhanceLevel() + 1;
//        SlotType type = item.getItem().getSlotType();
//
//        // --- CÔNG THỨC TIÊU HAO (RESOURCE SINK) ---
//        Map<Integer, Integer> materials = new HashMap<>();
//        int goldCost;
//
//        if (nextLv <= 10) {
//            // Tier 1: Sơ cấp (Đồng/Đá + Gỗ Sồi)
//            goldCost = nextLv * 1000;
//            // Số lượng: Cấp * 15 (Max 150 cái - khoảng 10 lần farm)
//            int mainQty = nextLv * 15;
//            int subQty = nextLv * 5;
//
//            // Vũ khí dùng Quặng, Giáp dùng Đá
//            if (type == SlotType.WEAPON) materials.put(GameConstants.MAT_ORE_COPPER, mainQty);
//            else materials.put(GameConstants.MAT_STONE, mainQty);
//
//            // Nguyên liệu phụ: Gỗ Sồi
//            materials.put(GameConstants.MAT_WOOD_OAK, subQty);
//
//        } else if (nextLv <= 20) {
//            // Tier 2: Trung cấp (Sắt + Gỗ Khô)
//            goldCost = nextLv * 3000;
//            int levelScale = nextLv - 10;
//            int mainQty = levelScale * 15;
//            int subQty = levelScale * 5;
//
//            materials.put(GameConstants.MAT_ORE_IRON, mainQty);
//            materials.put(GameConstants.MAT_WOOD_DRIED, subQty);
//
//        } else {
//            // Tier 3: Cao cấp (Bạch Kim + Gỗ Lạnh)
//            // Giai đoạn này tốn kém nhất để ép người chơi mua bán
//            goldCost = nextLv * 10000;
//            int levelScale = nextLv - 20;
//            int mainQty = levelScale * 20; // Tăng độ khó (Max 200 Bạch Kim/lần)
//            int subQty = levelScale * 10;
//
//            materials.put(GameConstants.MAT_ORE_PLATINUM, mainQty);
//            materials.put(GameConstants.MAT_WOOD_COLD, subQty);
//        }
//
//        // Kiểm tra và trừ nguyên liệu
//        checkAndConsumeResources(item, materials, goldCost);
//
//        // --- LOGIC CƯỜNG HÓA ---
//        item.setEnhanceLevel(nextLv);
//
//        // Nhảy dòng tại các mốc +3, +6, +9...
//        if (nextLv % 3 == 0) {
//            applySubStatRoll(item);
//        }
//
//        return userItemRepo.save(item);
//    }
//
//    // --- 2. TIẾN HÓA MYTHIC (EVOLVE) ---
//    @Transactional
//    public UserItem evolveToMythic(Long userItemId) {
//        UserItem item = userItemRepo.findById(userItemId)
//                .orElseThrow(() -> new RuntimeException("Item not found"));
//
//        if (item.isMythic()) throw new RuntimeException("Đã là Mythic rồi!");
//        if (item.getRarity() != Rarity.LEGENDARY) throw new RuntimeException("Chỉ đồ Legendary mới lên được Mythic");
//        if (item.getEnhanceLevel() < 30) throw new RuntimeException("Cần +30 để tiến hóa!");
//
//        // Chi phí Tiến Hóa (Cực đắt):
//        // 500 Gỗ Lạ + 200 Nguyên liệu lạ + 5 Echo Coin + 1 Triệu Vàng
//        Map<Integer, Integer> materials = new HashMap<>();
//        materials.put(GameConstants.MAT_WOOD_STRANGE, 500); // Farm map 6
//        materials.put(GameConstants.MAT_UNKNOWN, 200);      // Farm map 5-6
//        materials.put(GameConstants.MAT_ECHO_COIN, 5);      // Cực hiếm
//
//        checkAndConsumeResources(item, materials, 1000000);
//
//        // --- LOGIC BUFF CHỈ SỐ ---
//        // Nhân toàn bộ chỉ số hiện tại với hệ số random (1.005 -> 1.01)
//        double multiplier = randomRange(GameConstants.MYTHIC_EVOLUTION_RANGE[0], GameConstants.MYTHIC_EVOLUTION_RANGE[1]);
//
//        BigDecimal mainVal = item.getMainStatValue().multiply(BigDecimal.valueOf(multiplier));
//        item.setMainStatValue(mainVal);
//        item.setOriginalMainStatValue(mainVal); // Snapshot giá trị gốc Mythic
//
//        try {
//            List<SubStatDTO> stats = objectMapper.readValue(item.getSubStats(), new TypeReference<List<SubStatDTO>>() {});
//            for (SubStatDTO s : stats) {
//                s.setValue(s.getValue() * multiplier);
//            }
//            item.setSubStats(objectMapper.writeValueAsString(stats));
//        } catch (Exception e) {
//            // Ignore error for now
//        }
//
//        item.setMythic(true);
//        item.setEnhanceLevel(0); // Reset về Mythic +0
//        item.setMythicLevel(1);
//
//        return userItemRepo.save(item);
//    }
//
//    // --- 3. CƯỜNG HÓA MYTHIC (UPGRADE) ---
//    @Transactional
//    public UserItem enhanceMythic(Long userItemId) {
//        UserItem item = userItemRepo.findById(userItemId)
//                .orElseThrow(() -> new RuntimeException("Item not found"));
//
//        if (!item.isMythic()) throw new RuntimeException("Chưa phải Mythic!");
//
//        // Chi phí: (Level*50) Nguyên liệu lạ + (Level) Echo Coin + Vàng
//        int nextLv = item.getMythicLevel() + 1;
//        int goldCost = nextLv * 100000; // Tăng dần theo cấp
//
//        Map<Integer, Integer> materials = new HashMap<>();
//        materials.put(GameConstants.MAT_UNKNOWN, nextLv * 50); // Đốt nguyên liệu lạ
//        materials.put(GameConstants.MAT_ECHO_COIN, nextLv);    // Cần nhiều Coin
//
//        checkAndConsumeResources(item, materials, goldCost);
//
//        // --- LOGIC TĂNG CHỈ SỐ ---
//        item.setMythicLevel(nextLv);
//
//        // Main Stat: Tăng 1% cố định trên giá trị gốc Mythic
//        BigDecimal mainBonus = item.getOriginalMainStatValue().multiply(BigDecimal.valueOf(GameConstants.MYTHIC_MAIN_GROWTH));
//        item.setMainStatValue(item.getMainStatValue().add(mainBonus));
//
//        // Sub Stats: Tăng random 0.5% - 1% trên giá trị hiện tại (Compounding)
//        try {
//            List<SubStatDTO> stats = objectMapper.readValue(item.getSubStats(), new TypeReference<List<SubStatDTO>>() {});
//            for (SubStatDTO s : stats) {
//                double growth = randomRange(GameConstants.MYTHIC_SUB_GROWTH[0], GameConstants.MYTHIC_SUB_GROWTH[1]);
//                // Công thức: Mới = Cũ * (1 + %tăng)
//                s.setValue(s.getValue() * (1 + growth));
//            }
//            item.setSubStats(objectMapper.writeValueAsString(stats));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return userItemRepo.save(item);
//    }
//
//    private void applySubStatRoll(UserItem item) {
//        try {
//            List<SubStatDTO> stats = objectMapper.readValue(item.getSubStats(), new TypeReference<List<SubStatDTO>>() {});
//
//            // Logic nhảy dòng (Epic 7 style)
//            if (stats.size() < 4) {
//                // Thêm dòng mới
//                stats.add(itemGenService.generateRandomSubStat(item, stats));
//            } else {
//                // Random chọn 1 dòng cũ để tăng
//                int index = random.nextInt(stats.size());
//                SubStatDTO selectedStat = stats.get(index);
//                double bonus = itemGenService.getEnhanceRollValue(selectedStat.getCode(), item.getItem().getTier());
//                selectedStat.setValue(selectedStat.getValue() + bonus);
//            }
//            item.setSubStats(objectMapper.writeValueAsString(stats));
//        } catch (Exception e) {
//            throw new RuntimeException("Lỗi dữ liệu chỉ số phụ: " + e.getMessage());
//        }
//    }
//
//    private double randomRange(double min, double max) {
//        return min + (max - min) * random.nextDouble();
//    }
//}


package com.echommo.service;

import com.echommo.config.GameConstants;
import com.echommo.dto.SubStatDTO;
import com.echommo.entity.UserItem;
import com.echommo.entity.Wallet;
import com.echommo.enums.Rarity;
import com.echommo.enums.SlotType;
import com.echommo.repository.UserItemRepository;
import com.echommo.repository.WalletRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList; // Thêm ArrayList để khởi tạo list nếu cần
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final UserItemRepository userItemRepo;
    private final WalletRepository walletRepo;
    private final ItemGenerationService itemGenService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    // --- HELPER: KIỂM TRA & TRỪ TÀI NGUYÊN ---
    private void checkAndConsumeResources(UserItem targetItem, Map<Integer, Integer> materialCosts, int goldCost) {
        Integer userId = targetItem.getCharacter().getUser().getUserId();
        Integer charId = targetItem.getCharacter().getCharId();

        // 1. Kiểm tra Vàng
        Wallet wallet = walletRepo.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Lỗi dữ liệu: Không tìm thấy ví tiền!"));

        if (wallet.getGold().compareTo(BigDecimal.valueOf(goldCost)) < 0) {
            throw new RuntimeException("Không đủ vàng! Cần " + goldCost + " G nhưng bạn chỉ có " + wallet.getGold() + " G.");
        }

        // 2. Kiểm tra Nguyên liệu (Chỉ check trước, chưa trừ)
        Map<Integer, UserItem> foundItems = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : materialCosts.entrySet()) {
            int matId = entry.getKey();
            int qtyNeeded = entry.getValue();

            // Tìm trong kho đồ của nhân vật
            UserItem userMat = userItemRepo.findByCharacter_CharIdAndItem_ItemId(charId, matId)
                    .orElse(null);

            if (userMat == null || userMat.getQuantity() < qtyNeeded) {
                String currentQty = (userMat == null) ? "0" : String.valueOf(userMat.getQuantity());
                // Lưu ý: ID hiển thị lỗi này giúp bạn biết đang thiếu nguyên liệu ID nào (tra trong seed_core.sql)
                throw new RuntimeException("Thiếu nguyên liệu (ID: " + matId + ")! Cần: " + qtyNeeded + ", Có: " + currentQty);
            }
            foundItems.put(matId, userMat);
        }

        // 3. Thực hiện trừ (Sau khi đã validate đủ hết)
        // Trừ tiền
        wallet.setGold(wallet.getGold().subtract(BigDecimal.valueOf(goldCost)));
        walletRepo.save(wallet);

        // Trừ nguyên liệu
        for (Map.Entry<Integer, Integer> entry : materialCosts.entrySet()) {
            int matId = entry.getKey();
            int qtyNeeded = entry.getValue();
            UserItem itemToReduce = foundItems.get(matId);

            itemToReduce.setQuantity(itemToReduce.getQuantity() - qtyNeeded);

            // Nếu hết thì xóa khỏi DB để sạch rác
            if (itemToReduce.getQuantity() <= 0) {
                userItemRepo.delete(itemToReduce);
            } else {
                userItemRepo.save(itemToReduce);
            }
        }
    }

    // --- 1. CƯỜNG HÓA THƯỜNG (Lv 1 -> 30) ---
    @Transactional
    public UserItem enhanceItem(Long userItemId) {
        UserItem item = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại"));

        if (item.isMythic()) {
            throw new RuntimeException("Vật phẩm này đã là Mythic, hãy dùng chức năng Nâng Cấp Mythic!");
        }
        if (item.getEnhanceLevel() >= 30) {
            throw new RuntimeException("Đã đạt cấp tối đa (+30). Hãy Tiến Hóa lên Mythic!");
        }

        int nextLv = item.getEnhanceLevel() + 1;
        SlotType type = item.getItem().getSlotType();

        // --- CÔNG THỨC TIÊU HAO (RESOURCE SINK) ---
        Map<Integer, Integer> materials = new HashMap<>();
        int goldCost;

        if (nextLv <= 10) {
            // Tier 1: Sơ cấp (Đồng/Đá + Gỗ Sồi)
            goldCost = nextLv * 1000;
            int mainQty = nextLv * 15;
            int subQty = nextLv * 5;

            // Vũ khí dùng Quặng (ID 6), Giáp dùng Đá/Than (ID 5)
            if (type == SlotType.WEAPON) materials.put(GameConstants.MAT_ORE_COPPER, mainQty);
            else materials.put(GameConstants.MAT_STONE, mainQty);

            // Nguyên liệu phụ: Gỗ Sồi (ID 1)
            materials.put(GameConstants.MAT_WOOD_OAK, subQty);

        } else if (nextLv <= 20) {
            // Tier 2: Trung cấp (Sắt + Gỗ Khô)
            goldCost = nextLv * 3000;
            int levelScale = nextLv - 10;
            int mainQty = levelScale * 15;
            int subQty = levelScale * 5;

            materials.put(GameConstants.MAT_ORE_IRON, mainQty);   // ID 7
            materials.put(GameConstants.MAT_WOOD_DRIED, subQty); // ID 2

        } else {
            // Tier 3: Cao cấp (Bạch Kim + Gỗ Lạnh)
            goldCost = nextLv * 10000;
            int levelScale = nextLv - 20;
            int mainQty = levelScale * 20;
            int subQty = levelScale * 10;

            materials.put(GameConstants.MAT_ORE_PLATINUM, mainQty); // ID 8
            materials.put(GameConstants.MAT_WOOD_COLD, subQty);     // ID 3
        }

        // Kiểm tra và trừ nguyên liệu
        checkAndConsumeResources(item, materials, goldCost);

        // --- LOGIC CƯỜNG HÓA ---
        item.setEnhanceLevel(nextLv);

        // Nhảy dòng tại các mốc +3, +6, +9...
        if (nextLv % 3 == 0) {
            applySubStatRoll(item);
        }

        return userItemRepo.save(item);
    }

    // --- 2. TIẾN HÓA MYTHIC (EVOLVE) ---
    @Transactional
    public UserItem evolveToMythic(Long userItemId) {
        UserItem item = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.isMythic()) throw new RuntimeException("Đã là Mythic rồi!");
        if (item.getRarity() != Rarity.LEGENDARY) throw new RuntimeException("Chỉ đồ Legendary mới lên được Mythic");
        if (item.getEnhanceLevel() < 30) throw new RuntimeException("Cần +30 để tiến hóa!");

        Map<Integer, Integer> materials = new HashMap<>();
        materials.put(GameConstants.MAT_WOOD_STRANGE, 500); // ID 4
        materials.put(GameConstants.MAT_UNKNOWN, 200);      // ID 12
        materials.put(GameConstants.MAT_ECHO_COIN, 5);      // ID 11

        checkAndConsumeResources(item, materials, 1000000);

        // --- LOGIC BUFF CHỈ SỐ ---
        double multiplier = randomRange(GameConstants.MYTHIC_EVOLUTION_RANGE[0], GameConstants.MYTHIC_EVOLUTION_RANGE[1]);

        BigDecimal mainVal = item.getMainStatValue().multiply(BigDecimal.valueOf(multiplier));
        item.setMainStatValue(mainVal);
        item.setOriginalMainStatValue(mainVal);

        try {
            // [FIX] Handle null subStats
            String json = item.getSubStats();
            if (json == null || json.trim().isEmpty()) json = "[]";

            List<SubStatDTO> stats = objectMapper.readValue(json, new TypeReference<List<SubStatDTO>>() {});
            for (SubStatDTO s : stats) {
                s.setValue(s.getValue() * multiplier);
            }
            item.setSubStats(objectMapper.writeValueAsString(stats));
        } catch (Exception e) {
            // Ignore error purely for evolution stat scaling
        }

        item.setMythic(true);
        item.setEnhanceLevel(0);
        item.setMythicLevel(1);

        return userItemRepo.save(item);
    }

    // --- 3. CƯỜNG HÓA MYTHIC (UPGRADE) ---
    @Transactional
    public UserItem enhanceMythic(Long userItemId) {
        UserItem item = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.isMythic()) throw new RuntimeException("Chưa phải Mythic!");

        int nextLv = item.getMythicLevel() + 1;
        int goldCost = nextLv * 100000;

        Map<Integer, Integer> materials = new HashMap<>();
        materials.put(GameConstants.MAT_UNKNOWN, nextLv * 50); // ID 12
        materials.put(GameConstants.MAT_ECHO_COIN, nextLv);    // ID 11

        checkAndConsumeResources(item, materials, goldCost);

        // --- LOGIC TĂNG CHỈ SỐ ---
        item.setMythicLevel(nextLv);

        BigDecimal mainBonus = item.getOriginalMainStatValue().multiply(BigDecimal.valueOf(GameConstants.MYTHIC_MAIN_GROWTH));
        item.setMainStatValue(item.getMainStatValue().add(mainBonus));

        try {
            // [FIX] Handle null subStats
            String json = item.getSubStats();
            if (json == null || json.trim().isEmpty()) json = "[]";

            List<SubStatDTO> stats = objectMapper.readValue(json, new TypeReference<List<SubStatDTO>>() {});
            for (SubStatDTO s : stats) {
                double growth = randomRange(GameConstants.MYTHIC_SUB_GROWTH[0], GameConstants.MYTHIC_SUB_GROWTH[1]);
                s.setValue(s.getValue() * (1 + growth));
            }
            item.setSubStats(objectMapper.writeValueAsString(stats));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userItemRepo.save(item);
    }

    // --- [FIXED] LOGIC NHẢY DÒNG PHỤ AN TOÀN ---
    private void applySubStatRoll(UserItem item) {
        try {
            // 1. Kiểm tra NULL hoặc Rỗng
            String json = item.getSubStats();
            if (json == null || json.trim().isEmpty()) {
                json = "[]"; // Coi như danh sách rỗng
            }

            // 2. Parse JSON thành List an toàn
            List<SubStatDTO> stats = objectMapper.readValue(json, new TypeReference<List<SubStatDTO>>() {});

            // 3. Nếu list bị null (dù đã parse), khởi tạo lại
            if (stats == null) {
                stats = new ArrayList<>();
            }

            // 4. Logic nhảy dòng (Epic 7 style)
            if (stats.size() < 4) {
                // Nếu chưa đủ 4 dòng -> Thêm dòng mới
                stats.add(itemGenService.generateRandomSubStat(item, stats));
            } else {
                // Nếu đã đủ 4 dòng -> Random chọn 1 dòng cũ để tăng chỉ số
                int index = random.nextInt(stats.size());
                SubStatDTO selectedStat = stats.get(index);

                // Tính lượng chỉ số được cộng thêm
                double bonus = itemGenService.getEnhanceRollValue(selectedStat.getCode(), item.getItem().getTier());
                selectedStat.setValue(selectedStat.getValue() + bonus);
            }

            // 5. Lưu ngược lại vào DB
            item.setSubStats(objectMapper.writeValueAsString(stats));

        } catch (Exception e) {
            // Log lỗi ra console server để debug nhưng không làm crash game
            System.err.println("Lỗi khi roll chỉ số phụ cho Item ID " + item.getUserItemId() + ": " + e.getMessage());
            // Có thể chọn throw tiếp hoặc nuốt lỗi tùy strategy, ở đây ta throw để Frontend biết
            throw new RuntimeException("Lỗi xử lý chỉ số phụ: " + e.getMessage());
        }
    }

    private double randomRange(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
}