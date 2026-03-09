package com.ecommerce.commerce.payment.repository;

import com.ecommerce.commerce.payment.entity.Payment;
import com.ecommerce.commerce.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    List<Payment> findByUserId(String userId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);
}
