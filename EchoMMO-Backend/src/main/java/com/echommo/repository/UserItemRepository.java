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

    // 1. Get Inventory (Newest items first)
    List<UserItem> findByCharacter_CharIdOrderByAcquiredAtDesc(Integer charId);

    // 2. Get currently equipped items (For calculating Battle Power)
    List<UserItem> findByCharacter_CharIdAndIsEquippedTrue(Integer charId);

    // 3. Find item currently in a specific slot (to unequip it before equipping new one)
    @Query("SELECT ui FROM UserItem ui WHERE ui.character.charId = :charId AND ui.isEquipped = true AND ui.item.slotType = :slotType")
    Optional<UserItem> findEquippedItemBySlot(@Param("charId") Integer charId, @Param("slotType") SlotType slotType);

    // 4. Find specific item for Stacking (Consumables/Materials only)
    // Warning: Use only for items where isStackable = true
    Optional<UserItem> findByCharacter_CharIdAndItem_ItemId(Integer charId, Integer itemId);

    // 5. Find a specific unique item instance (Safe for Equipment)
    // Use this when the user clicks "Equip" on a specific sword in their bag
    Optional<UserItem> findByUserItemIdAndCharacter_CharId(Long userItemId, Integer charId);

    // 6. Find material by name (Optional - for crafting quests)
    Optional<UserItem> findByCharacter_CharIdAndItem_Name(Integer charId, String name);
}