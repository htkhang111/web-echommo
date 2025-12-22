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
import java.math.RoundingMode;
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
            recalculateStats(c);
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
        c.setStr(5); c.setVit(5); c.setAgi(5);
        c.setDex(5); c.setIntelligence(5); c.setLuck(5);
        c.setStatPoints(0);

        recalculateStats(c);
        Character savedChar = characterRepo.save(c);
        grantStarterPack(savedChar);
        return savedChar;
    }

    /**
     * TÍNH TOÁN LẠI TẤT CẢ CHỈ SỐ
     * Fix: Chỉ cộng chỉ số từ UserItem (Túi đồ) để tránh nhân đôi
     */
    @Transactional
    public void recalculateStats(Character c) {
        ensureNoNullStats(c);
        int lvl = safeInt(c.getLevel());

        // 1. Chỉ số gốc từ điểm tiềm năng
        int rawMaxHp = 200 + (safeInt(c.getVit()) * 20);
        int rawAtk = 10 + (safeInt(c.getStr()) * 2);
        int rawDef = 5 + (safeInt(c.getVit()) / 5);
        int rawSpeed = 10 + safeInt(c.getAgi());
        int rawCritRate = 5 + (safeInt(c.getLuck()) / 5);
        int rawCritDmg = 150 + (safeInt(c.getDex()) / 2);

        // 2. Quét trang bị đang mặc
        List<UserItem> equippedItems = userItemRepo.findByCharacter_CharIdAndIsEquippedTrue(c.getCharId());

        BigDecimal equipAtk = BigDecimal.ZERO;
        BigDecimal equipDef = BigDecimal.ZERO;
        BigDecimal equipHp = BigDecimal.ZERO;

        double bonusCritRate = 0;
        double bonusCritDmg = 0;
        double bonusSpeed = 0;

        for (UserItem item : equippedItems) {
            BigDecimal mainVal = safe(item.getMainStatValue());
            String type = item.getMainStatType();

            // Nếu type lỗi, gán mặc định để xử lý ở switch
            if (type == null || type.equals("NONE") || type.isEmpty()) {
                type = "UNKNOWN";
            }

            // --- XỬ LÝ CHỈ SỐ CHÍNH (MAIN STAT) ---
            switch (type) {
                case "ATK_FLAT":
                case "ATK":
                    equipAtk = equipAtk.add(mainVal);
                    break;
                case "DEF_FLAT":
                case "DEF":
                    equipDef = equipDef.add(mainVal);
                    break;
                case "HP_FLAT":
                case "HP":
                    equipHp = equipHp.add(mainVal);
                    break;
                case "ATK_PERCENT":
                    equipAtk = equipAtk.add(BigDecimal.valueOf(rawAtk).multiply(mainVal.divide(BigDecimal.valueOf(100))));
                    break;
                case "DEF_PERCENT":
                    equipDef = equipDef.add(BigDecimal.valueOf(rawDef).multiply(mainVal.divide(BigDecimal.valueOf(100))));
                    break;
                case "HP_PERCENT":
                    equipHp = equipHp.add(BigDecimal.valueOf(rawMaxHp).multiply(mainVal.divide(BigDecimal.valueOf(100))));
                    break;
                case "CRIT_RATE": bonusCritRate += mainVal.doubleValue(); break;
                case "CRIT_DMG": bonusCritDmg += mainVal.doubleValue(); break;
                case "SPEED": bonusSpeed += mainVal.doubleValue(); break;

                // [FALLBACK] Nếu không rõ loại chỉ số, tự động cộng dựa trên Slot
                case "UNKNOWN":
                default:
                    SlotType slot = item.getItem().getSlotType();
                    if (slot == SlotType.WEAPON || slot == SlotType.RING) {
                        equipAtk = equipAtk.add(mainVal); // Kiếm/Nhẫn -> Cộng Công
                    } else if (slot == SlotType.ARMOR || slot == SlotType.HELMET ||
                            slot == SlotType.BOOTS || slot == SlotType.NECKLACE) {
                        equipDef = equipDef.add(mainVal); // Áo/Mũ/Giày -> Cộng Thủ
                    }
                    break;
            }

            // --- XỬ LÝ DÒNG ẨN (SUBSTATS) ---
            try {
                if (item.getSubStats() != null && !item.getSubStats().equals("[]") && !item.getSubStats().isEmpty()) {
                    List<SubStatDTO> subs = objectMapper.readValue(item.getSubStats(), new TypeReference<List<SubStatDTO>>() {});
                    for (SubStatDTO sub : subs) {
                        double val = sub.getValue();
                        switch (sub.getCode()) {
                            case "ATK_FLAT" -> equipAtk = equipAtk.add(BigDecimal.valueOf(val));
                            case "DEF_FLAT" -> equipDef = equipDef.add(BigDecimal.valueOf(val));
                            case "HP_FLAT" -> equipHp = equipHp.add(BigDecimal.valueOf(val));
                            case "SPEED" -> bonusSpeed += val;
                            case "CRIT_RATE" -> bonusCritRate += val;
                            case "CRIT_DMG" -> bonusCritDmg += val;
                            case "ATK_PERCENT" -> equipAtk = equipAtk.add(BigDecimal.valueOf(rawAtk * (val / 100.0)));
                            case "DEF_PERCENT" -> equipDef = equipDef.add(BigDecimal.valueOf(rawDef * (val / 100.0)));
                            case "HP_PERCENT" -> equipHp = equipHp.add(BigDecimal.valueOf(rawMaxHp * (val / 100.0)));
                        }
                    }
                }
            } catch (Exception ignored) {}

            // Debug Log để kiểm tra
            System.out.println("SCAN ITEM: " + item.getItem().getName() + " | Type: " + type + " | Value: " + mainVal);
        }

        // 3. Tổng hợp và lưu (Làm tròn HALF_UP)
        c.setBaseAtk(rawAtk + equipAtk.setScale(0, RoundingMode.HALF_UP).intValue());
        c.setBaseDef(rawDef + equipDef.setScale(0, RoundingMode.HALF_UP).intValue());
        c.setMaxHp(rawMaxHp + equipHp.setScale(0, RoundingMode.HALF_UP).intValue());
        c.setBaseSpeed(rawSpeed + (int) Math.round(bonusSpeed));
        c.setBaseCritRate(rawCritRate + (int) Math.round(bonusCritRate));
        c.setBaseCritDmg(rawCritDmg + (int) Math.round(bonusCritDmg));

        // Tính lực chiến
        int power = (c.getMaxHp() / 10) + (c.getBaseAtk() * 5) + (c.getBaseDef() * 8) + (lvl * 20);
        c.setTotalPower(power);

        if (c.getCurrentHp() == null || c.getCurrentHp() > c.getMaxHp()) {
            c.setCurrentHp(c.getMaxHp());
        }

        System.out.println("FINAL STATS for " + c.getName() + ": ATK=" + c.getBaseAtk() + " (Raw:" + rawAtk + " + Equip:" + equipAtk + ")");
        characterRepo.saveAndFlush(c);
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

        c = characterRepo.save(c);
        recalculateStats(c);
        recalculateStatPoints(c);

        return characterRepo.save(c);
    }

    private void recalculateStatPoints(Character c) {
        int lvl = safeInt(c.getLevel());
        int totalPointsEarned = (lvl - 1) * 5;
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