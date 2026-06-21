package com.vuon.repository;

import com.vuon.model.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, UUID> {
    Page<BlogPost> findByIsPublishedTrue(Pageable pageable);
    Page<BlogPost> findByIsPublishedTrueAndCategory(String category, Pageable pageable);
    Optional<BlogPost> findBySlugAndIsPublishedTrue(String slug);
}
