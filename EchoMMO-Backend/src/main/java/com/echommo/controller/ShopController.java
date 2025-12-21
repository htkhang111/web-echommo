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

@RestController
@RequestMapping("/api/shop")
public class ShopController extends BaseController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserItemRepository userItemRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/items")
    public ResponseEntity<?> getShopItems() {
        // Chỉ hiện vật phẩm Shop (Vô hạn)
        List<Item> items = itemRepository.findByIsSystemItemTrue();
        return ResponseEntity.ok(items);
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

        // Chặn mua nếu là hàng Drop hoặc Limited
        if (Boolean.FALSE.equals(item.getIsSystemItem())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vật phẩm này không được bán trong Shop (Limited/Drop only)."));
        }

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        int quantity = request.getQuantity() > 0 ? request.getQuantity() : 1;
        BigDecimal totalCost = item.getBasePrice().multiply(BigDecimal.valueOf(quantity));

        if (wallet.getGold().compareTo(totalCost) < 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không đủ ngân lượng!"));
        }

        wallet.setGold(wallet.getGold().subtract(totalCost));
        walletRepository.save(wallet);

        UserItem userItem = userItemRepository.findByCharacterAndItem(character, item)
                .orElse(new UserItem());

        if (userItem.getUserItemId() == null) {
            userItem.setCharacter(character);
            userItem.setItem(item);
            userItem.setQuantity(0);
        }

        userItem.setQuantity(userItem.getQuantity() + quantity);
        userItemRepository.save(userItem);

        return ResponseEntity.ok(Map.of("message", "Giao dịch thành công!", "goldBalance", wallet.getGold()));
    }
}