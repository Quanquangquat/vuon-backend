package com.vuon.service;

import com.vuon.exception.AppException;
import com.vuon.model.PlantJournal;
import com.vuon.model.User;
import com.vuon.repository.PlantJournalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlantJournalService {

    private final PlantJournalRepository journalRepository;

    public List<PlantJournal> getJournal(User user) {
        return journalRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public PlantJournal addEntry(User user, String plantName, String notes,
                                  String imageUrl, String healthStatus, LocalDateTime plantedAt) {
        if (plantName == null || plantName.isBlank()) {
            throw AppException.badRequest("Tên cây không được để trống");
        }
        PlantJournal.HealthStatus status = parseHealth(healthStatus);
        return journalRepository.save(PlantJournal.builder()
                .user(user).plantName(plantName).notes(notes)
                .image(imageUrl).healthStatus(status)
                .plantedAt(plantedAt != null ? plantedAt : LocalDateTime.now())
                .build());
    }

    @Transactional
    public PlantJournal updateEntry(UUID entryId, User user, String notes,
                                     String imageUrl, String healthStatus) {
        PlantJournal entry = journalRepository.findByIdAndUser(entryId, user)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy nhật ký"));
        if (notes        != null) entry.setNotes(notes);
        if (imageUrl     != null) entry.setImage(imageUrl);
        if (healthStatus != null) entry.setHealthStatus(parseHealth(healthStatus));
        return journalRepository.save(entry);
    }

    @Transactional
    public void deleteEntry(UUID entryId, User user) {
        PlantJournal entry = journalRepository.findByIdAndUser(entryId, user)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy nhật ký"));
        journalRepository.delete(entry);
    }

    private PlantJournal.HealthStatus parseHealth(String s) {
        try { return PlantJournal.HealthStatus.valueOf(s); }
        catch (Exception e) { return PlantJournal.HealthStatus.good; }
    }
}
