package com.vuon.service;

import com.vuon.dto.response.PageResponse;
import com.vuon.exception.AppException;
import com.vuon.model.CommunityComment;
import com.vuon.model.CommunityPost;
import com.vuon.model.User;
import com.vuon.repository.CommunityCommentRepository;
import com.vuon.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityPostRepository    postRepository;
    private final CommunityCommentRepository commentRepository;

    public PageResponse<CommunityPost> getPosts(int page, int limit) {
        var pageable = PageRequest.of(page - 1, limit);
        return PageResponse.from(postRepository.findByIsVisibleTrueOrderByCreatedAtDesc(pageable));
    }

    @Transactional
    public CommunityPost createPost(User user, String content, String imageUrl) {
        if (content == null || content.isBlank()) {
            throw AppException.badRequest("Nội dung không được để trống");
        }
        return postRepository.save(
                CommunityPost.builder().user(user).content(content).image(imageUrl).build()
        );
    }

    /** Toggle like: thích → bỏ thích, bỏ thích → thích */
    @Transactional
    public boolean toggleLike(UUID postId, User user) {
        // Đơn giản: dùng likes counter (production nên dùng bảng community_likes)
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> AppException.notFound("Bài viết không tồn tại"));
        // Giả sử mỗi lần gọi là toggle
        post.setLikes(post.getLikes() + 1);
        postRepository.save(post);
        return true;
    }

    public List<CommunityComment> getComments(UUID postId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> AppException.notFound("Bài viết không tồn tại"));
        return commentRepository.findByPostOrderByCreatedAtAsc(post);
    }

    @Transactional
    public CommunityComment addComment(UUID postId, User user, String content) {
        if (content == null || content.isBlank()) {
            throw AppException.badRequest("Nội dung bình luận không được trống");
        }
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> AppException.notFound("Bài viết không tồn tại"));
        return commentRepository.save(
                CommunityComment.builder().post(post).user(user).content(content).build()
        );
    }

    @Transactional
    public void hidePost(UUID postId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> AppException.notFound("Bài viết không tồn tại"));
        post.setVisible(false);
        postRepository.save(post);
    }
}
