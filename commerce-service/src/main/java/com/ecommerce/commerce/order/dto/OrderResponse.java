package com.ecommerce.commerce.order.dto;

import com.ecommerce.commerce.order.entity.OrderItem;
import com.ecommerce.commerce.order.entity.OrderStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderResponse {
    private Long id;
    private String userId;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private List<OrderItem> items;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
