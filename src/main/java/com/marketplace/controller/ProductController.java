package com.marketplace.controller;

import com.marketplace.dto.ApiResponse;
import com.marketplace.dto.product.ProductRequest;
import com.marketplace.dto.product.ProductResponse;
import com.marketplace.security.CurrentUserResolver;
import com.marketplace.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final CurrentUserResolver currentUserResolver;

    public ProductController(ProductService productService, CurrentUserResolver currentUserResolver) {
        this.productService = productService;
        this.currentUserResolver = currentUserResolver;
    }

    /**
     * GET /api/products - List all products with pagination (public, no authentication required)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(Pageable pageable) {
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(
            ApiResponse.success("Products retrieved successfully", products)
        );
    }

    /**
     * GET /api/products/{id} - Get a single product by ID (public, no authentication required)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(
            ApiResponse.success("Product retrieved successfully", product)
        );
    }

    /**
     * POST /api/products - Create a new product (SELLER/ADMIN only)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {
        Long currentUserId = currentUserResolver.resolveUserId(authentication);
        ProductResponse product = productService.createProduct(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Product created successfully", product));
    }

    /**
     * PUT /api/products/{id} - Update an existing product (SELLER/ADMIN only, owner verification required)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {
        Long currentUserId = currentUserResolver.resolveUserId(authentication);
        ProductResponse product = productService.updateProduct(id, request, currentUserId);
        return ResponseEntity.ok(
            ApiResponse.success("Product updated successfully", product)
        );
    }

    /**
     * DELETE /api/products/{id} - Delete a product (SELLER/ADMIN only, owner verification required)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            Authentication authentication) {
        Long currentUserId = currentUserResolver.resolveUserId(authentication);
        productService.deleteProduct(id, currentUserId);
        return ResponseEntity.ok(
            ApiResponse.success("Product deleted successfully")
        );
    }
}

