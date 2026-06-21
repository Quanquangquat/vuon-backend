package com.vuon.service;

import com.vuon.dto.request.OrderRequest;
import com.vuon.dto.response.PageResponse;
import com.vuon.exception.AppException;
import com.vuon.model.*;
import com.vuon.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service xử lý đơn hàng
 * Dùng @Transactional để đảm bảo tính toàn vẹn dữ liệu:
 * - Trừ kho, tạo đơn, xoá giỏ hàng trong cùng 1 transaction
 * - Nếu có lỗi → rollback tất cả
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository     orderRepository;
    private final CartItemRepository  cartItemRepository;
    private final ProductRepository   productRepository;
    private final PromotionRepository promotionRepository;
    private final UserRepository      userRepository;

    public PageResponse<Order> getOrders(User user, String status, int page, int limit) {
        var pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        var result = (status != null)
                ? orderRepository.findByUserAndStatus(user, Order.Status.valueOf(status), pageable)
                : orderRepository.findByUser(user, pageable);

        return PageResponse.from(result);
    }

    public Order getOrderById(UUID orderId, User user) {
        return orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> AppException.notFound("Đơn hàng không tồn tại"));
    }

    /** Tạo đơn hàng từ giỏ hàng - xử lý trong 1 transaction */
    @Transactional
    public Order createOrder(User user, OrderRequest request) {
        // 1. Lấy giỏ hàng
        List<CartItem> cartItems = cartItemRepository.findByUserOrderByCreatedAtDesc(user);
        if (cartItems.isEmpty()) {
            throw AppException.badRequest("Giỏ hàng trống");
        }

        // 2. Kiểm tra tồn kho
        for (CartItem item : cartItems) {
            Product p = item.getProduct();
            if (!p.isInStock() || p.getStock() < item.getQuantity()) {
                throw AppException.badRequest(
                        "Sản phẩm \"" + p.getName() + "\" không đủ số lượng tồn kho"
                );
            }
        }

        // 3. Tính tiền
        int subtotal    = cartItems.stream()
                                   .mapToInt(i -> i.getProduct().getPrice() * i.getQuantity())
                                   .sum();
        int shippingFee = subtotal >= 200000 ? 0 : 30000;

        // 4. Áp mã giảm giá
        int       discountAmount = 0;
        Promotion promo          = null;

        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            promo = promotionRepository.findActiveByCode(request.getPromoCode().toUpperCase())
                    .orElseThrow(() -> AppException.badRequest("Mã giảm giá không hợp lệ"));

            if (subtotal < promo.getMinOrder()) {
                throw AppException.badRequest(
                        "Đơn hàng tối thiểu " + promo.getMinOrder() + "đ để dùng mã này"
                );
            }

            discountAmount = switch (promo.getType()) {
                case percent  -> (int) (subtotal * promo.getDiscount() / 100.0);
                case fixed    -> Math.min(promo.getDiscount(), subtotal);
                case freeship -> shippingFee;
            };

            promo.setUsedCount(promo.getUsedCount() + 1);
            promotionRepository.save(promo);
        }

        int total = subtotal + shippingFee - discountAmount;

        // 5. Tạo đơn hàng
        Order order = Order.builder()
                .user(user)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .shippingFee(shippingFee)
                .total(total)
                .paymentMethod(Order.PaymentMethod.valueOf(
                        request.getPaymentMethod() != null ? request.getPaymentMethod() : "COD"))
                .promotion(promo)
                .shippingName(request.getShippingName())
                .shippingPhone(request.getShippingPhone())
                .shippingAddress(request.getShippingAddress())
                .shippingNote(request.getShippingNote())
                .items(new ArrayList<>())
                .build();

        // 6. Tạo order items + trừ kho
        for (CartItem cartItem : cartItems) {
            Product p = cartItem.getProduct();

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(p)
                    .productName(p.getName())
                    .productImage(p.getImage())
                    .price(p.getPrice())
                    .quantity(cartItem.getQuantity())
                    .subtotal(p.getPrice() * cartItem.getQuantity())
                    .build();
            order.getItems().add(item);

            // Trừ tồn kho
            p.setStock(p.getStock() - cartItem.getQuantity());
            if (p.getStock() <= 0) p.setInStock(false);
            productRepository.save(p);
        }

        order = orderRepository.save(order);

        // 7. Xoá giỏ hàng
        cartItemRepository.deleteByUser(user);

        // 8. Cập nhật thống kê user
        user.setTotalOrders(user.getTotalOrders() + 1);
        user.setTotalSpent(user.getTotalSpent() + total);
        userRepository.save(user);

        return order;
    }

    @Transactional
    public void cancelOrder(UUID orderId, User user) {
        Order order = getOrderById(orderId, user);
        if (order.getStatus() != Order.Status.processing) {
            throw AppException.badRequest("Chỉ có thể huỷ đơn hàng đang xử lý");
        }
        order.setStatus(Order.Status.cancelled);
        orderRepository.save(order);
    }
}
