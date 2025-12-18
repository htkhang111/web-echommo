package com.echommo.service;

import com.echommo.config.GameConstants;
import com.echommo.dto.SubStatDTO;
import com.echommo.entity.UserItem;
import com.echommo.enums.Rarity;
import com.echommo.repository.UserItemRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final UserItemRepository userItemRepo;
    private final ItemGenerationService itemGenService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    // --- 1. CƯỜNG HÓA THƯỜNG (Lv 1 -> 30) ---
    @Transactional
    public UserItem enhanceItem(Long userItemId) {
        UserItem item = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.isMythic()) {
            throw new RuntimeException("Vật phẩm Mythic phải dùng chức năng Cường hóa Mythic");
        }
        if (item.getEnhanceLevel() >= 30) {
            throw new RuntimeException("Đã đạt cấp tối đa (+30). Hãy tiến hóa lên Mythic!");
        }

        // TODO: Trừ nguyên liệu và vàng ở đây (InventoryService call)

        int newLevel = item.getEnhanceLevel() + 1;
        item.setEnhanceLevel(newLevel);

        // LOGIC "NHẢY DÒNG" (JUMP STAT) tại +3, +6, +9...
        if (newLevel % 3 == 0) {
            applySubStatRoll(item);
        }

        return userItemRepo.save(item);
    }

    private void applySubStatRoll(UserItem item) {
        try {
            List<SubStatDTO> stats = objectMapper.readValue(item.getSubStats(), new TypeReference<List<SubStatDTO>>() {});

            // Logic Epic Seven:
            // Nếu chưa đủ 4 dòng -> Thêm dòng mới
            // Nếu đủ 4 dòng -> Random chọn 1 dòng để tăng chỉ số
            if (stats.size() < 4) {
                // Thêm dòng mới
                SubStatDTO newStat = itemGenService.generateRandomSubStat(item, stats);
                stats.add(newStat);
            } else {
                // Nhảy số dòng cũ
                int index = random.nextInt(stats.size());
                SubStatDTO selectedStat = stats.get(index);

                double bonus = itemGenService.getEnhanceRollValue(selectedStat.getCode(), item.getItem().getTier());
                selectedStat.setValue(selectedStat.getValue() + bonus);
            }

            item.setSubStats(objectMapper.writeValueAsString(stats));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý chỉ số phụ: " + e.getMessage());
        }
    }

    // --- 2. TIẾN HÓA MYTHIC (Evolution) ---
    @Transactional
    public UserItem evolveToMythic(Long userItemId) {
        UserItem item = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.isMythic()) throw new RuntimeException("Vật phẩm đã là Mythic");
        if (item.getRarity() != Rarity.LEGENDARY) throw new RuntimeException("Chỉ vật phẩm Legendary mới được lên Mythic");
        if (item.getEnhanceLevel() < 30) throw new RuntimeException("Cần cường hóa +30 để tiến hóa");

        // 1. Snapshot chỉ số cũ & Random buff (1.005 - 1.01)
        double multiplier = randomRange(GameConstants.MYTHIC_EVOLUTION_RANGE[0], GameConstants.MYTHIC_EVOLUTION_RANGE[1]);

        // Main stat
        BigDecimal mainVal = item.getMainStatValue().multiply(BigDecimal.valueOf(multiplier));
        item.setMainStatValue(mainVal);
        item.setOriginalMainStatValue(mainVal); // Lưu mốc gốc Mythic

        // Sub stats
        try {
            List<SubStatDTO> stats = objectMapper.readValue(item.getSubStats(), new TypeReference<List<SubStatDTO>>() {});
            for (SubStatDTO s : stats) {
                // Sub stat cũng được nhân hệ số buff khi lên Mythic
                s.setValue(s.getValue() * multiplier);
            }
            item.setSubStats(objectMapper.writeValueAsString(stats));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Chuyển trạng thái
        item.setMythic(true);
        item.setEnhanceLevel(0); // Reset về Mythic +0
        item.setMythicLevel(1);

        return userItemRepo.save(item);
    }

    // --- 3. CƯỜNG HÓA MYTHIC (Linear Growth) ---
    @Transactional
    public UserItem enhanceMythic(Long userItemId) {
        UserItem item = userItemRepo.findById(userItemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.isMythic()) throw new RuntimeException("Vật phẩm chưa đạt cấp Mythic");

        // TODO: Trừ nguyên liệu cao cấp + Echo Coin

        item.setMythicLevel(item.getMythicLevel() + 1);

        // Main Stat: Tăng 1% so với gốc Mythic
        BigDecimal mainBonus = item.getOriginalMainStatValue().multiply(BigDecimal.valueOf(GameConstants.MYTHIC_MAIN_GROWTH));
        item.setMainStatValue(item.getMainStatValue().add(mainBonus));

        // Sub Stats: Tăng random 0.5% - 1% trên giá trị hiện tại (Compounding)
        try {
            List<SubStatDTO> stats = objectMapper.readValue(item.getSubStats(), new TypeReference<List<SubStatDTO>>() {});
            for (SubStatDTO s : stats) {
                double growth = randomRange(GameConstants.MYTHIC_SUB_GROWTH[0], GameConstants.MYTHIC_SUB_GROWTH[1]);
                // Tăng tịnh tiến: value = value * (1 + growth)
                s.setValue(s.getValue() * growth);
            }
            item.setSubStats(objectMapper.writeValueAsString(stats));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userItemRepo.save(item);
    }

    private double randomRange(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
}