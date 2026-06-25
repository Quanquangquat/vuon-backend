package com.vuon.controller;

import com.vuon.model.User;
import com.vuon.model.WateringReminder;
import com.vuon.repository.*;
import com.vuon.service.PlantJournalService;
import com.vuon.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Controller gộp các tính năng nhỏ:
 * Blog, Promotion, Favorites, PlantJournal, WateringReminder, FAQ, Health
 */
@RestController
@RequiredArgsConstructor
public class MiscController {

    private final BlogPostRepository      blogPostRepository;
    private final BannerRepository        bannerRepository;
    private final com.vuon.service.SiteSettingService siteSettingService;
    private final PromotionRepository     promotionRepository;
    private final FavoriteRepository      favoriteRepository;
    private final ProductRepository       productRepository;
    private final WateringReminderRepository reminderRepository;
    private final FaqRepository           faqRepository;
    private final UserRepository          userRepository;
    private final PlantJournalService     journalService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ========== HEALTH CHECK ==========
    @GetMapping("/api/health")
    public ResponseEntity<?> health() {
        return ApiResponse.ok(Map.of("status", "ok", "app", "VƯƠN Backend",
                                     "time", LocalDateTime.now().toString()));
    }

    // ========== BLOG ==========
    @GetMapping("/api/blog")
    public ResponseEntity<?> getBlog(@RequestParam(required = false) String category,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "6") int limit) {
        var pageable = org.springframework.data.domain.PageRequest.of(
                page - 1, limit, org.springframework.data.domain.Sort.by("createdAt").descending());
        var result = (category != null)
                ? blogPostRepository.findByIsPublishedTrueAndCategory(category, pageable)
                : blogPostRepository.findByIsPublishedTrue(pageable);
        return ApiResponse.ok(com.vuon.dto.response.PageResponse.from(result));
    }

    @GetMapping("/api/blog/{slug}")
    public ResponseEntity<?> getBlogPost(@PathVariable String slug) {
        var post = blogPostRepository.findBySlugAndIsPublishedTrue(slug)
                .orElseThrow(() -> com.vuon.exception.AppException.notFound("Bài viết không tồn tại"));
        post.setViews(post.getViews() + 1);
        blogPostRepository.save(post);
        return ApiResponse.ok(Map.of("post", post));
    }

    // ========== BANNERS ==========
    @GetMapping("/api/banners")
    public ResponseEntity<?> getBanners() {
        return ApiResponse.ok(Map.of("banners",
                bannerRepository.findByIsActiveTrueOrderBySortOrderAsc()));
    }

    // ========== SITE SETTINGS ==========
    @GetMapping("/api/settings")
    public ResponseEntity<?> getSettings() {
        return ApiResponse.ok(Map.of("settings", siteSettingService.get()));
    }

    // ========== PROMOTIONS ==========
    @GetMapping("/api/promotions")
    public ResponseEntity<?> getPromotions() {
        return ApiResponse.ok(Map.of("promotions", promotionRepository.findAllActive()));
    }

    @PostMapping("/api/promotions/validate")
    public ResponseEntity<?> validatePromo(@RequestBody Map<String, Object> body) {
        String code  = ((String) body.get("code")).toUpperCase();
        int    total = (int) body.get("orderTotal");
        var promo = promotionRepository.findActiveByCode(code)
                .orElseThrow(() -> com.vuon.exception.AppException.badRequest(
                        "Mã giảm giá không hợp lệ hoặc đã hết hạn"));
        if (total < promo.getMinOrder()) {
            throw com.vuon.exception.AppException.badRequest(
                    "Đơn hàng tối thiểu " + promo.getMinOrder() + "đ");
        }
        int discount = switch (promo.getType()) {
            case percent  -> (int) (total * promo.getDiscount() / 100.0);
            case fixed    -> promo.getDiscount();
            case freeship -> 30000;
        };
        return ApiResponse.ok(Map.of("promo", promo, "discount", discount));
    }

    // ========== FAVORITES ==========
    @GetMapping("/api/favorites")
    public ResponseEntity<?> getFavorites(@AuthenticationPrincipal UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        var favs = favoriteRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(f -> f.getProduct()).toList();
        return ApiResponse.ok(Map.of("favorites", favs));
    }

    @PostMapping("/api/favorites/{productId}")
    public ResponseEntity<?> addFavorite(@AuthenticationPrincipal UUID userId,
                                         @PathVariable UUID productId) {
        User user = userRepository.findById(userId).orElseThrow();
        var product = productRepository.findById(productId)
                .orElseThrow(() -> com.vuon.exception.AppException.notFound("Sản phẩm không tồn tại"));
        if (!favoriteRepository.existsByUserAndProduct(user, product)) {
            favoriteRepository.save(com.vuon.model.Favorite.builder()
                    .user(user).product(product).build());
        }
        return ApiResponse.ok(null, "Đã thêm vào yêu thích");
    }

    @DeleteMapping("/api/favorites/{productId}")
    public ResponseEntity<?> removeFavorite(@AuthenticationPrincipal UUID userId,
                                            @PathVariable UUID productId) {
        User user = userRepository.findById(userId).orElseThrow();
        var product = productRepository.findById(productId)
                .orElseThrow(() -> com.vuon.exception.AppException.notFound("Sản phẩm không tồn tại"));
        favoriteRepository.deleteByUserAndProduct(user, product);
        return ApiResponse.ok(null, "Đã xoá khỏi yêu thích");
    }

    // ========== PLANT JOURNAL ==========
    @GetMapping("/api/journal")
    public ResponseEntity<?> getJournal(@AuthenticationPrincipal UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return ApiResponse.ok(Map.of("entries", journalService.getJournal(user)));
    }

    @PostMapping("/api/journal")
    public ResponseEntity<?> addJournal(@AuthenticationPrincipal UUID userId,
                                        @RequestParam("plantName") String plantName,
                                        @RequestParam(value = "notes",        required = false) String notes,
                                        @RequestParam(value = "healthStatus", required = false) String health,
                                        @RequestParam(value = "image",        required = false) MultipartFile image)
            throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        String imageUrl = saveImage(image);
        var entry = journalService.addEntry(user, plantName, notes, imageUrl, health, null);
        return ApiResponse.created(entry, "Đã thêm vào nhật ký");
    }

    @PutMapping("/api/journal/{id}")
    public ResponseEntity<?> updateJournal(@AuthenticationPrincipal UUID userId,
                                           @PathVariable UUID id,
                                           @RequestParam(value = "notes",        required = false) String notes,
                                           @RequestParam(value = "healthStatus", required = false) String health,
                                           @RequestParam(value = "image",        required = false) MultipartFile image)
            throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        String imageUrl = saveImage(image);
        var entry = journalService.updateEntry(id, user, notes, imageUrl, health);
        return ApiResponse.ok(entry, "Đã cập nhật nhật ký");
    }

    @DeleteMapping("/api/journal/{id}")
    public ResponseEntity<?> deleteJournal(@AuthenticationPrincipal UUID userId,
                                           @PathVariable UUID id) {
        User user = userRepository.findById(userId).orElseThrow();
        journalService.deleteEntry(id, user);
        return ApiResponse.ok(null, "Đã xoá khỏi nhật ký");
    }

    // ========== WATERING REMINDERS ==========
    @GetMapping("/api/reminders")
    public ResponseEntity<?> getReminders(@AuthenticationPrincipal UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return ApiResponse.ok(Map.of("reminders",
                reminderRepository.findByUserAndIsActiveTrueOrderByNextWaterAsc(user)));
    }

    @PostMapping("/api/reminders")
    public ResponseEntity<?> createReminder(@AuthenticationPrincipal UUID userId,
                                            @RequestBody Map<String, Object> body) {
        User user = userRepository.findById(userId).orElseThrow();
        String plantName     = (String) body.get("plantName");
        int    frequency     = body.containsKey("frequency") ? (int) body.get("frequency") : 1;
        String freqUnit      = (String) body.getOrDefault("frequencyUnit", "day");
        WateringReminder.FrequencyUnit unit = WateringReminder.FrequencyUnit.valueOf(freqUnit);

        LocalDateTime nextWater = LocalDateTime.now();
        nextWater = (unit == WateringReminder.FrequencyUnit.day)
                ? nextWater.plusDays(frequency)
                : nextWater.plusWeeks(frequency);

        var reminder = reminderRepository.save(WateringReminder.builder()
                .user(user).plantName(plantName)
                .frequency(frequency).frequencyUnit(unit)
                .lastWatered(LocalDateTime.now()).nextWater(nextWater)
                .build());
        return ApiResponse.created(reminder, "Đã tạo nhắc nhở");
    }

    @PutMapping("/api/reminders/{id}/water")
    public ResponseEntity<?> markWatered(@AuthenticationPrincipal UUID userId,
                                          @PathVariable UUID id) {
        User user = userRepository.findById(userId).orElseThrow();
        var reminder = reminderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> com.vuon.exception.AppException.notFound("Không tìm thấy nhắc nhở"));

        LocalDateTime next = LocalDateTime.now();
        next = (reminder.getFrequencyUnit() == WateringReminder.FrequencyUnit.day)
                ? next.plusDays(reminder.getFrequency())
                : next.plusWeeks(reminder.getFrequency());

        reminder.setLastWatered(LocalDateTime.now());
        reminder.setNextWater(next);
        return ApiResponse.ok(reminderRepository.save(reminder), "Đã đánh dấu tưới nước!");
    }

    @DeleteMapping("/api/reminders/{id}")
    public ResponseEntity<?> deleteReminder(@AuthenticationPrincipal UUID userId,
                                            @PathVariable UUID id) {
        User user = userRepository.findById(userId).orElseThrow();
        var reminder = reminderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> com.vuon.exception.AppException.notFound("Không tìm thấy nhắc nhở"));
        reminderRepository.delete(reminder);
        return ApiResponse.ok(null, "Đã xoá nhắc nhở");
    }

    // ========== FAQ ==========
    @GetMapping("/api/ai/faqs")
    public ResponseEntity<?> getFaqs() {
        return ApiResponse.ok(Map.of("faqs", faqRepository.findByIsActiveTrueOrderBySortOrderAsc()));
    }

    // Helper: lưu file ảnh upload
    private String saveImage(MultipartFile image) throws Exception {
        if (image == null || image.isEmpty()) return null;
        var dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        String filename = UUID.randomUUID() + getExt(image.getOriginalFilename());
        Files.copy(image.getInputStream(), dir.resolve(filename));
        return "/uploads/" + filename;
    }

    private String getExt(String name) {
        if (name == null) return ".jpg";
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot) : ".jpg";
    }
}
