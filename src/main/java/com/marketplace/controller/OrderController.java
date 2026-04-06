package com.marketplace.controller;

import com.marketplace.dto.ApiResponse;
import com.marketplace.dto.order.OrderRequest;
import com.marketplace.dto.order.OrderResponse;
import com.marketplace.dto.order.StatusUpdateRequest;
import com.marketplace.security.CurrentUserResolver;
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
@RequestMapping("/api/orders")
@Validated
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserResolver currentUserResolver;

    public OrderController(OrderService orderService, CurrentUserResolver currentUserResolver) {
        this.orderService = orderService;
        this.currentUserResolver = currentUserResolver;
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

        Long buyerId = currentUserResolver.resolveUserId(authentication);

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

        Long buyerId = currentUserResolver.resolveUserId(authentication);

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> ordersPage = orderService.getBuyerOrders(buyerId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", ordersPage));
    }

    /**
     * Get all orders globally (ADMIN only)
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> ordersPage = orderService.getAllOrders(pageable);

        return ResponseEntity.ok(ApiResponse.success("All orders retrieved successfully", ordersPage));
    }

    /**
     * Update order status (ADMIN only)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {

        OrderResponse updatedOrder = orderService.updateOrderStatus(id, request.status());
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", updatedOrder));
    }
}
