package com.vuon.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

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
}
