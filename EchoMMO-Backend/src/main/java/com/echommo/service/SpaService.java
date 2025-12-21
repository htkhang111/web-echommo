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
            if (wallet.getEchoCoin().compareTo(cost) < 0) {
                throw new RuntimeException("Không đủ Echo Coin! Cần: " + cost);
            }
            wallet.setEchoCoin(wallet.getEchoCoin().subtract(cost));
        } else {
            // [FIX] Xử lý Gold là BigDecimal
            if (wallet.getGold().compareTo(cost) < 0) {
                throw new RuntimeException("Không đủ Vàng! Cần: " + cost);
            }
            wallet.setGold(wallet.getGold().subtract(cost));
        }

        character.setCurrentHp(character.getMaxHp());
        character.setCurrentEnergy(character.getMaxEnergy());
        characterRepository.save(character);
        walletRepository.save(wallet);

        // [FIX] Trả về BigDecimal trực tiếp
        return new SpaStatusResponse(
                "Hồi phục thành công (" + pack.getName() +  ")!",
                character.getCurrentHp(),
                character.getCurrentEnergy(),
                wallet.getGold(),
                wallet.getEchoCoin()
        );
    }
}