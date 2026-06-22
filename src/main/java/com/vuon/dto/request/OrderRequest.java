package com.vuon.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    @NotBlank(message = "Tên người nhận không được trống")
    private String shippingName;

    @NotBlank(message = "Số điện thoại không được trống")
    private String shippingPhone;

    @NotBlank(message = "Địa chỉ không được trống")
    private String shippingAddress;

    private String shippingNote;

    private String paymentMethod = "COD";

    private String promoCode;

    /**
     * Danh sách sản phẩm gửi thẳng từ giỏ hàng frontend (kèm snapshot tên/ảnh/giá).
     * Nếu có thì tạo đơn từ đây, không phụ thuộc giỏ server hay ID có khớp DB hay không.
     */
    private List<Item> items;

    @Data
    public static class Item {
        private String productId;
        private String productName;
        private String productImage;
        private int    price;
        private int    quantity;
    }
}
