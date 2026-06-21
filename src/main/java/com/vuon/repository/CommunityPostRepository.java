package com.vuon.repository;

import com.vuon.model.CommunityPost;
import com.vuon.model.CommunityComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, UUID> {
    Page<CommunityPost> findByIsVisibleTrueOrderByCreatedAtDesc(Pageable pageable);
}
