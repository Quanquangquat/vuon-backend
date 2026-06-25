package com.vuon.dto.request;

import lombok.Data;

/**
 * Body cho admin tạo đơn thay khách: chọn userId + thông tin đơn (OrderRequest).
 */
@Data
public class AdminOrderRequest {
    private String userId;
    private OrderRequest order;
}
