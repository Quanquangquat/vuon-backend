package com.vuon.repository;

import com.vuon.model.Order;
import com.vuon.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByUser(User user, Pageable pageable);

    Page<Order> findByUserAndStatus(User user, Order.Status status, Pageable pageable);

    Optional<Order> findByIdAndUser(UUID id, User user);

    // Tổng doanh thu
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status = 'completed'")
    long getTotalRevenue();

    // Đếm theo trạng thái
    long countByStatus(Order.Status status);

    // Kiểm tra user đã mua và nhận hàng sản phẩm nào chưa
    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items i " +
           "WHERE o.user = :user AND i.product.id = :productId AND o.status = 'completed'")
    boolean hasUserPurchasedProduct(@Param("user") User user,
                                    @Param("productId") UUID productId);
}
