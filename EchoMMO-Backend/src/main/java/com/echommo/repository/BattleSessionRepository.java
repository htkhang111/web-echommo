package com.echommo.repository;

import com.echommo.entity.BattleSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // [QUAN TRỌNG] Phải import List

@Repository
public interface BattleSessionRepository extends JpaRepository<BattleSession, Long> {

    // [FIX QUAN TRỌNG NHẤT]
    // Đổi từ Optional<?> thành List<?>
    // Ý nghĩa: "Tìm cho tôi TẤT CẢ các session của nhân vật này"
    // -> Dù có 3 hay 10 dòng trùng nhau thì Java cũng không báo lỗi nữa.
    List<BattleSession> findByCharacter_CharId(Integer charId);
    // Xóa session theo ID nhân vật (Đổi Integer -> Long cho đồng bộ)
    void deleteByCharacter_CharId(Long charId);
}