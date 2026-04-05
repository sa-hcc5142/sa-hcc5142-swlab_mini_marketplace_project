package com.marketplace.service.impl;

import com.marketplace.dto.order.OrderRequest;
import com.marketplace.dto.order.OrderItemRequest;
import com.marketplace.dto.order.OrderResponse;
import com.marketplace.entity.Order;
import com.marketplace.entity.OrderItem;
import com.marketplace.entity.Product;
import com.marketplace.entity.User;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.InvalidOperationException;
import com.marketplace.mapper.OrderMapper;
import com.marketplace.repository.OrderRepository;
import com.marketplace.repository.OrderItemRepository;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.UserRepository;
import com.marketplace.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                          ProductRepository productRepository, UserRepository userRepository,
                          OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderMapper = orderMapper;
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

        savedOrder.setItems(orderItems);

        // Return response
        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getBuyerOrders(Long buyerId, Pageable pageable) {
        // Verify buyer exists
        userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + buyerId));

        // Fetch orders for buyer
        Page<Order> ordersPage = orderRepository.findByBuyer_Id(buyerId, pageable);

        return ordersPage.map(orderMapper::toOrderResponse);
    }
}

