package com.ecommerce.commerce.events;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderCreatedEvent {
    private Long orderId;
    private String userId;
    private BigDecimal totalPrice;
    private String shippingAddress;
    private LocalDateTime createdAt;
}
