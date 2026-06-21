package com.vuon.controller;

import com.vuon.model.CartItem;
import com.vuon.model.User;
import com.vuon.repository.UserRepository;
import com.vuon.service.CartService;
import com.vuon.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService    cartService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getCart(@AuthenticationPrincipal UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<CartItem> items = cartService.getCart(user);
        int total = items.stream().mapToInt(i -> i.getProduct().getPrice() * i.getQuantity()).sum();
        return ApiResponse.ok(Map.of("items", items, "total", total));
    }

    @PostMapping
    public ResponseEntity<?> addToCart(@AuthenticationPrincipal UUID userId,
                                       @RequestBody Map<String, Object> body) {
        User user   = userRepository.findById(userId).orElseThrow();
        UUID pid    = UUID.fromString((String) body.get("productId"));
        int  qty    = body.containsKey("quantity") ? (int) body.get("quantity") : 1;
        cartService.addToCart(user, pid, qty);
        return ApiResponse.ok(null, "Đã thêm vào giỏ hàng");
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> updateQuantity(@AuthenticationPrincipal UUID userId,
                                            @PathVariable UUID productId,
                                            @RequestBody Map<String, Integer> body) {
        User user = userRepository.findById(userId).orElseThrow();
        cartService.updateQuantity(user, productId, body.get("quantity"));
        return ApiResponse.ok(null, "Đã cập nhật giỏ hàng");
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeItem(@AuthenticationPrincipal UUID userId,
                                        @PathVariable UUID productId) {
        User user = userRepository.findById(userId).orElseThrow();
        cartService.removeFromCart(user, productId);
        return ApiResponse.ok(null, "Đã xoá khỏi giỏ hàng");
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        cartService.clearCart(user);
        return ApiResponse.ok(null, "Đã xoá giỏ hàng");
    }
}
