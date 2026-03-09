package com.ecommerce.commerce.cart.dto;

import com.ecommerce.commerce.cart.model.CartItem;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CartResponse {
    private String userId;
    private List<CartItem> items;
    private Integer totalItems;
    private Integer itemCount;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
