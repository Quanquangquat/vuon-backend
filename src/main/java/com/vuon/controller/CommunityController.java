package com.vuon.controller;

import com.vuon.model.User;
import com.vuon.repository.UserRepository;
import com.vuon.service.CommunityService;
import com.vuon.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final UserRepository   userRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<?> getPosts(@RequestParam(defaultValue = "1")  int page,
                                      @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(communityService.getPosts(page, limit));
    }

    @PostMapping
    public ResponseEntity<?> createPost(@AuthenticationPrincipal UUID userId,
                                        @RequestParam("content") String content,
                                        @RequestParam(value = "image", required = false) MultipartFile image)
            throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            var dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            String filename = UUID.randomUUID() + getExt(image.getOriginalFilename());
            Files.copy(image.getInputStream(), dir.resolve(filename));
            imageUrl = "/uploads/" + filename;
        }
        var post = communityService.createPost(user, content, imageUrl);
        return ApiResponse.created(post, "Đã đăng bài");
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> like(@AuthenticationPrincipal UUID userId,
                                   @PathVariable UUID id) {
        User user = userRepository.findById(userId).orElseThrow();
        boolean liked = communityService.toggleLike(id, user);
        return ApiResponse.ok(Map.of("liked", liked));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getComments(@PathVariable UUID id) {
        return ApiResponse.ok(Map.of("comments", communityService.getComments(id)));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@AuthenticationPrincipal UUID userId,
                                         @PathVariable UUID id,
                                         @RequestBody Map<String, String> body) {
        User user = userRepository.findById(userId).orElseThrow();
        var comment = communityService.addComment(id, user, body.get("content"));
        return ApiResponse.created(comment, "Đã bình luận");
    }

    private String getExt(String name) {
        if (name == null) return ".jpg";
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot) : ".jpg";
    }
}
