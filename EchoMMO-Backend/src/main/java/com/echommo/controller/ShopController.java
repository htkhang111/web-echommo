package com.echommo.controller;

import com.echommo.dto.BuyItemRequest;
import com.echommo.entity.Character;
import com.echommo.entity.Item;
import com.echommo.entity.User;
import com.echommo.entity.UserItem;
import com.echommo.entity.Wallet;
import com.echommo.repository.CharacterRepository;
import com.echommo.repository.ItemRepository;
import com.echommo.repository.UserItemRepository;
import com.echommo.repository.UserRepository;
import com.echommo.repository.WalletRepository;
import com.echommo.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shop")
public class ShopController extends BaseController {

    @Autowired private ItemRepository itemRepository;
    @Autowired private UserItemRepository userItemRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CharacterRepository characterRepository;
    @Autowired private JwtUtils jwtUtils;

    @GetMapping("/items")
    public ResponseEntity<?> getShopItems() {
        // 1. Lấy tất cả vật phẩm hệ thống (isSystemItem = true)
        List<Item> allItems = itemRepository.findByIsSystemItemTrue();

        // 2. [LOGIC MỚI] Lọc bỏ Tool Tier 5 (Hàng Drop/Craft, không bán shop)
        List<Item> filteredItems = allItems.stream()
                .filter(item -> {
                    // Nếu là TOOL và Tier >= 5 -> Ẩn khỏi shop
                    if ("TOOL".equals(item.getType()) && item.getTier() != null && item.getTier() >= 5) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(filteredItems);
    }

    @PostMapping("/buy")
    @Transactional
    public ResponseEntity<?> buyItem(@RequestBody BuyItemRequest request, @RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUserNameFromJwtToken(token.substring(7));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Character character = characterRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Chưa tạo nhân vật!"));

        Item item = itemRepository.findById(request.getItemId().intValue())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // Check 1: Chặn mua nếu là hàng Drop/Limited
        if (Boolean.FALSE.equals(item.getIsSystemItem())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vật phẩm này không được bán trong Shop (Limited/Drop only)."));
        }

        // Check 2: Chặn mua Tool Tier 5 (Đề phòng hack request API)
        if ("TOOL".equals(item.getType()) && item.getTier() != null && item.getTier() >= 5) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vật phẩm Huyền Thoại không thể mua bằng tiền thường!"));
        }

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        int quantity = request.getQuantity() > 0 ? request.getQuantity() : 1;
        BigDecimal totalCost = item.getBasePrice().multiply(BigDecimal.valueOf(quantity));

        if (wallet.getGold().compareTo(totalCost) < 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không đủ ngân lượng!"));
        }

        // Trừ tiền
        wallet.setGold(wallet.getGold().subtract(totalCost));
        walletRepository.save(wallet);

        // Cộng đồ (Logic Stack: Nếu có rồi thì cộng số lượng, chưa có thì tạo mới)
        UserItem userItem = userItemRepository.findByCharacterAndItem(character, item)
                .orElse(new UserItem());

        if (userItem.getUserItemId() == null) {
            userItem.setCharacter(character);
            userItem.setItem(item);
            userItem.setQuantity(0);

            // [QUAN TRỌNG] Khởi tạo độ bền từ Template khi mua mới
            userItem.setMaxDurability(item.getMaxDurability() != null ? item.getMaxDurability() : 100);
            userItem.setCurrentDurability(userItem.getMaxDurability());
        }

        userItem.setQuantity(userItem.getQuantity() + quantity);
        userItemRepository.save(userItem);

        return ResponseEntity.ok(Map.of("message", "Giao dịch thành công!", "goldBalance", wallet.getGold()));
    }
}