package com.vuon.repository;

import com.vuon.model.CartItem;
import com.vuon.model.Product;
import com.vuon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findByUserOrderByCreatedAtDesc(User user);
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    void deleteByUser(User user);
    void deleteByUserAndProduct(User user, Product product);
}
