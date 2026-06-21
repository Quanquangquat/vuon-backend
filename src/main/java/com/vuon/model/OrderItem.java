package com.vuon.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entity đại diện cho từng dòng sản phẩm trong đơn hàng
 */
@Entity
@Table(name = "order_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // Lưu snapshot tên và ảnh tại thời điểm đặt hàng
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "product_image")
    private String productImage;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int subtotal;
}
