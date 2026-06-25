package com.vuon.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cài đặt giao diện toàn site (1 dòng duy nhất).
 * Logo / favicon lưu URL hoặc data URL (base64) nên dùng TEXT.
 */
@Entity
@Table(name = "site_settings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SiteSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100)
    @Builder.Default
    private String brandName = "VƯƠN";

    @Column(columnDefinition = "TEXT")
    private String logoUrl;        // logo header

    @Column(columnDefinition = "TEXT")
    private String logoWhiteUrl;   // logo footer (nền tối)

    @Column(columnDefinition = "TEXT")
    private String faviconUrl;

    @Column(columnDefinition = "TEXT")
    private String footerDescription;

    @Column(length = 50)
    private String footerPhone;

    @Column(length = 150)
    private String footerEmail;

    @Column(columnDefinition = "TEXT")
    private String footerAddress;

    @Column(columnDefinition = "TEXT")
    private String facebookUrl;

    @Column(columnDefinition = "TEXT")
    private String instagramUrl;

    @Column(columnDefinition = "TEXT")
    private String youtubeUrl;

    @Column(columnDefinition = "TEXT")
    private String promoBarText;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
