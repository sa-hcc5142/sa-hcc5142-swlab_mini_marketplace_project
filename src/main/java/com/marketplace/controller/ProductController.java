package com.marketplace.controller;

import com.marketplace.dto.ApiResponse;
import com.marketplace.dto.product.ProductRequest;
import com.marketplace.dto.product.ProductResponse;
import com.marketplace.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
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
        // Extract current user ID from authentication
        Long currentUserId = getCurrentUserId(authentication);
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
        // Extract current user ID from authentication
        Long currentUserId = getCurrentUserId(authentication);
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
        // Extract current user ID from authentication
        Long currentUserId = getCurrentUserId(authentication);
        productService.deleteProduct(id, currentUserId);
        return ResponseEntity.ok(
            ApiResponse.success("Product deleted successfully")
        );
    }

    /**
     * Helper method to extract user ID from Spring Security Authentication object
     */
    private Long getCurrentUserId(Authentication authentication) {
        // In a real application, the user ID would be extracted from the JWT token or session
        // For now, we use a placeholder based on username (this should be improved)
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            // In a real scenario, you would query the UserRepository to get the ID from username
            // For testing, assuming username format contains userId info or retrieve from context
            // TODO: Enhance this to properly extract userId from JWT claims or UserDetails custom implementation
            return Long.parseLong(username.replaceAll("\\D+", "1")); // Placeholder extraction
        }
        throw new IllegalStateException("Unable to extract user ID from authentication");
    }
}

