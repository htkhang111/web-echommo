package com.echommo.service;

import com.echommo.entity.Item;
import com.echommo.entity.User;
import com.echommo.entity.UserItem;
import com.echommo.entity.Character;
import com.echommo.enums.Rarity;
import com.echommo.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@Transactional
public class GameService {

    @Autowired private UserRepository userRepo;
    @Autowired private CharacterRepository charRepo;
    @Autowired private UserItemRepository userItemRepo;
    @Autowired private ItemRepository itemRepo;
    @Autowired private CharacterService characterService; // Đã fix hàm này trả về Character

    private final Random random = new Random();

    // --- HELPER METHODS ---
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Lỗi xác thực: Người dùng chưa đăng nhập."));
    }

    private Character getCharacter(Integer userId) {
        // [FIX] Dùng findByUser_UserId (Integer)
        return charRepo.findByUser_UserId(userId)
                .orElseGet(() -> {
                    User user = userRepo.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("User not found"));
                    // Tạo nhân vật mặc định nếu chưa có
                    return characterService.createDefaultCharacter(user);
                });
    }

    // --- EXPLORATION LOGIC ---
    private List<String> getMapResources(int level) {
        if (level < 20) return List.of("Gỗ", "Đá", "Quặng Đồng", "Cá");
        if (level < 30) return List.of("Gỗ", "Đá", "Quặng Đồng", "Sắt", "Cá");
        if (level < 40) return List.of("Gỗ", "Đá", "Quặng Đồng", "Sắt");
        if (level < 50) return List.of("Gỗ", "Đá", "Quặng Đồng", "Sắt", "Bạch Kim");
        return List.of("Gỗ", "Cá");
    }

    public Map<String, Object> explore(Integer userId) {
        Character character = getCharacter(userId);
        Map<String, Object> result = new HashMap<>();
        List<String> logs = new ArrayList<>();

        int expGain = 15;
        // [FIX] Dùng setter/getter chuẩn
        character.setCurrentExp(character.getCurrentExp() + expGain);

        // Check lên cấp
        if (character.getCurrentExp() >= character.getLevel() * 100L) {
            character.setCurrentExp(0L);
            character.setLevel(character.getLevel() + 1);
            character.setMaxHp(character.getMaxHp() + 50);
            character.setCurrentHp(character.getMaxHp());
            logs.add("🎉 LÊN CẤP! Cấp độ hiện tại: " + character.getLevel());
        }
        logs.add("Bạn đi thám hiểm... (+ " + expGain + " EXP)");

        // 70% cơ hội nhặt đồ
        if (random.nextInt(100) < 70) {
            List<String> possibleDrops = getMapResources(character.getLevel());
            String dropName = possibleDrops.get(random.nextInt(possibleDrops.size()));
            Item matItem = itemRepo.findByName(dropName).orElse(null);

            if (matItem != null) {
                // [FIX] Dùng findByCharacter_CharIdAndItem_ItemId
                UserItem ui = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), matItem.getItemId())
                        .orElse(null);

                if (ui == null) {
                    ui = new UserItem();
                    ui.setCharacter(character);
                    ui.setItem(matItem);
                    ui.setQuantity(0);
                    ui.setIsEquipped(false);
                    ui.setEnhanceLevel(0);
                    ui.setAcquiredAt(LocalDateTime.now());
                    ui.setMainStatValue(BigDecimal.ZERO);
                    ui.setRarity(Rarity.COMMON);
                    ui.setSubStats("[]");
                }

                ui.setQuantity(ui.getQuantity() + 1);
                userItemRepo.save(ui);
                logs.add("🎒 Nhặt được: " + dropName);
            }
        } else {
            logs.add("Không tìm thấy gì đặc biệt.");
        }

        charRepo.save(character);
        result.put("logs", logs);
        result.put("playerExp", character.getCurrentExp());
        result.put("playerLevel", character.getLevel());
        return result;
    }

    public List<UserItem> getInventory(Integer userId) {
        Character character = getCharacter(userId);
        // [FIX] Dùng charId
        return userItemRepo.findByCharacter_CharIdOrderByAcquiredAtDesc(character.getCharId());
    }
}