package com.marketplace.service.impl;

import com.marketplace.dto.order.OrderRequest;
import com.marketplace.dto.order.OrderItemRequest;
import com.marketplace.dto.order.OrderResponse;
import com.marketplace.dto.order.OrderItemResponse;
import com.marketplace.entity.Order;
import com.marketplace.entity.OrderItem;
import com.marketplace.entity.Product;
import com.marketplace.entity.User;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.InvalidOperationException;
import com.marketplace.repository.OrderRepository;
import com.marketplace.repository.OrderItemRepository;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.UserRepository;
import com.marketplace.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                          ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public OrderResponse placeOrder(OrderRequest orderRequest, Long buyerId) {
        // Verify buyer exists
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + buyerId));

        // Create order
        Order order = new Order();
        order.setBuyer(buyer);
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.ZERO);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        // Process order items and validate stock
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new java.util.ArrayList<>();

        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemRequest.getProductId()));

            // Validate stock availability
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new InvalidOperationException(
                        "Insufficient stock for product: " + product.getProductName() +
                        ". Available: " + product.getStock() + ", Requested: " + itemRequest.getQuantity());
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(new BigDecimal(product.getPrice().toString()));

            BigDecimal subtotal = orderItem.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            orderItem.setSubtotal(subtotal);

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(subtotal);

            // Deduct stock
            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        // Set total amount and save order
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Save order items
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }

        // Return response
        return mapToOrderResponse(savedOrder, orderItems);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getBuyerOrders(Long buyerId, Pageable pageable) {
        // Verify buyer exists
        userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + buyerId));

        // Fetch orders for buyer
        Page<Order> ordersPage = orderRepository.findByBuyer_Id(buyerId, pageable);

        // Map to response DTOs
        List<OrderResponse> responses = ordersPage.getContent().stream()
                .map(order -> mapToOrderResponse(order, order.getItems()))
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, ordersPage.getTotalElements());
    }

    private OrderResponse mapToOrderResponse(Order order, List<OrderItem> orderItems) {
        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : 0.0,
                        item.getSubtotal() != null ? item.getSubtotal().doubleValue() : 0.0
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getBuyer().getId(),
                itemResponses,
                order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0,
                order.getStatus(),
                instantToLocalDateTime(order.getCreatedAt()),
                instantToLocalDateTime(order.getUpdatedAt())
        );
    }

    private java.time.LocalDateTime instantToLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
    }
}

