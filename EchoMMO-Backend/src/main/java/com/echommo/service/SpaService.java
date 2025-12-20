package com.echommo.service;

import com.echommo.dto.SpaStatusResponse;
import com.echommo.entity.Character;
import com.echommo.entity.User;
import com.echommo.entity.Wallet;
import com.echommo.enums.SpaPackage;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SpaService {
    private final WalletRepository walletRepository;
    private final CharacterRepository characterRepository;

    @Transactional
    public SpaStatusResponse useSpa(User user, String packageType) {
        Character character = characterRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        SpaPackage pack;
        try {
            pack = SpaPackage.valueOf(packageType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Gói Spa không hợp lệ: " + packageType);
        }

        BigDecimal cost = pack.getCost();

        if (pack == SpaPackage.VIP) {
            // Gói VIP trừ Echo Coin (BigDecimal)
            if (wallet.getEchoCoin().compareTo(cost) < 0) {
                throw new RuntimeException("Không đủ Echo Coin! Cần: " + cost);
            }
            wallet.setEchoCoin(wallet.getEchoCoin().subtract(cost));
        } else {
            // Gói thường trừ Vàng (Long)
            // [FIX] cost là BigDecimal, wallet.gold là Long -> Cần chuyển đổi
            long goldCost = cost.longValue();
            if (wallet.getGold() < goldCost) {
                throw new RuntimeException("Không đủ Vàng! Cần: " + goldCost);
            }
            wallet.setGold(wallet.getGold() - goldCost);
        }

        // Hồi phục
        character.setCurrentHp(character.getMaxHp());
        character.setCurrentEnergy(character.getMaxEnergy());
        characterRepository.save(character);
        walletRepository.save(wallet);

        return new SpaStatusResponse(
                "Hồi phục thành công (" + pack.getName() + ")!",
                character.getCurrentHp(),
                character.getCurrentEnergy(),
                new BigDecimal(wallet.getGold()), // Convert Long -> BigDecimal cho DTO
                wallet.getEchoCoin()
        );
    }
}