package com.vuon.service;

import com.vuon.dto.response.PageResponse;
import com.vuon.exception.AppException;
import com.vuon.model.Product;
import com.vuon.model.Review;
import com.vuon.model.User;
import com.vuon.repository.OrderRepository;
import com.vuon.repository.ProductRepository;
import com.vuon.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service xử lý nghiệp vụ sản phẩm:
 * - Tìm kiếm, lọc, phân trang
 * - Đánh giá sản phẩm
 * - Cập nhật rating sau khi có review mới
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository  reviewRepository;
    private final OrderRepository   orderRepository;

    /** Lấy danh sách sản phẩm có filter + phân trang */
    public PageResponse<Product> getProducts(String category, String search,
                                             int page, int limit, String sort) {
        Sort sortSpec = switch (sort) {
            case "price_asc"  -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "rating"     -> Sort.by("rating").descending();
            case "popular"    -> Sort.by("reviewsCount").descending();
            default           -> Sort.by("createdAt").descending();
        };

        PageRequest pageable = PageRequest.of(page - 1, limit, sortSpec);
        Product.Category cat = parseCategory(category);

        Page<Product> result = (search != null && !search.isBlank())
                ? productRepository.searchProducts(cat, search, pageable)
                : (cat != null ? productRepository.findByIsActiveTrueAndCategory(cat, pageable)
                               : productRepository.findByIsActiveTrue(pageable));

        return PageResponse.from(result);
    }

    /** Lấy chi tiết sản phẩm kèm danh sách review */
    public Product getProductById(UUID id) {
        return productRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> AppException.notFound("Sản phẩm không tồn tại"));
    }

    public List<Review> getReviews(UUID productId) {
        Product product = getProductById(productId);
        return reviewRepository.findByProductOrderByCreatedAtDesc(product);
    }

    /** Thêm đánh giá - chỉ user đã mua mới được đánh giá */
    @Transactional
    public Review addReview(UUID productId, User user, int rating, String comment) {
        Product product = getProductById(productId);

        if (!orderRepository.hasUserPurchasedProduct(user, productId)) {
            throw AppException.badRequest("Bạn cần mua và nhận hàng thành công để đánh giá");
        }

        // Upsert: nếu đã đánh giá rồi thì cập nhật
        Review review = reviewRepository.findByProductAndUser(product, user)
                .orElse(Review.builder().product(product).user(user).build());
        review.setRating(rating);
        review.setComment(comment);
        review = reviewRepository.save(review);

        // Cập nhật rating trung bình cho sản phẩm
        updateProductRating(product);
        return review;
    }

    /** Tính lại rating trung bình sau mỗi review */
    private void updateProductRating(Product product) {
        Double avg   = reviewRepository.getAverageRating(product);
        long   count = reviewRepository.countByProduct(product);
        product.setRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0);
        product.setReviewsCount((int) count);
        productRepository.save(product);
    }

    private Product.Category parseCategory(String category) {
        if (category == null || category.isBlank() || "all".equals(category)) return null;
        try { return Product.Category.valueOf(category); }
        catch (IllegalArgumentException e) { return null; }
    }
}
