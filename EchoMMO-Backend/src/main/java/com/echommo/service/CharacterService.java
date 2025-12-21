package com.echommo.service;

import com.echommo.dto.CharacterRequest;
import com.echommo.dto.SubStatDTO;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CharacterService {

    @Autowired private CharacterRepository characterRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ItemRepository itemRepo;
    @Autowired private UserItemRepository userItemRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Helper: Chuyển null thành 0
    private BigDecimal safe(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }

    private int safeInt(Integer val) {
        return val == null ? 0 : val;
    }

    public Character getMyCharacter() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) return null;

        Character c = characterRepo.findByUser(user).orElse(null);

        if (c != null) {
            ensureNoNullStats(c);
            recalculateStats(c); // Cập nhật chỉ số từ trang bị
            recalculateStatPoints(c); // Cập nhật điểm tiềm năng theo (LVL-1)*5
        }
        return c;
    }

    @Transactional
    public Character createDefaultCharacter(User user) {
        return characterRepo.findByUser(user)
                .orElseGet(() -> initNewCharacter(user, user.getUsername()));
    }

    @Transactional
    public Character createCharacter(CharacterRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        if (characterRepo.findByUser(user).isPresent()) {
            throw new RuntimeException("Bạn đã có nhân vật rồi!");
        }
        return initNewCharacter(user, req.getName());
    }

    private Character initNewCharacter(User user, String charName) {
        Character c = new Character();
        c.setUser(user);
        c.setName(charName);
        c.setLevel(1);
        c.setCurrentExp(0L);
        c.setStatus(CharacterStatus.IDLE);
        c.setMonsterKills(0);

        // Chỉ số khởi đầu cơ bản (Mặc định là 5)
        c.setStr(5); c.setVit(5); c.setAgi(5);
        c.setDex(5); c.setIntelligence(5); c.setLuck(5);
        c.setStatPoints(0); // Cấp 1: (1-1)*5 = 0 điểm

        recalculateStats(c);

        Character savedChar = characterRepo.save(c);
        grantStarterPack(savedChar);
        return savedChar;
    }

    /**
     * TÍNH TOÁN LẠI TẤT CẢ CHỈ SỐ: Điểm tiềm năng + Trang bị mặc định
     */
    public void recalculateStats(Character c) {
        ensureNoNullStats(c);
        int lvl = safeInt(c.getLevel());

        // 1. CHỈ SỐ GỐC TỪ ĐIỂM TIỀM NĂNG
        int rawMaxHp = 200 + (safeInt(c.getVit()) * 20);
        int rawAtk = 10 + (safeInt(c.getStr()) * 2);
        int rawDef = 5 + (safeInt(c.getVit()) / 5);
        int rawSpeed = 10 + safeInt(c.getAgi());
        int rawCritRate = 5 + (safeInt(c.getLuck()) / 5);
        int rawCritDmg = 150 + (safeInt(c.getDex()) / 2);

        // 2. CỘNG CHỈ SỐ TỪ TRANG BỊ ĐANG MẶC
        List<UserItem> equippedItems = userItemRepo.findByCharacter_CharIdAndIsEquippedTrue(c.getCharId());

        BigDecimal equipAtk = BigDecimal.ZERO;
        BigDecimal equipDef = BigDecimal.ZERO;
        BigDecimal equipHp = BigDecimal.ZERO;

        double bonusCritRate = 0;
        double bonusCritDmg = 0;
        double bonusSpeed = 0;

        for (UserItem item : equippedItems) {
            BigDecimal mainVal = safe(item.getMainStatValue());

            // Cộng chỉ số chính
            if (item.getItem().getSlotType() == SlotType.WEAPON) {
                equipAtk = equipAtk.add(mainVal);
            } else if (item.getItem().getSlotType() == SlotType.ARMOR
                    || item.getItem().getSlotType() == SlotType.HELMET
                    || item.getItem().getSlotType() == SlotType.BOOTS) {
                equipDef = equipDef.add(mainVal);
            }

            // Cộng dòng phụ Sub Stats
            try {
                if (item.getSubStats() != null && !item.getSubStats().equals("[]")) {
                    List<SubStatDTO> subs = objectMapper.readValue(item.getSubStats(), new TypeReference<List<SubStatDTO>>() {});
                    for (SubStatDTO sub : subs) {
                        switch (sub.getCode()) {
                            case "ATK_FLAT" -> equipAtk = equipAtk.add(BigDecimal.valueOf(sub.getValue()));
                            case "DEF_FLAT" -> equipDef = equipDef.add(BigDecimal.valueOf(sub.getValue()));
                            case "HP_FLAT" -> equipHp = equipHp.add(BigDecimal.valueOf(sub.getValue()));
                            case "SPEED" -> bonusSpeed += sub.getValue();
                            case "CRIT_RATE" -> bonusCritRate += sub.getValue();
                            case "CRIT_DMG" -> bonusCritDmg += sub.getValue();
                            case "ATK_PERCENT" -> equipAtk = equipAtk.add(BigDecimal.valueOf(rawAtk * (sub.getValue() / 100.0)));
                            case "DEF_PERCENT" -> equipDef = equipDef.add(BigDecimal.valueOf(rawDef * (sub.getValue() / 100.0)));
                            case "HP_PERCENT" -> equipHp = equipHp.add(BigDecimal.valueOf(rawMaxHp * (sub.getValue() / 100.0)));
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // 3. TỔNG HỢP KẾT QUẢ VÀO CHARACTER
        c.setMaxHp(rawMaxHp + equipHp.intValue());
        c.setBaseAtk(rawAtk + equipAtk.intValue());
        c.setBaseDef(rawDef + equipDef.intValue());
        c.setBaseSpeed(rawSpeed + (int)bonusSpeed);
        c.setBaseCritRate(rawCritRate + (int)bonusCritRate);
        c.setBaseCritDmg(rawCritDmg + (int)bonusCritDmg);

        // Lực chiến tổng hợp
        int power = (c.getMaxHp() / 10) + (c.getBaseAtk() * 5) + (c.getBaseDef() * 8) + (lvl * 20);
        c.setTotalPower(power);

        // Chống tràn máu
        if (c.getCurrentHp() == null || c.getCurrentHp() > c.getMaxHp()) {
            c.setCurrentHp(c.getMaxHp());
        }

        characterRepo.save(c);
    }

    @Transactional
    public Character addStats(Map<String, Integer> stats) {
        Character c = getMyCharacter();
        if (c == null) throw new RuntimeException("Chưa tạo nhân vật!");

        recalculateStatPoints(c);
        int available = safeInt(c.getStatPoints());

        int addStr = stats.getOrDefault("str", 0);
        int addVit = stats.getOrDefault("vit", 0);
        int addAgi = stats.getOrDefault("agi", 0);
        int addDex = stats.getOrDefault("dex", 0);
        int addInt = stats.getOrDefault("int", 0);
        int addLuck = stats.getOrDefault("luck", 0);

        int totalCost = addStr + addVit + addAgi + addDex + addInt + addLuck;

        if (totalCost <= 0) throw new IllegalArgumentException("Vui lòng chọn chỉ số!");
        if (totalCost > available) throw new IllegalArgumentException("Không đủ điểm!");

        c.setStr(safeInt(c.getStr()) + addStr);
        c.setVit(safeInt(c.getVit()) + addVit);
        c.setAgi(safeInt(c.getAgi()) + addAgi);
        c.setDex(safeInt(c.getDex()) + addDex);
        c.setIntelligence(safeInt(c.getIntelligence()) + addInt);
        c.setLuck(safeInt(c.getLuck()) + addLuck);

        recalculateStats(c);
        return characterRepo.save(c);
    }

    /**
     * TÍNH LẠI ĐIỂM TIỀM NĂNG: n = (Level - 1) * 5
     */
    private void recalculateStatPoints(Character c) {
        int lvl = safeInt(c.getLevel());

        // Tổng điểm nhận được theo Level
        int totalPointsEarned = (lvl - 1) * 5;

        // Tổng điểm đã tiêu (Trừ đi 5 điểm gốc mỗi loại)
        int usedPoints = (safeInt(c.getStr()) - 5) +
                (safeInt(c.getVit()) - 5) +
                (safeInt(c.getAgi()) - 5) +
                (safeInt(c.getDex()) - 5) +
                (safeInt(c.getIntelligence()) - 5) +
                (safeInt(c.getLuck()) - 5);

        c.setStatPoints(Math.max(0, totalPointsEarned - usedPoints));
    }

    private void ensureNoNullStats(Character c) {
        if (c.getStr() == null) c.setStr(5);
        if (c.getVit() == null) c.setVit(5);
        if (c.getAgi() == null) c.setAgi(5);
        if (c.getDex() == null) c.setDex(5);
        if (c.getIntelligence() == null) c.setIntelligence(5);
        if (c.getLuck() == null) c.setLuck(5);
        if (c.getLevel() == null) c.setLevel(1);
        if (c.getCurrentExp() == null) c.setCurrentExp(0L);
    }

    private void grantStarterPack(Character character) {
        itemRepo.findByName("Kiếm Gỗ").ifPresent(i -> createItemForChar(character, i, 1, true));
        itemRepo.findByName("Áo Vải Rách").ifPresent(i -> createItemForChar(character, i, 1, true));
    }

    private void createItemForChar(Character c, Item item, int qty, boolean equip) {
        UserItem ui = new UserItem();
        ui.setCharacter(c);
        ui.setItem(item);
        ui.setQuantity(qty);
        ui.setIsEquipped(equip);
        ui.setRarity(Rarity.COMMON);
        ui.setEnhanceLevel(0);
        ui.setMainStatValue(BigDecimal.valueOf(5));
        ui.setOriginalMainStatValue(BigDecimal.valueOf(5));

        if (item.getSlotType() == SlotType.WEAPON) {
            ui.setMainStatType("ATK_FLAT");
        } else if (item.getSlotType() == SlotType.ARMOR) {
            ui.setMainStatType("DEF_FLAT");
        } else {
            ui.setMainStatType("NONE");
        }

        ui.setSubStats("[]");
        ui.setAcquiredAt(LocalDateTime.now());
        userItemRepo.save(ui);
    }
}