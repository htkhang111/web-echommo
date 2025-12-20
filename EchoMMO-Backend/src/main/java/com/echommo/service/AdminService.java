package com.echommo.service;

import com.echommo.entity.User;
import com.echommo.entity.Wallet;
import com.echommo.repository.UserRepository;
import com.echommo.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());

        // [FIX] Reduce đúng cho BigDecimal
        BigDecimal totalEcho = walletRepository.findAll().stream()
                .map(Wallet::getEchoCoin)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalGold = walletRepository.findAll().stream()
                .mapToLong(Wallet::getGold)
                .sum();

        stats.put("totalEchoMined", totalEcho);
        stats.put("totalGoldCirculation", totalGold);
        return stats;
    }

    @Transactional
    public void giveReward(Long userId, String type, String amountStr) {
        User user = userRepository.findById(userId).orElseThrow();
        Wallet wallet = walletRepository.findByUser(user).orElseThrow();

        if ("GOLD".equalsIgnoreCase(type)) {
            wallet.setGold(wallet.getGold() + Long.parseLong(amountStr));
        } else if ("ECHO".equalsIgnoreCase(type)) {
            // [FIX] Add cho BigDecimal
            wallet.setEchoCoin(wallet.getEchoCoin().add(new BigDecimal(amountStr)));
        }
        walletRepository.save(wallet);
    }
}