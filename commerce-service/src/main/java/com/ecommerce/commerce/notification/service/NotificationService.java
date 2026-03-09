package com.ecommerce.commerce.notification.service;

import com.ecommerce.commerce.events.OrderCreatedEvent;
import com.ecommerce.commerce.events.PaymentResultEvent;
import com.ecommerce.commerce.notification.entity.Notification;
import com.ecommerce.commerce.notification.entity.NotificationType;
import com.ecommerce.commerce.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Value("${notification.email.from}")
    private String fromEmail;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Notification: OrderCreated for order {}", event.getOrderId());
        String price = NumberFormat.getCurrencyInstance(Locale.US).format(event.getTotalPrice());
        String subject = "Order Confirmation - Order #" + event.getOrderId();
        String message = String.format(
            "Dear Customer,\n\nYour order #%d has been placed!\nTotal: %s\nShipping to: %s\n\nThank you!",
            event.getOrderId(), price, event.getShippingAddress());
        saveNotification(event.getUserId(), NotificationType.EMAIL, subject, message, "ORDER_CREATED", event.getOrderId());
        saveNotification(event.getUserId(), NotificationType.SMS,
            "SMS Notification",
            String.format("Order #%d confirmed! Total: %s", event.getOrderId(), price),
            "ORDER_CREATED", event.getOrderId());
    }

    @EventListener
    public void onPaymentResult(PaymentResultEvent event) {
        log.info("Notification: PaymentResult {} for order {}", event.getStatus(), event.getOrderId());
        String price = NumberFormat.getCurrencyInstance(Locale.US).format(event.getAmount());
        boolean success = "SUCCESS".equals(event.getStatus());
        String subject = success
            ? "Payment Successful - Order #" + event.getOrderId()
            : "Payment Failed - Order #" + event.getOrderId();
        String message = success
            ? String.format("Payment of %s processed successfully. Transaction: %s", price, event.getTransactionId())
            : String.format("Payment of %s failed. Reason: %s", price, event.getFailureReason());
        String eventType = success ? "PAYMENT_SUCCESS" : "PAYMENT_FAILED";
        saveNotification(event.getUserId(), NotificationType.EMAIL, subject, message, eventType, event.getOrderId());
    }

    private void saveNotification(String userId, NotificationType type, String subject,
                                   String message, String eventType, Long relatedId) {
        log.info("=== {} NOTIFICATION ===", type);
        log.info("To: {} | Subject: {}", userId, subject);
        log.info("Message: {}", message);
        notificationRepository.save(Notification.builder()
            .userId(userId).type(type).recipient(userId)
            .subject(subject).message(message)
            .eventType(eventType).relatedId(relatedId).build());
        log.info("Notification saved to DB.");
    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
