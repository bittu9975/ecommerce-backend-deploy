package com.ecommerce.commerce.notification.repository;

import com.ecommerce.commerce.notification.entity.Notification;
import com.ecommerce.commerce.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(String userId);
    List<Notification> findByType(NotificationType type);
    List<Notification> findByUserIdOrderBySentAtDesc(String userId);
}
