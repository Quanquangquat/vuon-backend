package com.vuon.repository;

import com.vuon.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    // Lấy sản phẩm đang active, có thể lọc theo category
    Page<Product> findByIsActiveTrueAndCategory(Product.Category category, Pageable pageable);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    // Tìm kiếm theo tên hoặc mô tả
    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
           "AND (:category IS NULL OR p.category = :category) " +
           "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchProducts(@Param("category") Product.Category category,
                                 @Param("search") String search,
                                 Pageable pageable);

    Optional<Product> findByIdAndIsActiveTrue(UUID id);
}
