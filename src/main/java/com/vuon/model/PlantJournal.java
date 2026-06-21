package com.vuon.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "plant_journal")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PlantJournal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "plant_name", nullable = false, length = 100)
    private String plantName;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column
    private String image;

    @Column(name = "health_status", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private HealthStatus healthStatus = HealthStatus.good;

    @Column(name = "planted_at")
    private LocalDateTime plantedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum HealthStatus { good, warning, danger }
}
