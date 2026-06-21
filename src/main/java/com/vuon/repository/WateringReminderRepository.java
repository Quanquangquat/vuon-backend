package com.vuon.repository;

import com.vuon.model.User;
import com.vuon.model.WateringReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WateringReminderRepository extends JpaRepository<WateringReminder, UUID> {
    List<WateringReminder> findByUserAndIsActiveTrueOrderByNextWaterAsc(User user);
    Optional<WateringReminder> findByIdAndUser(UUID id, User user);
}
