package com.echommo.service;

import com.echommo.config.GameConstants;
import com.echommo.dto.SubStatDTO;
import com.echommo.entity.User;
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
        // [FIX] Sử dụng hàm helper getUser() mới thêm vào UserItem
        User user = targetItem.getUser();
        if (user == null) throw new RuntimeException("Vật phẩm không có chủ sở hữu!");

        Integer userId = user.getUserId();
        Integer charId = targetItem.getCharacter().getCharId();

        // 1. Kiểm tra Vàng
        Wallet wallet = walletRepo.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Lỗi dữ liệu: Không tìm thấy ví tiền!"));

        // So sánh BigDecimal với int (convert int sang BigDecimal)
        if (wallet.getGold() < goldCost) {
            throw new RuntimeException("Không đủ vàng! Cần " + goldCost + " G nhưng bạn chỉ có " + wallet.getGold() + " G.");
        }

        // 2. Kiểm tra Nguyên liệu
        Map<Integer, UserItem> foundItems = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : materialCosts.entrySet()) {
            int matId = entry.getKey();
            int qtyNeeded = entry.getValue();

            UserItem userMat = userItemRepo.findByCharacter_CharIdAndItem_ItemId(charId, matId)
                    .orElse(null);

            if (userMat == null || userMat.getQuantity() < qtyNeeded) {
                String currentQty = (userMat == null) ? "0" : String.valueOf(userMat.getQuantity());
                throw new RuntimeException("Thiếu nguyên liệu (ID: " + matId + ")! Cần: " + qtyNeeded + ", Có: " + currentQty);
            }
            foundItems.put(matId, userMat);
        }

        // 3. Thực hiện trừ
        wallet.setGold(wallet.getGold() - goldCost);
        walletRepo.save(wallet);

        for (Map.Entry<Integer, Integer> entry : materialCosts.entrySet()) {
            int matId = entry.getKey();
            int qtyNeeded = entry.getValue();
            UserItem itemToReduce = foundItems.get(matId);

            itemToReduce.setQuantity(itemToReduce.getQuantity() - qtyNeeded);

            if (itemToReduce.getQuantity() <= 0) {
                userItemRepo.delete(itemToReduce);
            } else {
                userItemRepo.save(itemToReduce);
            }
        }
    }

    // --- 1. CƯỜNG HÓA THƯỜNG ---
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

        Map<Integer, Integer> materials = new HashMap<>();
        int goldCost;

        if (nextLv <= 10) {
            goldCost = nextLv * 1000;
            int mainQty = nextLv * 15;
            int subQty = nextLv * 5;
            if (type == SlotType.WEAPON) materials.put(GameConstants.MAT_ORE_COPPER, mainQty);
            else materials.put(GameConstants.MAT_STONE, mainQty);
            materials.put(GameConstants.MAT_WOOD_OAK, subQty);
        } else if (nextLv <= 20) {
            goldCost = nextLv * 3000;
            int levelScale = nextLv - 10;
            int mainQty = levelScale * 15;
            int subQty = levelScale * 5;
            materials.put(GameConstants.MAT_ORE_IRON, mainQty);
            materials.put(GameConstants.MAT_WOOD_DRIED, subQty);
        } else {
            goldCost = nextLv * 10000;
            int levelScale = nextLv - 20;
            int mainQty = levelScale * 20;
            int subQty = levelScale * 10;
            materials.put(GameConstants.MAT_ORE_PLATINUM, mainQty);
            materials.put(GameConstants.MAT_WOOD_COLD, subQty);
        }

        checkAndConsumeResources(item, materials, goldCost);

        item.setEnhanceLevel(nextLv);
        if (nextLv % 3 == 0) {
            applySubStatRoll(item);
        }

        return userItemRepo.save(item);
    }

    // --- 2. TIẾN HÓA MYTHIC ---
    @Transactional
    public UserItem evolveToMythic(Long userItemId) {
        UserItem item = userItemRepo.findById(userItemId).orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.isMythic()) throw new RuntimeException("Đã là Mythic rồi!");
        if (item.getRarity() != Rarity.LEGENDARY) throw new RuntimeException("Chỉ đồ Legendary mới lên được Mythic");
        if (item.getEnhanceLevel() < 30) throw new RuntimeException("Cần +30 để tiến hóa!");

        Map<Integer, Integer> materials = new HashMap<>();
        materials.put(GameConstants.MAT_WOOD_STRANGE, 500);
        materials.put(GameConstants.MAT_UNKNOWN, 200);
        materials.put(GameConstants.MAT_ECHO_COIN, 5);

        checkAndConsumeResources(item, materials, 1000000);

        double multiplier = randomRange(GameConstants.MYTHIC_EVOLUTION_RANGE[0], GameConstants.MYTHIC_EVOLUTION_RANGE[1]);
        BigDecimal mainVal = item.getMainStatValue().multiply(BigDecimal.valueOf(multiplier));
        item.setMainStatValue(mainVal);
        item.setOriginalMainStatValue(mainVal);

        try {
            List<SubStatDTO> stats = objectMapper.readValue(item.getSubStats(), new TypeReference<List<SubStatDTO>>() {});
            for (SubStatDTO s : stats) s.setValue(s.getValue() * multiplier);
            item.setSubStats(objectMapper.writeValueAsString(stats));
        } catch (Exception e) {}

        item.setMythic(true);
        item.setEnhanceLevel(0);
        item.setMythicLevel(1);

        return userItemRepo.save(item);
    }

    // --- 3. NÂNG CẤP MYTHIC ---
    @Transactional
    public UserItem enhanceMythic(Long userItemId) {
        UserItem item = userItemRepo.findById(userItemId).orElseThrow(() -> new RuntimeException("Item not found"));
        if (!item.isMythic()) throw new RuntimeException("Chưa phải Mythic!");

        int nextLv = item.getMythicLevel() + 1;
        int goldCost = nextLv * 100000;

        Map<Integer, Integer> materials = new HashMap<>();
        materials.put(GameConstants.MAT_UNKNOWN, nextLv * 50);
        materials.put(GameConstants.MAT_ECHO_COIN, nextLv);

        checkAndConsumeResources(item, materials, goldCost);

        item.setMythicLevel(nextLv);
        BigDecimal mainBonus = item.getOriginalMainStatValue().multiply(BigDecimal.valueOf(GameConstants.MYTHIC_MAIN_GROWTH));
        item.setMainStatValue(item.getMainStatValue().add(mainBonus));

        try {
            List<SubStatDTO> stats = objectMapper.readValue(item.getSubStats(), new TypeReference<List<SubStatDTO>>() {});
            for (SubStatDTO s : stats) {
                double growth = randomRange(GameConstants.MYTHIC_SUB_GROWTH[0], GameConstants.MYTHIC_SUB_GROWTH[1]);
                s.setValue(s.getValue() * (1 + growth));
            }
            item.setSubStats(objectMapper.writeValueAsString(stats));
        } catch (Exception e) { e.printStackTrace(); }

        return userItemRepo.save(item);
    }

    private void applySubStatRoll(UserItem item) {
        try {
            List<SubStatDTO> stats = objectMapper.readValue(item.getSubStats(), new TypeReference<List<SubStatDTO>>() {});
            if (stats.size() < 4) {
                stats.add(itemGenService.generateRandomSubStat(item, stats));
            } else {
                int index = random.nextInt(stats.size());
                SubStatDTO selectedStat = stats.get(index);
                double bonus = itemGenService.getEnhanceRollValue(selectedStat.getCode(), item.getItem().getTier());
                selectedStat.setValue(selectedStat.getValue() + bonus);
            }
            item.setSubStats(objectMapper.writeValueAsString(stats));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi dữ liệu chỉ số phụ: " + e.getMessage());
        }
    }

    private double randomRange(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
}