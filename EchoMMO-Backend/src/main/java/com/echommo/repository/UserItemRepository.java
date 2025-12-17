package com.echommo.repository;

import com.echommo.entity.UserItem;
import com.echommo.enums.SlotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    // [THÊM MỚI] Hàm cơ bản để tìm theo CharId (Sửa lỗi cannot find symbol)
    List<UserItem> findByCharacter_CharId(Integer charId);

    // 1. Get Inventory (Newest items first)
    List<UserItem> findByCharacter_CharIdOrderByAcquiredAtDesc(Integer charId);

    // 2. Get currently equipped items
    List<UserItem> findByCharacter_CharIdAndIsEquippedTrue(Integer charId);

    // 3. Find item currently in a specific slot
    @Query("SELECT ui FROM UserItem ui WHERE ui.character.charId = :charId AND ui.isEquipped = true AND ui.item.slotType = :slotType")
    Optional<UserItem> findEquippedItemBySlot(@Param("charId") Integer charId, @Param("slotType") SlotType slotType);

    // 4. Find specific item for Stacking
    Optional<UserItem> findByCharacter_CharIdAndItem_ItemId(Integer charId, Integer itemId);

    // 5. Find a specific unique item instance
    Optional<UserItem> findByUserItemIdAndCharacter_CharId(Long userItemId, Integer charId);

    // 6. Find material by name
    Optional<UserItem> findByCharacter_CharIdAndItem_Name(Integer charId, String name);
}