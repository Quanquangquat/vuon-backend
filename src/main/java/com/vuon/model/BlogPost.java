package com.vuon.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "blog_posts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, unique = true, length = 350)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String excerpt;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column
    private String image;

    @Column(nullable = false, length = 100)
    @Builder.Default
    private String author = "Admin VƯƠN";

    @Column(length = 50)
    private String category;

    @Column(nullable = false)
    @Builder.Default
    private int views = 0;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private boolean isPublished = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
