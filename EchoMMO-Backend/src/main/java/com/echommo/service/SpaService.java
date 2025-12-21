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
import java.time.LocalDateTime;

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

        // [LOGIC MỚI] Kiểm tra thời hạn
        if (character.getSpaEndTime() != null && character.getSpaEndTime().isAfter(LocalDateTime.now())) {
            // Đang spa thì ko trừ tiền nữa, trả về info luôn
            return new SpaStatusResponse(
                    "Đang thư giãn...",
                    character.getCurrentHp(),
                    character.getCurrentEnergy(),
                    wallet.getGold(),
                    wallet.getEchoCoin()
            );
        }

        SpaPackage pack;
        try {
            pack = SpaPackage.valueOf(packageType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Gói Spa không hợp lệ!");
        }

        BigDecimal cost = pack.getCost();
        boolean isFree = false;

        // 1. Logic Free Daily (Chỉ gói BASIC - Vàng)
        if (pack == SpaPackage.BASIC) {
            LocalDateTime lastFree = character.getLastFreeSpaUse();
            if (lastFree == null || lastFree.toLocalDate().isBefore(LocalDateTime.now().toLocalDate())) {
                cost = BigDecimal.ZERO;
                isFree = true;
            }
        }

        // 2. Logic Discount Tân Thủ (Chỉ gói VIP - Coin)
        // Dưới Lv 10: Tốn 0.5 Coin thay vì 5 Coin
        if (pack == SpaPackage.VIP && character.getLevel() < 10) {
            cost = new BigDecimal("0.5");
        }

        // Thanh toán
        if (pack == SpaPackage.VIP) {
            if (wallet.getEchoCoin().compareTo(cost) < 0) {
                throw new RuntimeException("Không đủ Echo Coin! Cần: " + cost);
            }
            wallet.setEchoCoin(wallet.getEchoCoin().subtract(cost));
        } else {
            // Basic
            if (wallet.getGold().compareTo(cost) < 0) {
                throw new RuntimeException("Không đủ Vàng! Cần: " + cost);
            }
            wallet.setGold(wallet.getGold().subtract(cost));
        }

        // Hồi phục & Cập nhật Time
        character.setCurrentHp(character.getMaxHp());
        character.setCurrentEnergy(character.getMaxEnergy());
        character.setSpaStartTime(LocalDateTime.now());
        character.setSpaEndTime(LocalDateTime.now().plusMinutes(pack == SpaPackage.VIP ? 60 : 30));

        if (isFree) {
            character.setLastFreeSpaUse(LocalDateTime.now());
        }

        characterRepository.save(character);
        walletRepository.save(wallet);

        String msg = isFree ? "Thư giãn miễn phí mỗi ngày!" : "Thanh toán thành công (-" + cost + ")";

        return new SpaStatusResponse(
                msg,
                character.getCurrentHp(),
                character.getCurrentEnergy(),
                wallet.getGold(),
                wallet.getEchoCoin()
        );
    }
}