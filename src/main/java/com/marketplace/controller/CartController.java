package com.marketplace.controller;

import com.marketplace.dto.ApiResponse;
import com.marketplace.dto.cart.CartItemRequest;
import com.marketplace.dto.cart.CartResponse;
import com.marketplace.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST Controller for shopping cart management
 * Handles cart operations including add, remove, update items
 */
@RestController
@RequestMapping("/api/carts")
public class CartController {
    
    private final CartService cartService;
    
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    
    /**
     * Get buyer's cart
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        Long buyerId = getCurrentUserId(authentication);
        CartResponse cart = cartService.getCart(buyerId);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }
    
    /**
     * Add item to cart
     */
    @PostMapping("/me/items")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @Valid @RequestBody CartItemRequest itemRequest,
            Authentication authentication) {
        Long buyerId = getCurrentUserId(authentication);
        CartResponse cart = cartService.addItem(buyerId, itemRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart successfully", cart));
    }
    
    /**
     * Update item quantity in cart
     */
    @PutMapping("/me/items/{cartItemId}")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        Long buyerId = getCurrentUserId(authentication);
        CartResponse cart = cartService.updateItem(buyerId, cartItemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Item updated successfully", cart));
    }
    
    /**
     * Remove item from cart
     */
    @DeleteMapping("/me/items/{cartItemId}")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Long cartItemId,
            Authentication authentication) {
        Long buyerId = getCurrentUserId(authentication);
        CartResponse cart = cartService.removeItem(buyerId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", cart));
    }
    
    /**
     * Clear entire cart
     */
    @DeleteMapping("/me/items")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication authentication) {
        Long buyerId = getCurrentUserId(authentication);
        cartService.clearCart(buyerId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully"));
    }
    
    /**
     * Helper method to extract user ID from Spring Security Authentication object
     */
    private Long getCurrentUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            // In a real scenario, extract userId from JWT claims or UserDetails custom implementation
            return 1L; // Placeholder - should be extracted from authentication
        }
        throw new IllegalStateException("Unable to extract user ID from authentication");
    }
}
