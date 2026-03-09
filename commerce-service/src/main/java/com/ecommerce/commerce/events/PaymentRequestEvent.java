package com.ecommerce.commerce.events;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentRequestEvent {
    private Long orderId;
    private String userId;
    private BigDecimal amount;
}
