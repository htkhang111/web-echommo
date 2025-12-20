package com.echommo.repository;

import com.echommo.entity.Character;
import com.echommo.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
// [FIX] ID là Integer (khớp với Entity Character)
public interface CharacterRepository extends JpaRepository<Character, Integer> {

    Optional<Character> findByUser(User user);

    // [FIX] Thêm hàm này để Service gọi được findByUserId
    @Query("SELECT c FROM Character c WHERE c.user.userId = :userId")
    Optional<Character> findByUserId(@Param("userId") Integer userId);

    // [FIX] Method dự phòng đúng chuẩn JPA
    Optional<Character> findByUser_UserId(Integer userId);

    boolean existsByName(String name);

    // [FIX] Cho LeaderboardService
    @Query("SELECT c FROM Character c ORDER BY c.level DESC, c.currentExp DESC")
    List<Character> findTopLevels(Pageable pageable);

    // [FIX] Tên trường trong Entity là monsterKills (check lại Entity Character của bạn)
    @Query("SELECT c FROM Character c ORDER BY c.monsterKills DESC")
    List<Character> findTopMonsterKills(Pageable pageable);
}