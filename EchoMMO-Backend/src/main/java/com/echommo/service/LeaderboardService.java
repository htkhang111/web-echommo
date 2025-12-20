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

    // --- 1. BXH LEVEL ---
    public List<LeaderboardEntry> getLevelLeaderboard() {
        // [FIX] Đã có hàm findTopLevels trong Repository
        List<Character> topChars = characterRepository.findTopLevels(PageRequest.of(0, 10));
        return mapCharacterToDto(topChars, "LEVEL");
    }

    // --- 2. BXH DIỆT QUÁI ---
    public List<LeaderboardEntry> getMonsterKillLeaderboard() {
        // [FIX] Đã có hàm findTopMonsterKills trong Repository
        List<Character> topChars = characterRepository.findTopMonsterKills(PageRequest.of(0, 10));
        return mapCharacterToDto(topChars, "MONSTER");
    }

    // --- 3. BXH TÀI PHÚ ---
    public List<LeaderboardEntry> getWealthLeaderboard() {
        // [FIX] Đã có hàm findTopWealth trong Repository
        List<Wallet> topWallets = walletRepository.findTopWealth(PageRequest.of(0, 10));

        List<LeaderboardEntry> result = new ArrayList<>();
        for (int i = 0; i < topWallets.size(); i++) {
            Wallet w = topWallets.get(i);
            if (w.getUser() == null) continue; // Skip nếu ví mồ côi

            String username = w.getUser().getUsername();
            String avatar = w.getUser().getAvatarUrl();

            // [FIX] Wallet.gold là Long, không cần chuyển đổi phức tạp
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

            String username = c.getUser().getUsername();
            String avatar = c.getUser().getAvatarUrl();

            String displayValue = "";
            String rank = String.valueOf(i + 1);

            if ("LEVEL".equals(type)) {
                displayValue = "Lv " + c.getLevel();
            } else if ("MONSTER".equals(type)) {
                // [FIX] Getter chuẩn monsterKills
                displayValue = String.format("%,d Trảm", c.getMonsterKills());
            }
            result.add(new LeaderboardEntry(username, displayValue, rank, avatar));
        }
        return result;
    }
}