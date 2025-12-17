package com.echommo.repository;

import com.echommo.entity.MarketListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MarketListingRepository extends JpaRepository<MarketListing, Integer> {

    // 1. Lấy danh sách chợ (Người mua xem)
    // Sắp xếp người bán mới nhất lên đầu
    List<MarketListing> findByStatusOrderByCreatedAtDesc(String status);

    // 2. Lấy danh sách hàng mình đang treo bán (Người bán xem)
    // Map vào field 'seller' (User) trong Entity MarketListing
    List<MarketListing> findBySeller_UserIdAndStatus(Integer userId, String status);
}