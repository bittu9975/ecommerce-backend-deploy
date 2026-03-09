package com.ecommerce.commerce.payment.controller;

import com.ecommerce.commerce.payment.dto.PaymentResponse;
import com.ecommerce.commerce.payment.entity.Payment;
import com.ecommerce.commerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/health")
    public ResponseEntity<String> health() { return ResponseEntity.ok("Commerce Service is running!"); }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getByOrderId(@PathVariable Long orderId) {
        Payment p = paymentService.getPaymentByOrderId(orderId);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toResponse(p));
    }

    @GetMapping("/user/{userId:.+}")
    public ResponseEntity<List<PaymentResponse>> getByUserId(@PathVariable String userId) {
        List<PaymentResponse> responses = paymentService.getPaymentsByUserId(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId()).orderId(p.getOrderId()).userId(p.getUserId())
                .amount(p.getAmount()).status(p.getStatus())
                .paymentMethod(p.getPaymentMethod()).transactionId(p.getTransactionId())
                .failureReason(p.getFailureReason())
                .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt()).build();
    }
}
