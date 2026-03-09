package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find by category
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    
    // Find active products
    Page<Product> findByActiveTrue(Pageable pageable);
    
    // Search by name (case-insensitive)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.active = true")
    Page<Product> searchByName(@Param("name") String name, Pageable pageable);
    
    // Search by name or description
    @Query("SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND p.active = true")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // Find by price range
    Page<Product> findByPriceBetweenAndActiveTrue(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    // Find by category and price range
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId " +
           "AND p.price BETWEEN :minPrice AND :maxPrice AND p.active = true")
    Page<Product> findByCategoryAndPriceRange(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
    
    // Find by brand
    Page<Product> findByBrandAndActiveTrue(String brand, Pageable pageable);
    
    // Find by SKU
    Optional<Product> findBySku(String sku);
    
    // Check if product exists by SKU
    boolean existsBySku(String sku);
    
    // Get all brands
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL AND p.active = true ORDER BY p.brand")
    List<String> findAllBrands();
    
    // Count products by category
    long countByCategoryId(Long categoryId);
}
