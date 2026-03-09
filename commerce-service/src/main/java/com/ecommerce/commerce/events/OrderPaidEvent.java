package com.ecommerce.commerce.events;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderPaidEvent {
    private Long orderId;
    private Long paymentId;
    private String userId;
    private BigDecimal amount;
    private String transactionId;
    private LocalDateTime paidAt;
}
