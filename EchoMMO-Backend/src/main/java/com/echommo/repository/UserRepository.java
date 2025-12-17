package com.echommo.repository;

import com.echommo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Hàm cũ (Mặc định Lazy Load -> Không lấy Wallet)
    Optional<User> findByUsername(String username);

    // [QUAN TRỌNG] Hàm mới: Lấy User kèm theo Wallet (Vàng/Ngọc)
    // Dùng LEFT JOIN FETCH để ép Hibernate tải thông tin ví ngay lập tức trong 1 câu truy vấn
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.wallet WHERE u.username = :username")
    Optional<User> findByUsernameWithWallet(@Param("username") String username);

    // Các hàm kiểm tra khác
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}