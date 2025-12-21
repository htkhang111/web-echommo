package com.echommo.repository;

import com.echommo.entity.Item;
import com.echommo.enums.Rarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    // [FIX] Thêm lại method này để sửa lỗi biên dịch
    Optional<Item> findByName(String name);

    // Các method mới cho tính năng Dump
    Optional<Item> findByCode(String code);

    List<Item> findByRarity(Rarity rarity);

    List<Item> findByRarityAndCodeNotLike(Rarity rarity, String codePattern);
}