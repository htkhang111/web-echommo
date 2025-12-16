package com.echommo.service;

import com.echommo.dto.SpaStatusResponse;
import com.echommo.entity.Character;
import com.echommo.entity.Wallet;
import com.echommo.enums.CharacterStatus;
import com.echommo.enums.SpaPackage;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class SpaService {

    @Autowired private CharacterRepository charRepo;
    @Autowired private WalletRepository walletRepo;

    // [FIX] Dùng CharacterService thay vì UserService để đồng bộ logic
    @Autowired private CharacterService charService;

    // 1. BẮT ĐẦU NGHỈ NGƠI
    @Transactional
    public SpaStatusResponse startSpa(SpaPackage packageType) {
        // [LOGIC MỚI] Lấy thẳng Character, không đi vòng qua User nữa
        Character character = charService.getMyCharacter();
        if (character == null) {
            throw new RuntimeException("Chưa có nhân vật! Hãy tạo nhân vật trước.");
        }

        // Lấy ví tiền dựa trên User ID của Character
        Wallet wallet = walletRepo.findByUser_UserId(character.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("Lỗi dữ liệu ví tiền!"));

        // Check tiền
        if (wallet.getGold().compareTo(packageType.getCost()) < 0) {
            throw new RuntimeException("Không đủ tiền! Cần " + packageType.getCost() + " Vàng.");
        }

        // Trừ tiền
        wallet.setGold(wallet.getGold().subtract(packageType.getCost()));
        walletRepo.save(wallet);

        // Set trạng thái & Thời gian
        LocalDateTime now = LocalDateTime.now();
        character.setStatus(CharacterStatus.RESTING);
        character.setSpaStartTime(now);
        character.setSpaEndTime(now.plusSeconds(packageType.getDurationSeconds()));
        character.setSpaPackageType(packageType.name());

        charRepo.save(character);

        return new SpaStatusResponse(
                "Bắt đầu nghỉ ngơi...",
                character.getCurrentHp(), character.getMaxHp(),
                character.getCurrentEnergy(), character.getMaxEnergy(),
                wallet.getGold(),
                (long) packageType.getDurationSeconds()
        );
    }

    // 2. HOÀN THÀNH NGHỈ NGƠI
    @Transactional
    public SpaStatusResponse completeSpa() {
        // [LOGIC MỚI] Lấy thẳng Character
        Character character = charService.getMyCharacter();
        if (character == null) {
            throw new RuntimeException("Chưa có nhân vật!");
        }

        // Check xem có đang nghỉ không
        if (character.getStatus() != CharacterStatus.RESTING || character.getSpaEndTime() == null) {
            return new SpaStatusResponse("Bạn đang không nghỉ ngơi.", character.getCurrentHp(), character.getMaxHp(), character.getCurrentEnergy(), character.getMaxEnergy(), null, 0L);
        }

        // Check thời gian (Server side validation)
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(character.getSpaEndTime())) {
            long remaining = ChronoUnit.SECONDS.between(now, character.getSpaEndTime());
            throw new RuntimeException("Chưa nghỉ xong! Còn " + remaining + " giây.");
        }

        // Hồi Full 100%
        character.setCurrentHp(character.getMaxHp());
        character.setCurrentEnergy(character.getMaxEnergy());

        // Reset trạng thái về IDLE
        character.setStatus(CharacterStatus.IDLE);
        character.setSpaStartTime(null);
        character.setSpaEndTime(null);
        character.setSpaPackageType(null);

        charRepo.save(character);

        return new SpaStatusResponse(
                "Hồi phục hoàn tất!",
                character.getCurrentHp(), character.getMaxHp(),
                character.getCurrentEnergy(), character.getMaxEnergy(),
                null, 0L
        );
    }
}