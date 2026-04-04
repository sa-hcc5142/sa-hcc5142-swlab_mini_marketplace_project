package com.marketplace.controller;

import com.marketplace.dto.ApiResponse;
import com.marketplace.dto.order.OrderRequest;
import com.marketplace.dto.order.OrderResponse;
import com.marketplace.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
@Validated
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Place a new order
     * Only BUYER and ADMIN roles can create orders
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody OrderRequest orderRequest,
            Authentication authentication) {

        // Extract current user ID from authentication
        Long buyerId = extractUserId(authentication);

        OrderResponse orderResponse = orderService.placeOrder(orderRequest, buyerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", orderResponse));
    }

    /**
     * Get orders for the current buyer
     * Buyers can only see their own orders; Admins can see all orders (implementation can be enhanced)
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getBuyerOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        // Extract current user ID from authentication
        Long buyerId = extractUserId(authentication);

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> ordersPage = orderService.getBuyerOrders(buyerId, pageable);

        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", ordersPage));
    }

    /**
     * Extract user ID from Authentication object
     * TODO: Replace with JWT token extraction for production
     */
    private Long extractUserId(Authentication authentication) {
        // For now, extract from custom user details or principal
        // In production, this would extract from JWT token
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            // Parse user ID from username or use a service to look it up
            // Placeholder: assumes username or custom claim contains user ID
            try {
                return Long.parseLong(username);
            } catch (NumberFormatException e) {
                // Fallback: use a default or service lookup
                return 1L; // Placeholder for demo
            }
        }
        return 1L; // Placeholder for demo
    }
}
