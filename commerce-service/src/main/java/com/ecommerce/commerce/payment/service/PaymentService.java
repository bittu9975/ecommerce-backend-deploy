package com.ecommerce.commerce.payment.service;

import com.ecommerce.commerce.events.OrderPaidEvent;
import com.ecommerce.commerce.events.PaymentRequestEvent;
import com.ecommerce.commerce.events.PaymentResultEvent;
import com.ecommerce.commerce.order.entity.OrderStatus;
import com.ecommerce.commerce.order.service.OrderService;
import com.ecommerce.commerce.payment.entity.Payment;
import com.ecommerce.commerce.payment.entity.PaymentStatus;
import com.ecommerce.commerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;
    private final Random random = new Random();

    @Value("${payment.mock.enabled}")
    private boolean mockEnabled;

    @Value("${payment.mock.success-rate}")
    private int successRate;

    @Async
    @EventListener
    @Transactional
    public void processPayment(PaymentRequestEvent event) {
        log.info("Processing payment for order: {}, amount: {}", event.getOrderId(), event.getAmount());

        if (paymentRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.warn("Payment already processed for order: {}", event.getOrderId());
            return;
        }

        Payment payment = Payment.builder()
                .orderId(event.getOrderId()).userId(event.getUserId())
                .amount(event.getAmount()).status(PaymentStatus.PROCESSING)
                .paymentMethod("MOCK_PAYMENT").build();
        payment = paymentRepository.save(payment);

        // Simulate processing delay
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        boolean success = !mockEnabled || random.nextInt(100) < successRate;

        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            paymentRepository.save(payment);
            log.info("Payment SUCCESS for order: {}, txn: {}", event.getOrderId(), payment.getTransactionId());

            // Update order status
            orderService.updateOrderStatus(event.getOrderId(), OrderStatus.PAID);

            // Notify via Spring Event
            eventPublisher.publishEvent(OrderPaidEvent.builder()
                    .orderId(payment.getOrderId()).paymentId(payment.getId())
                    .userId(payment.getUserId()).amount(payment.getAmount())
                    .transactionId(payment.getTransactionId()).paidAt(LocalDateTime.now()).build());

            eventPublisher.publishEvent(PaymentResultEvent.builder()
                    .orderId(payment.getOrderId()).paymentId(payment.getId())
                    .userId(payment.getUserId()).amount(payment.getAmount())
                    .status("SUCCESS").transactionId(payment.getTransactionId()).build());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Insufficient funds");
            paymentRepository.save(payment);
            log.warn("Payment FAILED for order: {}", event.getOrderId());

            eventPublisher.publishEvent(PaymentResultEvent.builder()
                    .orderId(payment.getOrderId()).paymentId(payment.getId())
                    .userId(payment.getUserId()).amount(payment.getAmount())
                    .status("FAILED").failureReason("Insufficient funds").build());
        }
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).orElse(null);
    }

    public List<Payment> getPaymentsByUserId(String userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
