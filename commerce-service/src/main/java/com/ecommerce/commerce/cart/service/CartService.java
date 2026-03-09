package com.ecommerce.commerce.cart.service;

import com.ecommerce.commerce.cart.dto.CartResponse;
import com.ecommerce.commerce.cart.dto.ProductDTO;
import com.ecommerce.commerce.cart.exception.CartException;
import com.ecommerce.commerce.cart.model.Cart;
import com.ecommerce.commerce.cart.model.CartItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductClient productClient;

    @Value("${cart.ttl}")
    private long cartTTL;

    @Value("${cart.max-items}")
    private int maxItems;

    private static final String CART_KEY_PREFIX = "cart:";

    public CartResponse getCart(String userId) {
        log.info("Fetching cart for user: {}", userId);
        Cart cart = (Cart) redisTemplate.opsForValue().get(cartKey(userId));
        if (cart == null) {
            cart = Cart.builder().userId(userId)
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        }
        return toResponse(cart);
    }

    public CartResponse addToCart(String userId, Long productId, Integer quantity) {
        log.info("Adding product {} qty {} to cart for user {}", productId, quantity, userId);
        ProductDTO product = productClient.getProductById(productId);
        if (!product.getActive()) throw new CartException("Product is not available");
        if (product.getStock() < quantity)
            throw new CartException("Insufficient stock. Available: " + product.getStock());

        Cart cart = (Cart) redisTemplate.opsForValue().get(cartKey(userId));
        if (cart == null)
            cart = Cart.builder().userId(userId)
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId)).findFirst();
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + quantity;
            if (product.getStock() < newQty)
                throw new CartException("Insufficient stock. Available: " + product.getStock());
            item.setQuantity(newQty);
        } else {
            if (cart.getItems().size() >= maxItems)
                throw new CartException("Cart is full. Maximum " + maxItems + " items allowed");
            cart.getItems().add(CartItem.builder()
                    .productId(product.getId()).productName(product.getName())
                    .price(product.getPrice()).quantity(quantity).imageUrl(product.getImageUrl())
                    .build());
        }
        cart.setUpdatedAt(LocalDateTime.now());
        save(userId, cart);
        return toResponse(cart);
    }

    public CartResponse updateCartItem(String userId, Long productId, Integer quantity) {
        Cart cart = (Cart) redisTemplate.opsForValue().get(cartKey(userId));
        if (cart == null) throw new CartException("Cart is empty");
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId)).findFirst()
                .orElseThrow(() -> new CartException("Product not found in cart"));
        ProductDTO product = productClient.getProductById(productId);
        if (product.getStock() < quantity)
            throw new CartException("Insufficient stock. Available: " + product.getStock());
        item.setQuantity(quantity);
        cart.setUpdatedAt(LocalDateTime.now());
        save(userId, cart);
        return toResponse(cart);
    }

    public CartResponse removeFromCart(String userId, Long productId) {
        Cart cart = (Cart) redisTemplate.opsForValue().get(cartKey(userId));
        if (cart == null) throw new CartException("Cart is empty");
        if (!cart.getItems().removeIf(i -> i.getProductId().equals(productId)))
            throw new CartException("Product not found in cart");
        cart.setUpdatedAt(LocalDateTime.now());
        save(userId, cart);
        return toResponse(cart);
    }

    public void clearCart(String userId) {
        redisTemplate.delete(cartKey(userId));
        log.info("Cart cleared for user: {}", userId);
    }

    private void save(String userId, Cart cart) {
        redisTemplate.opsForValue().set(cartKey(userId), cart, cartTTL, TimeUnit.SECONDS);
    }

    private String cartKey(String userId) { return CART_KEY_PREFIX + userId; }

    private CartResponse toResponse(Cart cart) {
        return CartResponse.builder()
                .userId(cart.getUserId()).items(cart.getItems())
                .totalItems(cart.getTotalItems()).itemCount(cart.getItemCount())
                .totalPrice(cart.getTotalPrice())
                .createdAt(cart.getCreatedAt()).updatedAt(cart.getUpdatedAt())
                .build();
    }
}
