package com.echommo.service;

import com.echommo.dto.LeaderboardEntry;
import com.echommo.entity.Character;
import com.echommo.entity.Wallet;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    @Autowired
    private CharacterRepository charRepo;

    @Autowired
    private WalletRepository walletRepo;

    // 1. BXH Level
    public List<LeaderboardEntry> getLevelLeaderboard() {
        List<Character> characters = charRepo.findTop10ByOrderByLevelDescCurrentExpDesc();
        AtomicInteger rankCounter = new AtomicInteger(1);

        return characters.stream()
                .map(c -> {
                    String name = (c.getName() != null) ? c.getName() : "Unknown";
                    if (c.getName() == null && c.getUser() != null) {
                        name = c.getUser().getUsername();
                    }

                    // FIX: Chỉ gửi con số để Frontend dễ xử lý logic
                    String valueDisplay = String.valueOf(c.getLevel());
                    String rankDisplay = String.valueOf(rankCounter.getAndIncrement());
                    String avatarDisplay = (name.length() > 0) ? String.valueOf(name.charAt(0)).toUpperCase() : "?";

                    return new LeaderboardEntry(name, valueDisplay, rankDisplay, avatarDisplay);
                })
                .collect(Collectors.toList());
    }

    // 2. BXH Tài sản
    public List<LeaderboardEntry> getWealthLeaderboard() {
        List<Wallet> wallets = walletRepo.findTop10ByOrderByGoldDesc();
        AtomicInteger rankCounter = new AtomicInteger(1);

        return wallets.stream()
                .map(w -> {
                    String name = (w.getUser() != null) ? w.getUser().getUsername() : "Unknown";

                    // FIX: Chỉ gửi con số, loại bỏ chữ "Vàng" để tránh lỗi NaN ở Frontend
                    String valueDisplay = (w.getGold() != null) ? String.format("%.0f", w.getGold()) : "0";

                    String rankDisplay = String.valueOf(rankCounter.getAndIncrement());
                    String avatarDisplay = (name.length() > 0) ? String.valueOf(name.charAt(0)).toUpperCase() : "?";

                    return new LeaderboardEntry(name, valueDisplay, rankDisplay, avatarDisplay);
                })
                .collect(Collectors.toList());
    }
}