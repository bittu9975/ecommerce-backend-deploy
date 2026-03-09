package com.ecommerce.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        log.warn("Auth Service is unavailable");
        return createFallbackResponse("Auth Service is currently unavailable");
    }

    @GetMapping("/product")
    public ResponseEntity<Map<String, Object>> productFallback() {
        log.warn("Product Service is unavailable");
        return createFallbackResponse("Product Service is currently unavailable");
    }

    @GetMapping("/cart")
    public ResponseEntity<Map<String, Object>> cartFallback() {
        log.warn("Cart Service is unavailable");
        return createFallbackResponse("Cart Service is currently unavailable");
    }

    @GetMapping("/order")
    public ResponseEntity<Map<String, Object>> orderFallback() {
        log.warn("Order Service is unavailable");
        return createFallbackResponse("Order Service is currently unavailable");
    }

    @GetMapping("/payment")
    public ResponseEntity<Map<String, Object>> paymentFallback() {
        log.warn("Payment Service is unavailable");
        return createFallbackResponse("Payment Service is currently unavailable");
    }

    @GetMapping("/notification")
    public ResponseEntity<Map<String, Object>> notificationFallback() {
        log.warn("Notification Service is unavailable");
        return createFallbackResponse("Notification Service is currently unavailable");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", message);
        
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}
