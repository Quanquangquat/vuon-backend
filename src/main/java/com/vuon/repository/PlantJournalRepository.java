package com.vuon.repository;

import com.vuon.model.PlantJournal;
import com.vuon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlantJournalRepository extends JpaRepository<PlantJournal, UUID> {
    List<PlantJournal> findByUserOrderByCreatedAtDesc(User user);
    Optional<PlantJournal> findByIdAndUser(UUID id, User user);
}
