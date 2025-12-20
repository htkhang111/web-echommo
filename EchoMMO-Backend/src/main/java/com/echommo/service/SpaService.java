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

        SpaPackage pack = SpaPackage.valueOf(packageType.toUpperCase());

        if (pack == SpaPackage.VIP) {
            // [FIX] Xử lý BigDecimal
            BigDecimal cost = BigDecimal.valueOf(pack.getPrice());
            if (wallet.getEchoCoin().compareTo(cost) < 0) {
                throw new RuntimeException("Không đủ Echo Coin!");
            }
            wallet.setEchoCoin(wallet.getEchoCoin().subtract(cost));
        } else {
            // [FIX] Xử lý Long
            Long cost = (long) pack.getPrice();
            if (wallet.getGold() < cost) {
                throw new RuntimeException("Không đủ Vàng!");
            }
            wallet.setGold(wallet.getGold() - cost);
        }

        character.setCurrentHp(character.getMaxHp());
        character.setCurrentEnergy(character.getMaxEnergy());
        characterRepository.save(character);
        walletRepository.save(wallet);

        return new SpaStatusResponse(
                "Hồi phục thành công!",
                character.getCurrentHp(),
                character.getCurrentEnergy(),
                wallet.getGold(),
                wallet.getEchoCoin()
        );
    }
}