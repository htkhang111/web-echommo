package com.echommo.service;

import com.echommo.dto.LeaderboardEntry;
import com.echommo.entity.Character;
import com.echommo.entity.User;
import com.echommo.entity.Wallet;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderboardService {

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private WalletRepository walletRepository;

    // --- 1. BXH LEVEL ---
    public List<LeaderboardEntry> getLevelLeaderboard() {
        List<Character> topChars = characterRepository.findTopLevels(PageRequest.of(0, 10));
        return mapCharacterToDto(topChars, "LEVEL");
    }

    // --- 2. BXH DIỆT QUÁI ---
    public List<LeaderboardEntry> getMonsterKillLeaderboard() {
        List<Character> topChars = characterRepository.findTopMonsterKills(PageRequest.of(0, 10));
        return mapCharacterToDto(topChars, "MONSTER");
    }

    // --- 3. BXH TÀI PHÚ ---
    public List<LeaderboardEntry> getWealthLeaderboard() {
        List<Wallet> topWallets = walletRepository.findTopWealth(PageRequest.of(0, 10));

        List<LeaderboardEntry> result = new ArrayList<>();
        for (int i = 0; i < topWallets.size(); i++) {
            Wallet w = topWallets.get(i);
            if (w.getUser() == null) continue;

            User user = w.getUser();
            String username = user.getUsername();

            // [UPDATE] Ưu tiên Profile Image -> Avatar Skin -> Default
            String avatar = resolveAvatar(user);

            String displayValue = String.format("%,d Vàng", w.getGold());
            String rank = String.valueOf(i + 1);

            result.add(new LeaderboardEntry(username, displayValue, rank, avatar));
        }
        return result;
    }

    // --- HÀM PHỤ TRỢ ---
    private List<LeaderboardEntry> mapCharacterToDto(List<Character> chars, String type) {
        List<LeaderboardEntry> result = new ArrayList<>();
        for (int i = 0; i < chars.size(); i++) {
            Character c = chars.get(i);
            if (c.getUser() == null) continue;

            User user = c.getUser();
            String username = user.getUsername();

            // [UPDATE] Ưu tiên Profile Image -> Avatar Skin -> Default
            String avatar = resolveAvatar(user);

            String displayValue = "";
            String rank = String.valueOf(i + 1);

            if ("LEVEL".equals(type)) {
                displayValue = "Lv " + c.getLevel();
            } else if ("MONSTER".equals(type)) {
                displayValue = String.format("%,d Trảm", c.getMonsterKills());
            }
            result.add(new LeaderboardEntry(username, displayValue, rank, avatar));
        }
        return result;
    }

    // [NEW] Logic chọn Avatar
    private String resolveAvatar(User user) {
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            return user.getProfileImageUrl();
        }
        return user.getAvatarUrl(); // Trả về Skin ID (VD: skin_yasou) hoặc emoji
    }
}