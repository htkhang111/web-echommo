package com.echommo.repository;

import com.echommo.entity.Wallet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    Optional<Wallet> findByUser_UserId(Integer userId);

    // [FIX] Sửa 'w.balance' thành 'w.gold' để khớp với Entity
    @Query("SELECT w FROM Wallet w JOIN FETCH w.user ORDER BY w.gold DESC")
    List<Wallet> findTopWealth(Pageable pageable);
}