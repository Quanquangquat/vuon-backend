package com.vuon.controller;

import com.vuon.model.Promotion;
import com.vuon.service.AdminService;
import com.vuon.service.CommunityService;
import com.vuon.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService     adminService;
    private final CommunityService communityService;
    private final com.vuon.service.SiteSettingService siteSettingService;
    private final com.vuon.service.OrderService orderService;
    private final com.vuon.repository.UserRepository userRepository;

    // ---- Dashboard ----
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        return ApiResponse.ok(adminService.getDashboard());
    }

    // ---- Products ----
    @GetMapping("/products")
    public ResponseEntity<?> getProducts(@RequestParam(defaultValue = "1")  int page,
                                         @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(adminService.getProducts(page, limit));
    }

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@RequestBody Map<String, Object> body) {
        return ApiResponse.created(adminService.createProduct(body), "Đã thêm sản phẩm");
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable UUID id,
                                           @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminService.updateProduct(id, body), "Đã cập nhật sản phẩm");
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID id) {
        adminService.deleteProduct(id);
        return ApiResponse.ok(null, "Đã xoá sản phẩm");
    }

    // ---- Orders ----
    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(@RequestParam(required = false) String status,
                                       @RequestParam(defaultValue = "1")  int page,
                                       @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(adminService.getOrders(status, page, limit));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable UUID id,
                                               @RequestBody Map<String, String> body) {
        var order = adminService.updateOrderStatus(id, body.get("status"));
        return ApiResponse.ok(order, "Đã cập nhật trạng thái đơn hàng");
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody com.vuon.dto.request.AdminOrderRequest body) {
        if (body.getUserId() == null || body.getOrder() == null)
            return ApiResponse.badRequest("Thiếu thông tin khách hàng hoặc đơn hàng");
        com.vuon.model.User user;
        try {
            user = userRepository.findById(UUID.fromString(body.getUserId()))
                    .orElseThrow(() -> com.vuon.exception.AppException.notFound("Khách hàng không tồn tại"));
        } catch (IllegalArgumentException e) {
            return ApiResponse.badRequest("ID khách hàng không hợp lệ");
        }
        var order = orderService.createOrder(user, body.getOrder());
        return ApiResponse.created(order, "Đã tạo đơn hàng");
    }

    // ---- Customers ----
    @GetMapping("/customers")
    public ResponseEntity<?> getCustomers(@RequestParam(defaultValue = "1")  int page,
                                          @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(adminService.getCustomers(page, limit));
    }

    // ---- Promotions ----
    @PostMapping("/promotions")
    public ResponseEntity<?> createPromotion(@RequestBody Map<String, Object> body) {
        Promotion promo = Promotion.builder()
                .title((String) body.get("title"))
                .code(((String) body.get("code")).toUpperCase())
                .discount(body.containsKey("discount") ? (int) body.get("discount") : 0)
                .type(Promotion.Type.valueOf((String) body.getOrDefault("type", "percent")))
                .description((String) body.get("description"))
                .minOrder(body.containsKey("minOrder") ? (int) body.get("minOrder") : 0)
                .validUntil(LocalDateTime.parse((String) body.get("validUntil")))
                .build();
        return ApiResponse.created(adminService.createPromotion(promo), "Đã tạo mã khuyến mãi");
    }

    @PutMapping("/promotions/{id}")
    public ResponseEntity<?> updatePromotion(@PathVariable UUID id,
                                             @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminService.updatePromotion(id, body), "Đã cập nhật khuyến mãi");
    }

    // ---- FAQ ----
    @PostMapping("/faq")
    public ResponseEntity<?> createFaq(@RequestBody Map<String, Object> body) {
        var faq = adminService.createFaq(
                (String) body.get("question"),
                (String) body.get("answer"),
                (String) body.getOrDefault("category", "general"),
                body.containsKey("sortOrder") ? (int) body.get("sortOrder") : 0
        );
        return ApiResponse.created(faq, "Đã thêm FAQ");
    }

    @PutMapping("/faq/{id}")
    public ResponseEntity<?> updateFaq(@PathVariable UUID id,
                                       @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminService.updateFaq(id, body), "Đã cập nhật FAQ");
    }

    // ---- Blog ----
    @PostMapping("/blog")
    public ResponseEntity<?> createBlog(@RequestBody Map<String, Object> body) {
        var post = adminService.createBlogPost(
                (String) body.get("title"),
                (String) body.get("excerpt"),
                (String) body.get("content"),
                (String) body.get("image"),
                (String) body.get("category"),
                (String) body.getOrDefault("author", "Admin VƯƠN")
        );
        return ApiResponse.created(post, "Đã đăng bài viết");
    }

    @PutMapping("/blog/{id}")
    public ResponseEntity<?> updateBlog(@PathVariable UUID id,
                                        @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminService.updateBlogPost(id, body), "Đã cập nhật bài viết");
    }

    @DeleteMapping("/blog/{id}")
    public ResponseEntity<?> deleteBlog(@PathVariable UUID id) {
        adminService.deleteBlogPost(id);
        return ApiResponse.ok(null, "Đã xoá bài viết");
    }

    // ---- Banners ----
    @GetMapping("/banners")
    public ResponseEntity<?> getBanners() {
        return ApiResponse.ok(Map.of("banners", adminService.getAllBanners()));
    }

    @PostMapping("/banners")
    public ResponseEntity<?> createBanner(@RequestBody Map<String, Object> body) {
        return ApiResponse.created(adminService.createBanner(body), "Đã thêm banner");
    }

    @PutMapping("/banners/{id}")
    public ResponseEntity<?> updateBanner(@PathVariable UUID id,
                                          @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminService.updateBanner(id, body), "Đã cập nhật banner");
    }

    @DeleteMapping("/banners/{id}")
    public ResponseEntity<?> deleteBanner(@PathVariable UUID id) {
        adminService.deleteBanner(id);
        return ApiResponse.ok(null, "Đã xoá banner");
    }

    // ---- Site Settings ----
    @GetMapping("/settings")
    public ResponseEntity<?> getSettings() {
        return ApiResponse.ok(Map.of("settings", siteSettingService.get()));
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(Map.of("settings", siteSettingService.update(body)), "Đã lưu cài đặt");
    }

    // ---- Community Moderation ----
    @DeleteMapping("/community/{id}")
    public ResponseEntity<?> hidePost(@PathVariable UUID id) {
        communityService.hidePost(id);
        return ApiResponse.ok(null, "Đã ẩn bài viết");
    }
}
