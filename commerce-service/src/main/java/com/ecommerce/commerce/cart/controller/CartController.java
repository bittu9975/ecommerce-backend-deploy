package com.ecommerce.commerce.cart.controller;

import com.ecommerce.commerce.cart.dto.AddToCartRequest;
import com.ecommerce.commerce.cart.dto.CartResponse;
import com.ecommerce.commerce.cart.dto.UpdateCartItemRequest;
import com.ecommerce.commerce.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping("/health")
    public ResponseEntity<String> health() { return ResponseEntity.ok("Commerce Service is running!"); }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart(userId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody AddToCartRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addToCart(userId(), req.getProductId(), req.getQuantity()));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long productId,
                                                   @Valid @RequestBody UpdateCartItemRequest req) {
        return ResponseEntity.ok(cartService.updateCartItem(userId(), productId, req.getQuantity()));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeFromCart(userId(), productId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart(userId());
        return ResponseEntity.noContent().build();
    }

    private String userId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
