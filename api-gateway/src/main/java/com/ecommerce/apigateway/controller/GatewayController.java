package com.ecommerce.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class GatewayController {
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "E-Commerce API Gateway");
        response.put("version", "1.0.0");
        response.put("status", "running");
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("auth", "/api/auth");
        endpoints.put("products", "/api/products");
        endpoints.put("categories", "/api/categories");
        endpoints.put("cart", "/api/cart");
        endpoints.put("orders", "/api/orders");
        endpoints.put("payments", "/api/payments");
        endpoints.put("notifications", "/api/notifications");
        response.put("endpoints", endpoints);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> r = new HashMap<>();
        r.put("status", "UP");
        r.put("service", "API Gateway");
        return ResponseEntity.ok(r);
    }
}
