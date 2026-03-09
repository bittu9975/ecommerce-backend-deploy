package com.ecommerce.commerce.cart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Integer getTotalItems() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
    public BigDecimal getTotalPrice() {
        return items.stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public int getItemCount() { return items.size(); }
}
