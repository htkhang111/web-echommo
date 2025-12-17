package com.echommo.repository;

import com.echommo.entity.MarketListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MarketListingRepository extends JpaRepository<MarketListing, Integer> {
    // Dùng hàm chuẩn JPA, không dùng @Query để tránh lỗi SQL
    List<MarketListing> findByStatusOrderByCreatedAtDesc(String status);

    List<MarketListing> findBySeller_UserIdAndStatus(Integer userId, String status);
}