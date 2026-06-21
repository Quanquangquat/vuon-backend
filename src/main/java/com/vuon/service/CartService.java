package com.vuon.service;

import com.vuon.exception.AppException;
import com.vuon.model.CartItem;
import com.vuon.model.Product;
import com.vuon.model.User;
import com.vuon.repository.CartItemRepository;
import com.vuon.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository  productRepository;

    public List<CartItem> getCart(User user) {
        return cartItemRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /** Thêm vào giỏ hoặc tăng số lượng nếu đã có */
    @Transactional
    public void addToCart(User user, UUID productId, int quantity) {
        Product product = productRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> AppException.notFound("Sản phẩm không tồn tại"));

        if (!product.isInStock()) {
            throw AppException.badRequest("Sản phẩm đã hết hàng");
        }

        cartItemRepository.findByUserAndProduct(user, product).ifPresentOrElse(
                item -> {
                    item.setQuantity(item.getQuantity() + quantity);
                    cartItemRepository.save(item);
                },
                () -> cartItemRepository.save(
                        CartItem.builder().user(user).product(product).quantity(quantity).build()
                )
        );
    }

    /** Cập nhật số lượng - nếu quantity <= 0 thì xoá */
    @Transactional
    public void updateQuantity(User user, UUID productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> AppException.notFound("Sản phẩm không tồn tại"));

        if (quantity <= 0) {
            cartItemRepository.deleteByUserAndProduct(user, product);
            return;
        }
        CartItem item = cartItemRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> AppException.notFound("Sản phẩm không có trong giỏ hàng"));
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeFromCart(User user, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> AppException.notFound("Sản phẩm không tồn tại"));
        cartItemRepository.deleteByUserAndProduct(user, product);
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }
}
