package com.echommo.repository;

import com.echommo.entity.PvpQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PvpQueueRepository extends JpaRepository<PvpQueue, Integer> {

    Optional<PvpQueue> findByCharId(Integer charId);

    // Tìm người khác mình, đang tìm trận, level trong khoảng cho phép, vào sớm nhất
    @Query(value = "SELECT * FROM pvp_queue " +
            "WHERE char_id != :myCharId " +
            "AND status = 'SEARCHING' " +
            "AND level BETWEEN :minLevel AND :maxLevel " +
            "ORDER BY joined_at ASC LIMIT 1", nativeQuery = true)
    Optional<PvpQueue> findMatchCandidate(Integer myCharId, Integer minLevel, Integer maxLevel);
}