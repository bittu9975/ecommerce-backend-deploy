package com.ecommerce.commerce.cart.service;

import com.ecommerce.commerce.cart.dto.ProductDTO;
import com.ecommerce.commerce.cart.exception.CartException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Slf4j
public class ProductClient {

    @Value("${product-service.url}")
    private String productServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public ProductDTO getProductById(Long productId) {
        log.info("Fetching product {} from Product Service", productId);
        try {
            String url = productServiceUrl + "/" + productId;
            HttpHeaders headers = new HttpHeaders();
            String token = extractTokenFromCurrentRequest();
            if (token != null) headers.set("Authorization", "Bearer " + token);
            ResponseEntity<ProductDTO> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), ProductDTO.class);
            ProductDTO product = response.getBody();
            if (product == null) throw new CartException("Product not found with ID: " + productId);
            log.info("Fetched product: {}", product.getName());
            return product;
        } catch (CartException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching product: {}", e.getMessage());
            throw new CartException("Unable to fetch product details. Product Service may be unavailable.");
        }
    }

    private String extractTokenFromCurrentRequest() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            String bearer = attrs.getRequest().getHeader("Authorization");
            if (bearer != null && bearer.startsWith("Bearer ")) return bearer.substring(7);
        } catch (Exception e) {
            log.warn("Could not extract token: {}", e.getMessage());
        }
        return null;
    }
}
