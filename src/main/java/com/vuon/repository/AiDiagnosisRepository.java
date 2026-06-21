package com.vuon.repository;

import com.vuon.model.AiDiagnosis;
import com.vuon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiDiagnosisRepository extends JpaRepository<AiDiagnosis, UUID> {
    List<AiDiagnosis> findByUserOrderByCreatedAtDesc(User user);
}
