package com.vuon.controller;

import com.vuon.dto.request.OrderRequest;
import com.vuon.model.User;
import com.vuon.repository.UserRepository;
import com.vuon.service.OrderService;
import com.vuon.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService   orderService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getOrders(@AuthenticationPrincipal UUID userId,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(defaultValue = "1")  int page,
                                       @RequestParam(defaultValue = "10") int limit) {
        User user = userRepository.findById(userId).orElseThrow();
        return ApiResponse.ok(orderService.getOrders(user, status, page, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@AuthenticationPrincipal UUID userId,
                                      @PathVariable UUID id) {
        User user = userRepository.findById(userId).orElseThrow();
        return ApiResponse.ok(Map.of("order", orderService.getOrderDetail(id, user)));
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@AuthenticationPrincipal UUID userId,
                                         @Valid @RequestBody OrderRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        var order = orderService.createOrder(user, request);
        return ApiResponse.created(order, "Đặt hàng thành công!");
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@AuthenticationPrincipal UUID userId,
                                         @PathVariable UUID id) {
        User user = userRepository.findById(userId).orElseThrow();
        orderService.cancelOrder(id, user);
        return ApiResponse.ok(null, "Đã huỷ đơn hàng");
    }
}
