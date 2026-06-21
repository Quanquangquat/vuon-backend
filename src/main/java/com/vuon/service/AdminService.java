package com.vuon.service;

import com.vuon.dto.response.PageResponse;
import com.vuon.exception.AppException;
import com.vuon.model.*;
import com.vuon.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service dành riêng cho Admin
 * Quản lý sản phẩm, đơn hàng, khách hàng, khuyến mãi
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository      userRepository;
    private final ProductRepository   productRepository;
    private final OrderRepository     orderRepository;
    private final PromotionRepository promotionRepository;
    private final FaqRepository       faqRepository;
    private final BlogPostRepository  blogPostRepository;

    /** Dashboard: tổng quan thống kê */
    public Map<String, Object> getDashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers",    userRepository.count());
        stats.put("totalProducts", productRepository.count());
        stats.put("totalRevenue",  orderRepository.getTotalRevenue());
        stats.put("ordersByStatus", Map.of(
                "processing", orderRepository.countByStatus(Order.Status.processing),
                "shipping",   orderRepository.countByStatus(Order.Status.shipping),
                "completed",  orderRepository.countByStatus(Order.Status.completed),
                "cancelled",  orderRepository.countByStatus(Order.Status.cancelled)
        ));
        return stats;
    }

    // ---- Products ----
    public PageResponse<Product> getProducts(int page, int limit) {
        var pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        return PageResponse.from(productRepository.findAll(pageable));
    }

    @Transactional
    public Product createProduct(String name, String category, int price, String image,
                                  String description, String difficulty, String light,
                                  String careLevel, int stock) {
        return productRepository.save(Product.builder()
                .name(name)
                .category(Product.Category.valueOf(category))
                .price(price).image(image).description(description)
                .difficulty(Product.Difficulty.fromString(difficulty))
                .light(light).careLevel(careLevel)
                .stock(stock).inStock(stock > 0)
                .build());
    }

    @Transactional
    public Product updateProduct(UUID id, Map<String, Object> fields) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sản phẩm không tồn tại"));
        if (fields.containsKey("name"))        p.setName((String) fields.get("name"));
        if (fields.containsKey("price"))       p.setPrice((int) fields.get("price"));
        if (fields.containsKey("stock")) {
            int stock = (int) fields.get("stock");
            p.setStock(stock);
            p.setInStock(stock > 0);
        }
        if (fields.containsKey("description")) p.setDescription((String) fields.get("description"));
        if (fields.containsKey("image"))       p.setImage((String) fields.get("image"));
        return productRepository.save(p);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sản phẩm không tồn tại"));
        p.setActive(false);
        productRepository.save(p);
    }

    // ---- Orders ----
    public PageResponse<Order> getOrders(String status, int page, int limit) {
        var pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        var result = (status != null)
                ? orderRepository.findAll(pageable)  // Thêm filter status nếu cần
                : orderRepository.findAll(pageable);
        return PageResponse.from(result);
    }

    @Transactional
    public Order updateOrderStatus(UUID orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Đơn hàng không tồn tại"));
        try {
            order.setStatus(Order.Status.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest("Trạng thái không hợp lệ");
        }
        return orderRepository.save(order);
    }

    // ---- Customers ----
    public PageResponse<User> getCustomers(int page, int limit) {
        var pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        return PageResponse.from(userRepository.findAll(pageable));
    }

    // ---- Promotions ----
    @Transactional
    public Promotion createPromotion(Promotion promo) {
        return promotionRepository.save(promo);
    }

    @Transactional
    public Promotion updatePromotion(UUID id, Map<String, Object> fields) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Khuyến mãi không tồn tại"));
        if (fields.containsKey("isActive")) p.setActive((boolean) fields.get("isActive"));
        if (fields.containsKey("title"))    p.setTitle((String) fields.get("title"));
        return promotionRepository.save(p);
    }

    // ---- FAQ ----
    @Transactional
    public Faq createFaq(String question, String answer, String category, int sortOrder) {
        return faqRepository.save(Faq.builder()
                .question(question).answer(answer)
                .category(category).sortOrder(sortOrder)
                .build());
    }

    @Transactional
    public Faq updateFaq(UUID id, Map<String, Object> fields) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("FAQ không tồn tại"));
        if (fields.containsKey("question")) faq.setQuestion((String) fields.get("question"));
        if (fields.containsKey("answer"))   faq.setAnswer((String) fields.get("answer"));
        if (fields.containsKey("isActive")) faq.setActive((boolean) fields.get("isActive"));
        return faqRepository.save(faq);
    }

    // ---- Blog ----
    @Transactional
    public BlogPost createBlogPost(String title, String excerpt, String content,
                                    String image, String category, String author) {
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-") + "-" + System.currentTimeMillis();
        return blogPostRepository.save(BlogPost.builder()
                .title(title).slug(slug).excerpt(excerpt)
                .content(content).image(image)
                .category(category).author(author)
                .build());
    }
}
