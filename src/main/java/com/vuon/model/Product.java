package com.vuon.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity đại diện cho sản phẩm bộ kit trồng cây
 */
@Entity
@Table(name = "products")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(nullable = false)
    private int price;

    @Column
    private String image;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Column(length = 50)
    private String light;

    @Column(name = "care_level", length = 200)
    private String careLevel;

    @Column(nullable = false)
    @Builder.Default
    private double rating = 0;

    @Column(name = "reviews_count", nullable = false)
    @Builder.Default
    private int reviewsCount = 0;

    @Column(name = "in_stock", nullable = false)
    @Builder.Default
    private boolean inStock = true;

    @Column(nullable = false)
    @Builder.Default
    private int stock = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Category { vegetable, flower, combo, accessory }

    public enum Difficulty {
        Dễ, Trung_bình, Khó;

        // Map từ String DB về enum
        public static Difficulty fromString(String s) {
            return switch (s) {
                case "Trung bình" -> Trung_bình;
                case "Khó"        -> Khó;
                default           -> Dễ;
            };
        }
    }
}
