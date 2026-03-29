package com.marketplace.service;

import com.marketplace.dto.product.ProductRequest;
import com.marketplace.dto.product.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    /**
     * Get all products with pagination (public, no authentication required)
     */
    Page<ProductResponse> getAllProducts(Pageable pageable);

    /**
     * Get a single product by ID (public, no authentication required)
     */
    ProductResponse getProductById(Long id);

    /**
     * Create a new product (SELLER/ADMIN only)
     */
    ProductResponse createProduct(ProductRequest request, Long sellerId);

    /**
     * Update an existing product (SELLER/ADMIN only, owner verification required)
     */
    ProductResponse updateProduct(Long id, ProductRequest request, Long currentUserId);

    /**
     * Delete a product (SELLER/ADMIN only, owner verification required)
     */
    void deleteProduct(Long id, Long currentUserId);
}
