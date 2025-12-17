package com.echommo.enums;

public enum SlotType {
    NONE,
    // Trang bị
    WEAPON,     // Vũ khí
    ARMOR,      // Áo
    HELMET,     // Mũ
    BOOTS,      // Giày
    RING,       // Nhẫn (Thêm nếu cần)
    NECKLACE,   // Dây chuyền (Thêm nếu cần)

    // [FIX] Thêm 2 dòng này để sửa lỗi
    CONSUMABLE, // Vật phẩm tiêu hao (Máu, Mana...)
    MATERIAL    // Nguyên liệu (Gỗ, Đá, Sắt...)
}