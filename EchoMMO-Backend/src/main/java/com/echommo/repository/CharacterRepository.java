package com.echommo.repository;

import com.echommo.entity.Character;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Integer> {

    // --- PHẦN 1: LOGIC GAME ---

    // [FIX QUAN TRỌNG] Thêm hàm này để khớp với PvpController đang gọi
    // Vì Entity Character lưu đối tượng 'User' chứ không phải 'userId' trực tiếp,
    // nên ta dùng @Query để map chính xác.
    @Query("SELECT c FROM Character c WHERE c.user.userId = :userId")
    Optional<Character> findByUserId(@Param("userId") Integer userId);

    // Hàm cũ của bạn (Spring Data JPA chuẩn), vẫn giữ lại nếu các chỗ khác dùng
    Optional<Character> findByUser_UserId(Integer userId);

    // Kiểm tra tên nhân vật đã tồn tại
    boolean existsByName(String name);

    // Lấy Character + User + Wallet trong 1 lần query (Tối ưu hiệu năng)
    @Query("SELECT c FROM Character c JOIN FETCH c.user u JOIN FETCH u.wallet WHERE u.userId = :userId")
    Optional<Character> findByUser_UserIdWithUserAndWallet(@Param("userId") Integer userId);


    // --- PHẦN 2: LOGIC LEADERBOARD ---

    // [BXH LEVEL]
    @Query("SELECT c FROM Character c JOIN FETCH c.user ORDER BY c.level DESC, c.currentExp DESC")
    List<Character> findTopLevels(Pageable pageable);

    // [BXH SÁT QUÁI]
    @Query("SELECT c FROM Character c JOIN FETCH c.user ORDER BY c.monsterKills DESC")
    List<Character> findTopMonsterKills(Pageable pageable);

    // Tìm theo tên (Dùng cho logic tìm bạn bè hoặc admin)
    Optional<Character> findByName(String name);
}