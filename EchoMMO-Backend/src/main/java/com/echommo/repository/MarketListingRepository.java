package com.echommo.repository;

import com.echommo.entity.MarketListing;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MarketListingRepository extends JpaRepository<MarketListing, Integer> {

    // Tìm các tin đăng đang ACTIVE, sắp xếp mới nhất
    List<MarketListing> findByStatusOrderByCreatedAtDesc(String status);

    // Tìm tin đăng của user cụ thể (Seller ID là Integer)
    List<MarketListing> findBySeller_UserIdAndStatus(Integer userId, String status);
    @Modifying
    @Transactional
    @Query("DELETE FROM MarketListing m WHERE m.item.itemId = :itemId")
    void deleteAllByItemId(Long itemId);

}