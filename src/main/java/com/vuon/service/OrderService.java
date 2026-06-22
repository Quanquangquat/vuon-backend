package com.vuon.service;

import com.vuon.dto.request.OrderRequest;
import com.vuon.dto.response.OrderResponse;
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

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrders(User user, String status, int page, int limit) {
        var pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        var result = (status != null)
                ? orderRepository.findByUserAndStatus(user, Order.Status.valueOf(status), pageable)
                : orderRepository.findByUser(user, pageable);

        // Map sang DTO ngay trong transaction để nạp lazy items an toàn
        return PageResponse.from(result.map(OrderResponse::from));
    }

    /** Lấy entity đơn hàng (dùng nội bộ, vd huỷ đơn) */
    public Order getOrderById(UUID orderId, User user) {
        return orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> AppException.notFound("Đơn hàng không tồn tại"));
    }

    /** Lấy chi tiết đơn hàng dạng DTO cho frontend */
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(UUID orderId, User user) {
        return OrderResponse.from(getOrderById(orderId, user));
    }

    /**
     * Tạo đơn hàng. Ưu tiên danh sách items gửi thẳng từ frontend (snapshot tên/ảnh/giá),
     * không phụ thuộc giỏ server hay ID sản phẩm có khớp DB hay không → tránh "đặt hàng thành
     * công nhưng không lưu". Nếu request không có items thì dùng giỏ hàng phía server.
     */
    @Transactional
    public OrderResponse createOrder(User user, OrderRequest request) {
        Order order = Order.builder()
                .user(user)
                .paymentMethod(parsePaymentMethod(request.getPaymentMethod()))
                .shippingName(request.getShippingName())
                .shippingPhone(request.getShippingPhone())
                .shippingAddress(request.getShippingAddress())
                .shippingNote(request.getShippingNote())
                .items(new ArrayList<>())
                .build();

        int subtotal = 0;

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // Tạo đơn từ snapshot items của frontend
            for (OrderRequest.Item reqItem : request.getItems()) {
                int     qty   = Math.max(1, reqItem.getQuantity());
                Product p     = findProductSafe(reqItem.getProductId());
                int     price = reqItem.getPrice() > 0 ? reqItem.getPrice()
                              : (p != null ? p.getPrice() : 0);
                String  name  = reqItem.getProductName() != null ? reqItem.getProductName()
                              : (p != null ? p.getName() : "Sản phẩm");
                String  image = reqItem.getProductImage() != null ? reqItem.getProductImage()
                              : (p != null ? p.getImage() : null);

                OrderItem item = OrderItem.builder()
                        .order(order).product(p)
                        .productName(name).productImage(image)
                        .price(price).quantity(qty).subtotal(price * qty)
                        .build();
                order.getItems().add(item);
                subtotal += price * qty;

                if (p != null) { // chỉ trừ kho khi tìm thấy sản phẩm thật
                    p.setStock(Math.max(0, p.getStock() - qty));
                    if (p.getStock() <= 0) p.setInStock(false);
                    productRepository.save(p);
                }
            }
        } else {
            // Fallback: dùng giỏ hàng phía server
            List<CartItem> cartItems = cartItemRepository.findByUserOrderByCreatedAtDesc(user);
            if (cartItems.isEmpty()) throw AppException.badRequest("Giỏ hàng trống");
            for (CartItem cartItem : cartItems) {
                Product p   = cartItem.getProduct();
                int     qty = cartItem.getQuantity();
                OrderItem item = OrderItem.builder()
                        .order(order).product(p)
                        .productName(p.getName()).productImage(p.getImage())
                        .price(p.getPrice()).quantity(qty).subtotal(p.getPrice() * qty)
                        .build();
                order.getItems().add(item);
                subtotal += p.getPrice() * qty;
                p.setStock(Math.max(0, p.getStock() - qty));
                if (p.getStock() <= 0) p.setInStock(false);
                productRepository.save(p);
            }
        }

        int shippingFee = subtotal >= 200000 ? 0 : 30000;

        // Áp mã giảm giá
        int       discountAmount = 0;
        Promotion promo          = null;
        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            promo = promotionRepository.findActiveByCode(request.getPromoCode().toUpperCase())
                    .orElseThrow(() -> AppException.badRequest("Mã giảm giá không hợp lệ"));
            if (subtotal < promo.getMinOrder())
                throw AppException.badRequest("Đơn hàng tối thiểu " + promo.getMinOrder() + "đ để dùng mã này");
            discountAmount = switch (promo.getType()) {
                case percent  -> (int) (subtotal * promo.getDiscount() / 100.0);
                case fixed    -> Math.min(promo.getDiscount(), subtotal);
                case freeship -> shippingFee;
            };
            promo.setUsedCount(promo.getUsedCount() + 1);
            promotionRepository.save(promo);
        }

        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setDiscountAmount(discountAmount);
        order.setTotal(subtotal + shippingFee - discountAmount);
        order.setPromotion(promo);

        order = orderRepository.save(order);

        // Xoá giỏ hàng server (nếu có) + cập nhật thống kê user
        cartItemRepository.deleteByUser(user);
        user.setTotalOrders(user.getTotalOrders() + 1);
        user.setTotalSpent(user.getTotalSpent() + order.getTotal());
        userRepository.save(user);

        return OrderResponse.from(order);
    }

    /** Map giá trị paymentMethod từ frontend về enum hợp lệ (mặc định COD) */
    private Order.PaymentMethod parsePaymentMethod(String m) {
        if (m == null) return Order.PaymentMethod.COD;
        return switch (m.toUpperCase()) {
            case "BANK", "BANK_TRANSFER" -> Order.PaymentMethod.bank_transfer;
            case "WALLET", "E_WALLET"    -> Order.PaymentMethod.e_wallet;
            case "MOMO"                  -> Order.PaymentMethod.momo;
            case "ZALOPAY"               -> Order.PaymentMethod.zalopay;
            default                       -> Order.PaymentMethod.COD;
        };
    }

    /** Tìm product theo id; trả null nếu id không phải UUID (mock) hoặc không tồn tại */
    private Product findProductSafe(String id) {
        if (id == null || id.isBlank()) return null;
        try { return productRepository.findById(UUID.fromString(id)).orElse(null); }
        catch (IllegalArgumentException e) { return null; }
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
