package com.echommo.service;

import com.echommo.dto.LeaderboardEntry;
import com.echommo.entity.Character;
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

    // --- 1. LOGIC BXH LEVEL ---
    public List<LeaderboardEntry> getLevelLeaderboard() {
        List<Character> topChars = characterRepository.findTopLevels(PageRequest.of(0, 10));
        return mapCharacterToDto(topChars, "LEVEL");
    }

    // --- 2. LOGIC BXH DIỆT QUÁI ---
    public List<LeaderboardEntry> getMonsterKillLeaderboard() {
        List<Character> topChars = characterRepository.findTopMonsterKills(PageRequest.of(0, 10));
        return mapCharacterToDto(topChars, "MONSTER");
    }

    // --- 3. LOGIC BXH TÀI PHÚ ---
    public List<LeaderboardEntry> getWealthLeaderboard() {
        List<Wallet> topWallets = walletRepository.findTopWealth(PageRequest.of(0, 10));

        List<LeaderboardEntry> result = new ArrayList<>();
        for (int i = 0; i < topWallets.size(); i++) {
            Wallet w = topWallets.get(i);

            // Lấy thông tin từ User
            String username = w.getUser().getUsername();
            String avatar = w.getUser().getAvatarUrl();

            // [FIX QUAN TRỌNG]
            // 1. Dùng .getGold() thay vì .getBalance()
            // 2. Chuyển BigDecimal sang longValue() để format số nguyên cho đẹp (bỏ .00)
            String displayValue = String.format("%,d Vàng", w.getGold().longValue());

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
            String username = c.getUser().getUsername();
            String avatar = c.getUser().getAvatarUrl();

            String displayValue = "";
            String rank = String.valueOf(i + 1);

            if ("LEVEL".equals(type)) {
                displayValue = "Lv " + c.getLevel();
            } else if ("MONSTER".equals(type)) {
                displayValue = String.format("%,d Quái", c.getMonsterKills());
            }
            result.add(new LeaderboardEntry(username, displayValue, rank, avatar));
        }
        return result;
    }
}