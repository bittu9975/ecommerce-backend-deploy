package com.ecommerce.commerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateOrderRequest {
    @NotBlank(message = "Shipping address is required")
    @Size(max = 500)
    private String shippingAddress;
}
