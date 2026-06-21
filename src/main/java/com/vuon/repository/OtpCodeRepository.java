package com.vuon.repository;

import com.vuon.model.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    @Query("SELECT o FROM OtpCode o WHERE o.email = :email AND o.code = :code " +
           "AND o.type = :type AND o.used = false AND o.expiresAt > CURRENT_TIMESTAMP " +
           "ORDER BY o.createdAt DESC")
    Optional<OtpCode> findValidOtp(String email, String code, OtpCode.Type type);
}
