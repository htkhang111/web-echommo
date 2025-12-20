package com.echommo.repository;

import com.echommo.entity.PvpMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PvpMatchRepository extends JpaRepository<PvpMatch, Long> {

    // Tìm trận đấu đang diễn ra (PENDING hoặc ACTIVE) của nhân vật
    // Logic: Nhân vật có thể là Player1 HOẶC Player2
    @Query("SELECT m FROM PvpMatch m WHERE (m.player1.charId = :charId OR m.player2.charId = :charId) AND m.status IN ('PENDING', 'ACTIVE')")
    Optional<PvpMatch> findActiveMatchByCharId(@Param("charId") Integer charId);
}