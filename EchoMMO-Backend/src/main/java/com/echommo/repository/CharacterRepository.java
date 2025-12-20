package com.echommo.repository;

import com.echommo.entity.Character;
import com.echommo.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    Optional<Character> findByUser(User user);

    // [FIX] Thêm method này vì CharacterService đang gọi nó
    Optional<Character> findByUser_UserId(Long userId);

    boolean existsByName(String name);

    // [FIX] Cho LeaderboardService
    @Query("SELECT c FROM Character c ORDER BY c.level DESC, c.currentExp DESC")
    List<Character> findTopLevels(Pageable pageable);

    @Query("SELECT c FROM Character c ORDER BY c.monstersKilled DESC") // Giả sử có field monstersKilled
    List<Character> findTopMonsterKills(Pageable pageable);
}