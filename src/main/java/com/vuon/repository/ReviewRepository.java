package com.vuon.repository;

import com.vuon.model.Product;
import com.vuon.model.Review;
import com.vuon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByProductOrderByCreatedAtDesc(Product product);
    Optional<Review> findByProductAndUser(Product product, User user);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product")
    Double getAverageRating(Product product);

    long countByProduct(Product product);
}
