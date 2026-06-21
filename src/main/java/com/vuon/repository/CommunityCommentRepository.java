package com.vuon.repository;

import com.vuon.model.CommunityComment;
import com.vuon.model.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommunityCommentRepository extends JpaRepository<CommunityComment, UUID> {
    List<CommunityComment> findByPostOrderByCreatedAtAsc(CommunityPost post);
    long countByPost(CommunityPost post);
}
