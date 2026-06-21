package com.vuon.controller;

import com.vuon.model.User;
import com.vuon.repository.UserRepository;
import com.vuon.service.ProductService;
import com.vuon.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1")   int page,
            @RequestParam(defaultValue = "12")  int limit,
            @RequestParam(defaultValue = "created_at") String sort) {
        return ApiResponse.ok(productService.getProducts(category, search, page, limit, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable UUID id) {
        var product = productService.getProductById(id);
        var reviews = productService.getReviews(id);
        return ApiResponse.ok(Map.of("product", product, "reviews", reviews));
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<?> addReview(@PathVariable UUID id,
                                       @AuthenticationPrincipal UUID userId,
                                       @RequestBody Map<String, Object> body) {
        User user   = userRepository.findById(userId).orElseThrow();
        int  rating = (int) body.get("rating");
        String comment = (String) body.get("comment");
        var review = productService.addReview(id, user, rating, comment);
        return ApiResponse.created(review, "Cảm ơn bạn đã đánh giá!");
    }
}
