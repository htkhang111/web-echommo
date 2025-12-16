package com.echommo.repository;

import com.echommo.entity.BattleSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
// [FIX] Đổi Integer thành Long vì ID trong BattleSession là Long
public interface BattleSessionRepository extends JpaRepository<BattleSession, Long> {

    // Tìm session theo Character ID
    Optional<BattleSession> findByCharacter_CharId(Integer charId);

    // Xóa session
    void deleteByCharacter_CharId(Integer charId);
}