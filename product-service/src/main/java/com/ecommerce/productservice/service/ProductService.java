package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.ProductDTO;
import com.ecommerce.productservice.dto.ProductSearchRequest;
import com.ecommerce.productservice.entity.Category;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.exception.ResourceNotFoundException;
import com.ecommerce.productservice.repository.CategoryRepository;
import com.ecommerce.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

//    @Cacheable(value = "products", key = "'all:' + #page + ':' + #size + ':' + #sortBy + ':' + #sortDirection")
    public Page<ProductDTO> getAllProducts(int page, int size, String sortBy, String sortDirection) {
        log.info("Fetching all products - page: {}, size: {}", page, size);
        
        Sort sort = sortDirection.equalsIgnoreCase("DESC") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findByActiveTrue(pageable);
        
        return productPage.map(this::convertToDTO);
    }

//    @Cacheable(value = "products", key = "#id")
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return convertToDTO(product);
    }

    public Page<ProductDTO> searchProducts(ProductSearchRequest searchRequest) {
        log.info("Searching products with request: {}", searchRequest);
        
        Sort sort = searchRequest.getSortDirection().equalsIgnoreCase("DESC") ? 
                    Sort.by(searchRequest.getSortBy()).descending() : 
                    Sort.by(searchRequest.getSortBy()).ascending();
        
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(), 
                searchRequest.getSize(), 
                sort
        );

        Page<Product> productPage;

        // Search based on criteria
        if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().isEmpty()) {
            productPage = productRepository.searchByKeyword(searchRequest.getKeyword(), pageable);
        } else if (searchRequest.getCategoryId() != null && 
                   searchRequest.getMinPrice() != null && 
                   searchRequest.getMaxPrice() != null) {
            productPage = productRepository.findByCategoryAndPriceRange(
                    searchRequest.getCategoryId(),
                    searchRequest.getMinPrice(),
                    searchRequest.getMaxPrice(),
                    pageable
            );
        } else if (searchRequest.getCategoryId() != null) {
            productPage = productRepository.findByCategoryId(searchRequest.getCategoryId(), pageable);
        } else if (searchRequest.getMinPrice() != null && searchRequest.getMaxPrice() != null) {
            productPage = productRepository.findByPriceBetweenAndActiveTrue(
                    searchRequest.getMinPrice(),
                    searchRequest.getMaxPrice(),
                    pageable
            );
        } else if (searchRequest.getBrand() != null && !searchRequest.getBrand().isEmpty()) {
            productPage = productRepository.findByBrandAndActiveTrue(searchRequest.getBrand(), pageable);
        } else {
            productPage = productRepository.findByActiveTrue(pageable);
        }

        return productPage.map(this::convertToDTO);
    }

    public List<String> getAllBrands() {
        log.info("Fetching all brands");
        return productRepository.findAllBrands();
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getName());
        
        // Check if SKU already exists
        if (productDTO.getSku() != null && productRepository.existsBySku(productDTO.getSku())) {
            throw new RuntimeException("Product already exists with SKU: " + productDTO.getSku());
        }

        // Find category
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", productDTO.getCategoryId()));

        Product product = Product.builder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .stock(productDTO.getStock())
                .imageUrl(productDTO.getImageUrl())
                .category(category)
                .active(productDTO.getActive() != null ? productDTO.getActive() : true)
                .brand(productDTO.getBrand())
                .sku(productDTO.getSku())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());
        
        return convertToDTO(savedProduct);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with id: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Check if SKU conflicts with another product
        if (productDTO.getSku() != null && 
            !productDTO.getSku().equals(product.getSku()) &&
            productRepository.existsBySku(productDTO.getSku())) {
            throw new RuntimeException("Product already exists with SKU: " + productDTO.getSku());
        }

        // Find category if changed
        if (!product.getCategory().getId().equals(productDTO.getCategoryId())) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", productDTO.getCategoryId()));
            product.setCategory(category);
        }

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        product.setImageUrl(productDTO.getImageUrl());
        product.setActive(productDTO.getActive() != null ? productDTO.getActive() : product.getActive());
        product.setBrand(productDTO.getBrand());
        product.setSku(productDTO.getSku());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with id: {}", updatedProduct.getId());
        
        return convertToDTO(updatedProduct);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        productRepository.delete(product);
        log.info("Product deleted successfully with id: {}", id);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO updateStock(Long id, Integer quantity) {
        log.info("Updating stock for product id: {} with quantity: {}", id, quantity);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setStock(quantity);
        Product updatedProduct = productRepository.save(product);
        
        log.info("Stock updated successfully for product id: {}", id);
        return convertToDTO(updatedProduct);
    }

    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .active(product.getActive())
                .brand(product.getBrand())
                .sku(product.getSku())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
