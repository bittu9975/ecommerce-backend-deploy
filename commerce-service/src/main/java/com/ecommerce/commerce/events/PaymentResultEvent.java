package com.ecommerce.commerce.events;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentResultEvent {
    private Long orderId;
    private Long paymentId;
    private String userId;
    private BigDecimal amount;
    private String status;   // "SUCCESS" or "FAILED"
    private String transactionId;
    private String failureReason;
}
