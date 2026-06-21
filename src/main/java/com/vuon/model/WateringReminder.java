package com.vuon.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "watering_reminders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WateringReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "plant_name", nullable = false, length = 100)
    private String plantName;

    @Column(nullable = false)
    @Builder.Default
    private int frequency = 1;

    @Column(name = "frequency_unit", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FrequencyUnit frequencyUnit = FrequencyUnit.day;

    @Column(name = "last_watered")
    private LocalDateTime lastWatered;

    @Column(name = "next_water")
    private LocalDateTime nextWater;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum FrequencyUnit { day, week }
}
