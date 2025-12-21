package com.echommo.service;

import com.echommo.config.GameConstants;
import com.echommo.entity.Character;
import com.echommo.entity.Wallet;
import com.echommo.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DumpService {

    private final CharacterService charService;
    private final WalletRepository walletRepo;
    private final Random random = new Random();

    @Transactional
    public Map<String, Object> dumpFish(String fishType, int amount) {
        Character c = charService.getMyCharacter();
        Wallet w = c.getUser().getWallet();

        // 1. Trừ Cá
        if ("NORMAL".equals(fishType)) {
            if (w.getFish() < amount) throw new RuntimeException("Không đủ Cá thường!");
            w.setFish(w.getFish() - amount);
        } else if ("SHARK".equals(fishType)) {
            if (w.getShark() < amount) throw new RuntimeException("Không đủ Cá mập!");
            w.setShark(w.getShark() - amount);
        } else {
            throw new RuntimeException("Loại cá không hợp lệ");
        }

        // 2. Quay thưởng (Loop từng con cá để tính tỉ lệ)
        BigDecimal totalGold = BigDecimal.ZERO;
        BigDecimal totalEcho = BigDecimal.ZERO;
        Map<String, Integer> items = new HashMap<>();

        for (int i = 0; i < amount; i++) {
            int roll = random.nextInt(100); // 0-99

            if (roll < 70) {
                // 70% ra Vàng (Lỗ vốn nhẹ: 10-20 Gold)
                totalGold = totalGold.add(BigDecimal.valueOf(10 + random.nextInt(11)));
            } else if (roll < 90) {
                // 20% ra Rác (Gỗ/Đá)
                items.merge("Gỗ Vụn", 1, Integer::sum);
                w.setWood(w.getWood() + 1);
            } else if (roll < 99) {
                // 9% ra Nguyên liệu xịn (Random Strange/BlackWood)
                if (random.nextBoolean()) {
                    items.merge("Gỗ Lạ", 1, Integer::sum);
                    w.setStrangeWood(w.getStrangeWood() + 1);
                } else {
                    items.merge("Quặng Lạ", 1, Integer::sum);
                    w.setUnknownMaterial(w.getUnknownMaterial() + 1);
                }
            } else {
                // 1% JACKPOT ECHO COIN
                double echoFrac = 0.001 + (0.499 * random.nextDouble());
                BigDecimal val = BigDecimal.valueOf(echoFrac);
                totalEcho = totalEcho.add(val);
            }
        }

        // Cộng kết quả vào ví
        w.setGold(w.getGold().add(totalGold));
        w.setEchoCoin(w.getEchoCoin().add(totalEcho));
        walletRepo.save(w);

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Đã thả " + amount + " con cá xuống hồ!");
        res.put("goldReceived", totalGold);
        res.put("echoReceived", totalEcho);
        res.put("itemsReceived", items);

        return res;
    }
}