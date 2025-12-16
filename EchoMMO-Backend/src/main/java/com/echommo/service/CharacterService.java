package com.echommo.service;

import com.echommo.dto.CharacterRequest;
import com.echommo.entity.Character;
import com.echommo.entity.User;
import com.echommo.enums.CharacterStatus;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CharacterService {

    @Autowired private CharacterRepository characterRepo;
    @Autowired private UserRepository userRepo;

    public Character getMyCharacter() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) return null;
        return characterRepo.findByUser_UserId(user.getUserId()).orElse(null);
    }

    @Transactional
    public Character createDefaultCharacter(User user) {
        // Kiểm tra xem đã có chưa
        if (characterRepo.findByUser_UserId(user.getUserId()).isPresent()) {
            return characterRepo.findByUser_UserId(user.getUserId()).get();
        }

        Character c = new Character();
        c.setUser(user);
        c.setName(user.getUsername()); // Tên mặc định là username
        c.setLevel(1);
        c.setStatus(CharacterStatus.IDLE);

        // [FIX] SET CHỈ SỐ CƠ BẢN ĐẦY ĐỦ
        c.setStr(5); c.setDex(5); c.setIntelligence(5); c.setLuck(5);
        c.setStatPoints(0);

        // Base Stats
        c.setBaseAtk(10);
        c.setBaseDef(2);
        c.setBaseSpeed(10);
        c.setBaseCritRate(5); // 5%
        c.setBaseCritDmg(150); // 150%

        // [FIX QUAN TRỌNG] Set Max HP/Energy và Current = Max
        c.setMaxHp(100);
        c.setCurrentHp(100); // Đầy máu
        c.setMaxEnergy(50);
        c.setCurrentEnergy(50); // Đầy năng lượng

        c.setCurrentExp(0);

        return characterRepo.save(c);
    }

    @Transactional
    public Character createCharacter(CharacterRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        if (characterRepo.findByUser_UserId(user.getUserId()).isPresent()) {
            throw new RuntimeException("Bạn đã có nhân vật rồi!");
        }

        Character c = new Character();
        c.setUser(user);
        c.setName(req.getName());
        c.setLevel(1);
        c.setStatus(CharacterStatus.IDLE);

        // Stats khởi đầu
        c.setStr(5); c.setDex(5); c.setIntelligence(5); c.setLuck(5);
        c.setStatPoints(0);
        c.setBaseAtk(10); c.setBaseDef(2); c.setBaseSpeed(10);
        c.setBaseCritRate(5); c.setBaseCritDmg(150);

        // [FIX] Full bình
        c.setMaxHp(100);
        c.setCurrentHp(100);
        c.setMaxEnergy(50);
        c.setCurrentEnergy(50);
        c.setCurrentExp(0);

        return characterRepo.save(c);
    }
}