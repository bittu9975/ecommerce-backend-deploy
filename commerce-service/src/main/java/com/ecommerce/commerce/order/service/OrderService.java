package com.ecommerce.commerce.order.service;

import com.ecommerce.commerce.cart.dto.CartResponse;
import com.ecommerce.commerce.cart.model.CartItem;
import com.ecommerce.commerce.cart.service.CartService;
import com.ecommerce.commerce.events.OrderCreatedEvent;
import com.ecommerce.commerce.events.PaymentRequestEvent;
import com.ecommerce.commerce.order.dto.CreateOrderRequest;
import com.ecommerce.commerce.order.dto.OrderResponse;
import com.ecommerce.commerce.order.entity.Order;
import com.ecommerce.commerce.order.entity.OrderItem;
import com.ecommerce.commerce.order.entity.OrderStatus;
import com.ecommerce.commerce.order.exception.OrderException;
import com.ecommerce.commerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        log.info("Creating order for user: {}", userId);

        CartResponse cart = cartService.getCart(userId);
        if (cart.getItems() == null || cart.getItems().isEmpty())
            throw new OrderException("Cannot create order from empty cart");

        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalPrice(cart.getTotalPrice())
                .shippingAddress(request.getShippingAddress())
                .build();

        for (CartItem cartItem : cart.getItems()) {
            order.addItem(OrderItem.builder()
                    .productId(cartItem.getProductId())
                    .productName(cartItem.getProductName())
                    .price(cartItem.getPrice())
                    .quantity(cartItem.getQuantity())
                    .imageUrl(cartItem.getImageUrl())
                    .build());
        }

        Order saved = orderRepository.save(order);
        log.info("Order created: {}", saved.getId());

        // Publish in-process Spring Events (replaces RabbitMQ)
        eventPublisher.publishEvent(OrderCreatedEvent.builder()
                .orderId(saved.getId()).userId(saved.getUserId())
                .totalPrice(saved.getTotalPrice()).shippingAddress(saved.getShippingAddress())
                .createdAt(saved.getCreatedAt()).build());

        eventPublisher.publishEvent(PaymentRequestEvent.builder()
                .orderId(saved.getId()).userId(saved.getUserId())
                .amount(saved.getTotalPrice()).build());

        // Clear cart after order created
        cartService.clearCart(userId);

        return toResponse(saved);
    }

    public Page<OrderResponse> getUserOrders(String userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    public OrderResponse getOrderById(String userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found: " + orderId));
        if (!order.getUserId().equals(userId))
            throw new OrderException("You don't have permission to view this order");
        return toResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found: " + orderId));
        order.setStatus(status);
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(Order o) {
        return OrderResponse.builder()
                .id(o.getId()).userId(o.getUserId()).status(o.getStatus())
                .totalPrice(o.getTotalPrice()).items(o.getItems())
                .shippingAddress(o.getShippingAddress())
                .createdAt(o.getCreatedAt()).updatedAt(o.getUpdatedAt())
                .build();
    }
}
