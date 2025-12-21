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

    /**
     * Lấy thông tin nhân vật của người dùng hiện tại
     */
    public Character getMyCharacter() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) return null;

        Character c = characterRepo.findByUser(user).orElse(null);

        if (c != null) {
            ensureNoNullStats(c);
            recalculateStatPoints(c);
        }
        return c;
    }

    /**
     * Tính toán lại điểm tiềm năng khả dụng dựa trên level
     */
    private void recalculateStatPoints(Character c) {
        int lvl = (c.getLevel() != null) ? c.getLevel() : 1;
        int pointsPerLevel = 5;
        try {
            pointsPerLevel = GameConstants.STAT_POINTS_PER_LEVEL;
        } catch (Exception ignored) {}

        // Tổng điểm nhận được từ level 1 đến hiện tại
        int totalExpected = (lvl - 1) * pointsPerLevel;

        // Điểm đã cộng vào các chỉ số (mặc định ban đầu mỗi cái là 5)
        int usedPoints = (c.getStr() - 5) + (c.getVit() - 5) + (c.getAgi() - 5) +
                (c.getDex() - 5) + (c.getIntelligence() - 5) + (c.getLuck() - 5);

        c.setStatPoints(Math.max(0, totalExpected - usedPoints));
    }

    /**
     * Tạo nhân vật mặc định (thường dùng khi login lần đầu)
     */
    @Transactional
    public Character createDefaultCharacter(User user) {
        return characterRepo.findByUser(user)
                .orElseGet(() -> initNewCharacter(user, user.getUsername()));
    }

    /**
     * Người dùng tự tạo nhân vật với tên riêng
     */
    @Transactional
    public Character createCharacter(CharacterRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        if (characterRepo.findByUser(user).isPresent()) {
            throw new RuntimeException("Bạn đã có nhân vật rồi!");
        }
        return initNewCharacter(user, req.getName());
    }

    /**
     * Logic khởi tạo nhân vật mới
     */
    private Character initNewCharacter(User user, String charName) {
        Character c = new Character();
        c.setUser(user);
        c.setName(charName);
        c.setLevel(1);
        c.setCurrentExp(0L);
        c.setStatus(CharacterStatus.IDLE);
        c.setMonsterKills(0);

        // Chỉ số cơ bản ban đầu
        c.setStr(5); c.setVit(5); c.setAgi(5);
        c.setDex(5); c.setIntelligence(5); c.setLuck(5);
        c.setBaseDef(5);

        recalculateStats(c);

        Character savedChar = characterRepo.save(c);
        grantStarterPack(savedChar);
        return savedChar;
    }

    /**
     * Cộng điểm tiềm năng
     */
    @Transactional
    public Character addStats(Map<String, Integer> stats) {
        Character c = getMyCharacter();
        if (c == null) throw new RuntimeException("Chưa tạo nhân vật!");

        recalculateStatPoints(c);
        int available = c.getStatPoints();

        int addStr = stats.getOrDefault("str", 0);
        int addVit = stats.getOrDefault("vit", 0);
        int addAgi = stats.getOrDefault("agi", 0);
        int addDex = stats.getOrDefault("dex", 0);
        int addInt = stats.getOrDefault("int", 0);
        int addLuck = stats.getOrDefault("luck", 0);

        int totalCost = addStr + addVit + addAgi + addDex + addInt + addLuck;

        if (totalCost <= 0) throw new IllegalArgumentException("Vui lòng chọn chỉ số để cộng!");
        if (totalCost > available) throw new IllegalArgumentException("Không đủ điểm! Bạn chỉ còn " + available + " điểm.");

        c.setStr(c.getStr() + addStr);
        c.setVit(c.getVit() + addVit);
        c.setAgi(c.getAgi() + addAgi);
        c.setDex(c.getDex() + addDex);
        c.setIntelligence(c.getIntelligence() + addInt);
        c.setLuck(c.getLuck() + addLuck);

        recalculateStats(c);
        return characterRepo.save(c);
    }

    /**
     * Tính toán lại toàn bộ chỉ số chiến đấu (HP, Atk, Speed, Power...)
     * [FIX]: Đã thêm logic tăng trưởng tự động theo level
     */
    public void recalculateStats(Character c) {
        ensureNoNullStats(c);
        int lvl = c.getLevel();

        // 1. Tăng trưởng tự động (Level càng cao, base càng tăng dù không cộng điểm)
        int autoHpBonus = (lvl - 1) * 15;
        int autoAtkBonus = (lvl - 1) * 2;

        // 2. Tính HP (Vit ảnh hưởng chính)
        int newMaxHp = 200 + (c.getVit() * 20) + autoHpBonus;
        c.setMaxHp(newMaxHp);

        // 3. Tính Công (Str ảnh hưởng chính)
        c.setBaseAtk(10 + (c.getStr() * 2) + autoAtkBonus);

        // 4. Tính Tốc độ (Agi ảnh hưởng)
        c.setBaseSpeed(10 + c.getAgi());

        // 5. Chí mạng (Luck và Dex ảnh hưởng)
        c.setBaseCritRate(5 + (c.getLuck() / 5));
        c.setBaseCritDmg(150 + (c.getDex() / 2));

        // 6. Tính Lực chiến (Total Power) - Công thức tổng quát để xếp hạng
        int power = (newMaxHp / 10) + (c.getBaseAtk() * 3) + (c.getBaseDef() * 5) + (lvl * 10);
        c.setTotalPower(power);

        // Đảm bảo HP hiện tại không vượt quá HP tối đa
        if (c.getCurrentHp() == null || c.getCurrentHp() > newMaxHp) {
            c.setCurrentHp(newMaxHp);
        }
    }

    /**
     * Đảm bảo không có chỉ số nào bị null gây lỗi tính toán
     */
    private void ensureNoNullStats(Character c) {
        if (c.getStr() == null) c.setStr(5);
        if (c.getVit() == null) c.setVit(5);
        if (c.getAgi() == null) c.setAgi(5);
        if (c.getDex() == null) c.setDex(5);
        if (c.getIntelligence() == null) c.setIntelligence(5);
        if (c.getLuck() == null) c.setLuck(5);
        if (c.getBaseDef() == null) c.setBaseDef(5);
        if (c.getLevel() == null) c.setLevel(1);
    }

    /**
     * Tặng túi đồ tân thủ
     */
    private void grantStarterPack(Character character) {
        itemRepo.findByName("Kiếm Gỗ").ifPresent(i -> createItemForChar(character, i, 1, true));
        itemRepo.findByName("Bình Máu Nhỏ").ifPresent(i -> createItemForChar(character, i, 5, false));
    }

    /**
     * Tạo bản ghi vật phẩm trong túi đồ (UserItem)
     */
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