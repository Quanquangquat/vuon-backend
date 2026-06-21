package com.vuon.repository;

import com.vuon.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.validUntil > CURRENT_TIMESTAMP")
    List<Promotion> findAllActive();

    @Query("SELECT p FROM Promotion p WHERE p.code = :code AND p.isActive = true " +
           "AND p.validUntil > CURRENT_TIMESTAMP")
    Optional<Promotion> findActiveByCode(String code);
}
