package com.marketplace.mapper;

import com.marketplace.dto.order.OrderItemResponse;
import com.marketplace.dto.order.OrderResponse;
import com.marketplace.entity.Order;
import com.marketplace.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting Order and OrderItem entities to DTOs
 * Handles bidirectional conversion between domain objects and API representations
 */
@Component
public class OrderMapper {

    /**
     * Convert Order entity to OrderResponse DTO
     * Includes nested OrderItemResponse for each OrderItem
     *
     * @param order Order entity to convert
     * @return OrderResponse DTO with all fields populated
     */
    public OrderResponse toOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setBuyerId(order.getBuyer() != null ? order.getBuyer().getId() : null);
        response.setStatus(order.getStatus());
        response.setTotalPrice(order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0);
        response.setCreatedAt(instantToLocalDateTime(order.getCreatedAt()));
        response.setUpdatedAt(instantToLocalDateTime(order.getUpdatedAt()));

        // Map nested OrderItems to OrderItemResponse list
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            List<OrderItemResponse> items = order.getItems()
                    .stream()
                    .map(this::toOrderItemResponse)
                    .collect(Collectors.toList());
            response.setItems(items);
        }

        return response;
    }

    /**
     * Convert OrderItem entity to OrderItemResponse DTO
     * Includes product details for reference
     *
     * @param orderItem OrderItem entity to convert
     * @return OrderItemResponse DTO with pricing and product info
     */
    public OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        OrderItemResponse response = new OrderItemResponse();
        response.setId(orderItem.getId());
        response.setProductId(orderItem.getProduct() != null ? orderItem.getProduct().getId() : null);
        response.setProductName(orderItem.getProduct() != null ? orderItem.getProduct().getProductName() : null);
        response.setQuantity(orderItem.getQuantity());
        response.setPricePerUnit(orderItem.getUnitPrice() != null ? orderItem.getUnitPrice().doubleValue() : 0.0);
        response.setSubtotal(orderItem.getSubtotal() != null ? orderItem.getSubtotal().doubleValue() : 0.0);

        return response;
    }

    /**
     * Convert Instant to LocalDateTime for API responses
     */
    private LocalDateTime instantToLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}


