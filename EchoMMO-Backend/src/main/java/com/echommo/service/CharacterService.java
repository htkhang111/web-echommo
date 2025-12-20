package com.echommo.service;

import com.echommo.config.GameConstants;
import com.echommo.dto.CharacterRequest;
import com.echommo.entity.Character;
import com.echommo.entity.Item;
import com.echommo.entity.User;
import com.echommo.entity.UserItem;
import com.echommo.enums.CharacterStatus;
import com.echommo.enums.Rarity;
import com.echommo.enums.SlotType;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.ItemRepository;
import com.echommo.repository.UserItemRepository;
import com.echommo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class CharacterService {

    @Autowired private CharacterRepository characterRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ItemRepository itemRepo;
    @Autowired private UserItemRepository userItemRepo;

    // --- 1. LẤY THÔNG TIN NHÂN VẬT (Tính toán điểm tiềm năng ảo để hiển thị) ---
    public Character getMyCharacter() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) return null;

        Character c = characterRepo.findByUserId(user.getUserId()).orElse(null);
        if (c != null) {
            // [LOGIC TÍNH ĐIỂM TIỀM NĂNG]
            int lvl = c.getLevel() != null ? c.getLevel() : 1;
            int str = c.getStr() != null ? c.getStr() : 5;
            int vit = c.getVit() != null ? c.getVit() : 5;
            int agi = c.getAgi() != null ? c.getAgi() : 5;
            int dex = c.getDex() != null ? c.getDex() : 5;
            int intel = c.getIntelligence() != null ? c.getIntelligence() : 5;
            int luck = c.getLuck() != null ? c.getLuck() : 5;

            // Mặc định 5 điểm mỗi cấp
            int pointsPerLevel = 5;
            try { pointsPerLevel = GameConstants.STAT_POINTS_PER_LEVEL; } catch (Exception ignored) {}

            // Tổng điểm đáng lẽ có = (Level - 1) * 5
            int expectedPoints = (lvl - 1) * pointsPerLevel;

            // Tổng điểm đã cộng (Trừ đi 5 điểm gốc ban đầu của mỗi chỉ số)
            int usedPoints = (str - 5) + (vit - 5) + (agi - 5) + (dex - 5) + (intel - 5) + (luck - 5);

            // Set vào biến tạm để Frontend hiển thị (không lưu DB dòng này)
            c.setStatPoints(Math.max(0, expectedPoints - usedPoints));

            // Fill default null values
            ensureNoNullStats(c);
        }
        return c;
    }

    // --- 2. TẠO NHÂN VẬT MẶC ĐỊNH (Dùng nội bộ) ---
    @Transactional
    public Character createDefaultCharacter(User user) {
        if (characterRepo.findByUserId(user.getUserId()).isPresent()) {
            return characterRepo.findByUserId(user.getUserId()).get();
        }
        return initNewCharacter(user, user.getUsername());
    }

    // --- 3. TẠO NHÂN VẬT (Dùng API) ---
    @Transactional
    public Character createCharacter(CharacterRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        if (characterRepo.findByUserId(user.getUserId()).isPresent()) {
            throw new RuntimeException("Bạn đã có nhân vật rồi!");
        }
        return initNewCharacter(user, req.getName());
    }

    // Helper tạo mới chung
    private Character initNewCharacter(User user, String charName) {
        Character c = new Character();
        c.setUser(user);
        c.setName(charName);
        c.setLevel(1);
        c.setStatus(CharacterStatus.IDLE);

        // Stats gốc
        c.setStr(5); c.setVit(5); c.setAgi(5);
        c.setDex(5); c.setIntelligence(5); c.setLuck(5);

        recalculateStats(c); // Tính máu, lực chiến lần đầu

        Character savedChar = characterRepo.save(c);
        grantStarterPack(savedChar);
        return savedChar;
    }

    // --- 4. CỘNG ĐIỂM TIỀM NĂNG (AN TOÀN) ---
    @Transactional
    public Character addStats(Map<String, Integer> stats) {
        Character c = getMyCharacter();
        if (c == null) throw new RuntimeException("Chưa tạo nhân vật!");

        // A. TÍNH TOÁN LẠI ĐIỂM TIỀM NĂNG THỰC TẾ (Validate Server-side)
        int lvl = c.getLevel() != null ? c.getLevel() : 1;
        int str = c.getStr() != null ? c.getStr() : 5;
        int vit = c.getVit() != null ? c.getVit() : 5;
        int agi = c.getAgi() != null ? c.getAgi() : 5;
        int dex = c.getDex() != null ? c.getDex() : 5;
        int intel = c.getIntelligence() != null ? c.getIntelligence() : 5;
        int luck = c.getLuck() != null ? c.getLuck() : 5;

        int pointsPerLevel = 5;
        try { pointsPerLevel = GameConstants.STAT_POINTS_PER_LEVEL; } catch (Exception ignored) {}

        int expectedPoints = (lvl - 1) * pointsPerLevel;
        int usedPoints = (str - 5) + (vit - 5) + (agi - 5) + (dex - 5) + (intel - 5) + (luck - 5);
        int available = Math.max(0, expectedPoints - usedPoints);

        // B. KIỂM TRA ĐẦU VÀO
        int addStr = stats.getOrDefault("str", 0);
        int addVit = stats.getOrDefault("vit", 0);
        int addAgi = stats.getOrDefault("agi", 0);
        int addDex = stats.getOrDefault("dex", 0);
        int addInt = stats.getOrDefault("int", 0);
        int addLuck = stats.getOrDefault("luck", 0);

        int totalCost = addStr + addVit + addAgi + addDex + addInt + addLuck;

        if (totalCost <= 0) throw new IllegalArgumentException("Vui lòng chọn chỉ số để cộng!");
        if (totalCost > available) throw new IllegalArgumentException("Không đủ điểm tiềm năng! Bạn chỉ còn " + available + " điểm.");

        // C. CỘNG VÀ LƯU
        c.setStr(str + addStr);
        c.setVit(vit + addVit);
        c.setAgi(agi + addAgi);
        c.setDex(dex + addDex);
        c.setIntelligence(intel + addInt);
        c.setLuck(luck + addLuck);

        // D. TÍNH LẠI CHỈ SỐ PHỤ & LỰC CHIẾN
        recalculateStats(c);

        // Cập nhật lại statPoints vào object trả về
        c.setStatPoints(available - totalCost);

        return characterRepo.save(c);
    }

    // --- 5. TÍNH TOÁN CHỈ SỐ PHỤ & LỰC CHIẾN (QUAN TRỌNG CHO PVP) ---
    private void recalculateStats(Character c) {
        ensureNoNullStats(c);

        int str = c.getStr();
        int vit = c.getVit();
        int agi = c.getAgi();
        int intel = c.getIntelligence();
        int dex = c.getDex();

        // 1. Máu = 200 + (VIT * 20)
        int oldMaxHp = c.getMaxHp() != null ? c.getMaxHp() : 200;
        int newMaxHp = 200 + (vit * 20);
        c.setMaxHp(newMaxHp);

        // 2. Tấn công = 10 + (STR * 2)
        c.setBaseAtk(10 + (str * 2));

        // 3. Tốc độ = 10 + AGI
        c.setBaseSpeed(10 + agi);

        // 4. Crit Rate/Dmg (Cơ bản)
        c.setBaseCritRate(5 + (luckByClass(c) / 5)); // Ví dụ
        c.setBaseCritDmg(150 + (dex / 2));

        // 5. [QUAN TRỌNG] TÍNH LỰC CHIẾN (Combat Power)
        // Công thức: HP/10 + ATK + DEF + SPEED + (Stats Tổng)
        int power = (newMaxHp / 10) + c.getBaseAtk() + c.getBaseDef() + c.getBaseSpeed()
                + str + vit + agi + intel + dex;
        c.setTotalPower(power);

        // Logic hồi đầy máu khi MaxHP tăng (giống game RPG)
        // Hoặc giữ nguyên % máu (tùy bạn chọn), ở đây mình giữ nguyên giá trị hiện tại
        if (c.getCurrentHp() == null) c.setCurrentHp(newMaxHp);
        if (c.getCurrentHp() > newMaxHp) c.setCurrentHp(newMaxHp);
    }

    // --- HELPER METHODS ---

    private void ensureNoNullStats(Character c) {
        if (c.getStr() == null) c.setStr(5);
        if (c.getVit() == null) c.setVit(5);
        if (c.getAgi() == null) c.setAgi(5);
        if (c.getDex() == null) c.setDex(5);
        if (c.getIntelligence() == null) c.setIntelligence(5);
        if (c.getLuck() == null) c.setLuck(5);
        if (c.getBaseDef() == null) c.setBaseDef(5);
    }

    private int luckByClass(Character c) {
        return c.getLuck() != null ? c.getLuck() : 5;
    }

    private void grantStarterPack(Character character) {
        Item sword = itemRepo.findByName("Kiếm Gỗ").orElse(null);
        if (sword != null) createItemForChar(character, sword, 1, true);

        Item potion = itemRepo.findByName("Bình Máu Nhỏ").orElse(null);
        if (potion != null) createItemForChar(character, potion, 5, false);
    }

    private void createItemForChar(Character c, Item item, int qty, boolean equip) {
        UserItem ui = new UserItem();
        ui.setCharacter(c);
        ui.setItem(item);
        ui.setQuantity(qty);
        ui.setIsEquipped(equip);
        ui.setRarity(Rarity.COMMON);
        ui.setEnhanceLevel(0);

        if (item.getSlotType() == SlotType.WEAPON) {
            ui.setMainStatType("ATK_FLAT");
            ui.setMainStatValue(BigDecimal.valueOf(5));
        } else {
            ui.setMainStatType(null);
            ui.setMainStatValue(BigDecimal.ZERO);
        }
        ui.setSubStats("[]");
        ui.setAcquiredAt(LocalDateTime.now());
        userItemRepo.save(ui);
    }
}