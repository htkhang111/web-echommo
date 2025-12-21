package com.echommo.repository;

import com.echommo.entity.PvpMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PvpMatchRepository extends JpaRepository<PvpMatch, Long> {

    // SỬA TÊN HÀM NÀY: findActiveMatchByCharId
    @Query("SELECT m FROM PvpMatch m WHERE (m.player1.id = :charId OR m.player2.id = :charId) AND m.status IN ('PENDING', 'ACTIVE')")
    Optional<PvpMatch> findActiveMatchByCharId(@Param("charId") Integer charId);
}