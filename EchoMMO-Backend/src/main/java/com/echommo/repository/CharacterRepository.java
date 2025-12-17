package com.echommo.repository;

import com.echommo.entity.Character;
import org.springframework.data.domain.Pageable; // [QUAN TRỌNG] Import cái này cho PageRequest
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Integer> {

    // --- PHẦN 1: LOGIC GAME (Giữ nguyên của bạn) ---

    // Tìm nhân vật theo User ID (Đổi long -> Integer cho khớp với Entity User)
    Optional<Character> findByUser_UserId(Integer userId);

    // Kiểm tra tên nhân vật đã tồn tại (Dùng lúc tạo nhân vật)
    boolean existsByName(String name);

    // Lấy Character + User + Wallet trong 1 lần query (Dùng cho Game Loop/Shop)
    @Query("SELECT c FROM Character c JOIN FETCH c.user u JOIN FETCH u.wallet WHERE u.userId = :userId")
    Optional<Character> findByUser_UserIdWithUserAndWallet(@Param("userId") Integer userId);


    // --- PHẦN 2: LOGIC LEADERBOARD (Mới thêm) ---

    // [BXH LEVEL]
    // Thay thế hàm findTop10 dài dòng cũ bằng @Query để có JOIN FETCH (tránh lỗi mất tên User)
    @Query("SELECT c FROM Character c JOIN FETCH c.user ORDER BY c.level DESC, c.currentExp DESC")
    List<Character> findTopLevels(Pageable pageable);

    // [BXH SÁT QUÁI]
    // Lấy Top diệt quái, kèm thông tin User
    @Query("SELECT c FROM Character c JOIN FETCH c.user ORDER BY c.monsterKills DESC")
    List<Character> findTopMonsterKills(Pageable pageable);
}