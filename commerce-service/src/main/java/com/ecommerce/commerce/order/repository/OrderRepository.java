package com.ecommerce.commerce.order.repository;

import com.ecommerce.commerce.order.entity.Order;
import com.ecommerce.commerce.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(String userId, Pageable pageable);
    Page<Order> findByUserIdAndStatus(String userId, OrderStatus status, Pageable pageable);
    List<Order> findByStatus(OrderStatus status);
    long countByUserId(String userId);
}
