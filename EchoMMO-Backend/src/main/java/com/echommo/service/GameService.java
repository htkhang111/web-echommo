package com.echommo.service;

import com.echommo.entity.Item;
import com.echommo.entity.User;
import com.echommo.entity.UserItem;
import com.echommo.entity.Character;
import com.echommo.enums.Rarity;
import com.echommo.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @Autowired private WalletRepository walletRepo;
    @Autowired private UserItemRepository userItemRepo;
    @Autowired private ItemRepository itemRepo;
    @Autowired private CharacterService characterService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    // --- HELPER METHODS: CURRENT USER ---
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || username.equals("anonymousUser")) {
            throw new RuntimeException("Lỗi xác thực: Người dùng chưa đăng nhập.");
        }
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Lỗi CSDL: Không tìm thấy người dùng [" + username + "]"));
    }

    // --- HELPER: GET CHARACTER (ĐÃ SỬA LỖI LONG/INTEGER) ---
    private Character getCharacter(Integer userId) {
        // [FIX LỖI ẢNH 3] Truyền thẳng userId (Integer) vào, KHÔNG ép kiểu Long
        return charRepo.findByUser_UserId(userId)
                .orElseGet(() -> {
                    // Fallback: Nếu chưa có thì tạo mới
                    User user = userRepo.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("User not found"));
                    return characterService.createDefaultCharacter(user);
                });
    }

    // =========================================================
    // 1. HỆ THỐNG MAP & FARM TÀI NGUYÊN (EXPLORE)
    // =========================================================

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

        // 1. Cộng EXP
        int expGain = 15;
        character.setCurrentExp(character.getCurrentExp() + expGain);

        // Check Level Up
        if (character.getCurrentExp() >= character.getLevel() * 100L) {
            character.setCurrentExp(0);
            character.setLevel(character.getLevel() + 1);
            character.setMaxHp(character.getMaxHp() + 50);
            character.setCurrentHp(character.getMaxHp());
            logs.add("🎉 LÊN CẤP! Cấp độ hiện tại: " + character.getLevel());
        }
        logs.add("Bạn đi thám hiểm... (+ " + expGain + " EXP)");

        // 2. Logic rớt nguyên liệu (70%)
        if (random.nextInt(100) < 70) {
            List<String> possibleDrops = getMapResources(character.getLevel());
            String dropName = possibleDrops.get(random.nextInt(possibleDrops.size()));

            Item matItem = itemRepo.findByName(dropName).orElse(null);

            if (matItem != null) {
                // [FIX LOGIC] Tìm trong túi CHARACTER (theo CharId) thay vì User
                UserItem ui = userItemRepo.findByCharacter_CharIdAndItem_ItemId(character.getCharId(), matItem.getItemId())
                        .orElse(null);

                if (ui == null) {
                    ui = new UserItem();
                    ui.setCharacter(character); // [FIX] Gán cho Character
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

    // =========================================================
    // 2. CÁC CHỨC NĂNG KHÁC
    // =========================================================

    public List<UserItem> getInventory(Integer userId) {
        // [FIX] Lấy đồ theo Character ID
        Character character = getCharacter(userId);
        return userItemRepo.findByCharacter_CharIdOrderByAcquiredAtDesc(character.getCharId());
    }

    public Map<String, Object> equipItem(Integer userId, Long userItemId) {
        return Map.of("message", "Vui lòng sử dụng API /api/inventory/equip");
    }

    public Map<String, Object> unequipItem(Integer userId, Long userItemId) {
        return Map.of("message", "Vui lòng sử dụng API /api/inventory/unequip");
    }

    public User getPlayerOrCreate(Integer userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }
}