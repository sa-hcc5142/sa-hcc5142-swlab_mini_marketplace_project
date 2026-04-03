package com.marketplace.service;

import com.marketplace.dto.order.OrderRequest;
import com.marketplace.dto.order.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    /**
     * Place a new order for a buyer
     * @param orderRequest Order items and quantities
     * @param buyerId ID of the buyer
     * @return OrderResponse with order details
     * @throws com.marketplace.exception.ResourceNotFoundException if product or buyer not found
     * @throws com.marketplace.exception.InvalidOperationException if insufficient stock
     */
    OrderResponse placeOrder(OrderRequest orderRequest, Long buyerId);

    /**
     * Get paginated orders for a specific buyer
     * @param buyerId ID of the buyer
     * @param pageable Pagination information
     * @return Page of OrderResponse for the buyer
     * @throws com.marketplace.exception.ResourceNotFoundException if buyer not found
     */
    Page<OrderResponse> getBuyerOrders(Long buyerId, Pageable pageable);
}
