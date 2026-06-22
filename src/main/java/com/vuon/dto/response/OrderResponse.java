package com.vuon.dto.response;

import com.vuon.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO trả về cho frontend khi xem đơn hàng.
 * Dùng DTO thay vì entity để tránh lỗi lazy-loading / đệ quy khi serialize JSON.
 */
@Data
@AllArgsConstructor
public class OrderResponse {

    private String        id;
    private String        status;
    private String        paymentMethod;
    private int           subtotal;
    private int           shippingFee;
    private int           discountAmount;
    private int           total;
    private String        shippingName;
    private String        shippingPhone;
    private String        shippingAddress;
    private String        shippingNote;
    private LocalDateTime createdAt;
    private List<Item>    items;

    @Data
    @AllArgsConstructor
    public static class Item {
        private String productId;
        private String productName;
        private String productImage;
        private int    price;
        private int    quantity;
        private int    subtotal;
    }

    public static OrderResponse from(Order o) {
        List<Item> items = o.getItems().stream()
                .map(it -> new Item(
                        it.getProduct() != null ? it.getProduct().getId().toString() : null,
                        it.getProductName(),
                        it.getProductImage(),
                        it.getPrice(),
                        it.getQuantity(),
                        it.getSubtotal()))
                .collect(Collectors.toList());

        return new OrderResponse(
                o.getId().toString(),
                o.getStatus().name(),
                o.getPaymentMethod().name(),
                o.getSubtotal(),
                o.getShippingFee(),
                o.getDiscountAmount(),
                o.getTotal(),
                o.getShippingName(),
                o.getShippingPhone(),
                o.getShippingAddress(),
                o.getShippingNote(),
                o.getCreatedAt(),
                items);
    }
}
