package com.marketplace.service;

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
import com.marketplace.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testBuyer;
    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testBuyer = new User();
        testBuyer.setId(1L);
        testBuyer.setEmail("buyer@example.com");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setProductName("Test Product");
        testProduct.setPrice(50.0);
        testProduct.setStock(10);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setBuyer(testBuyer);
        testOrder.setStatus("PENDING");
        testOrder.setTotalAmount(new java.math.BigDecimal("100.0"));
        testOrder.setCreatedAt(java.time.Instant.now());
        testOrder.setUpdatedAt(java.time.Instant.now());
    }

    @Test
    void placeOrder_ShouldCreateOrderAndDeductStock() {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest(1L, 2);
        OrderRequest orderRequest = new OrderRequest(Arrays.asList(itemRequest));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testBuyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(new OrderItem());
        when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(new OrderResponse(
            1L,
            1L,
            List.of(),
            100.0,
            "PENDING",
            null,
            null
        ));

        // Act
        OrderResponse response = orderService.placeOrder(orderRequest, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getBuyerId());
        assertEquals(100.0, response.getTotalPrice());
        assertEquals("PENDING", response.getStatus());

        // Verify stock was deducted
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertEquals(8, productCaptor.getValue().getStock());

        // Verify order was saved
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    void placeOrder_ShouldThrowWhenStockInsufficient() {
        // Arrange
        testProduct.setStock(1); // Only 1 item in stock
        OrderItemRequest itemRequest = new OrderItemRequest(1L, 5); // Requesting 5
        OrderRequest orderRequest = new OrderRequest(Arrays.asList(itemRequest));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testBuyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> {
            orderService.placeOrder(orderRequest, 1L);
        });

        // Verify stock was NOT deducted
        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_ShouldThrowWhenProductNotFound() {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest(999L, 2);
        OrderRequest orderRequest = new OrderRequest(Arrays.asList(itemRequest));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testBuyer));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.placeOrder(orderRequest, 1L);
        });

        // Verify no order was created
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getBuyerOrders_ShouldReturnPaginatedOrders() {
        // Arrange
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setId(1L);
        orderItem1.setProduct(testProduct);
        orderItem1.setQuantity(2);
        orderItem1.setUnitPrice(new java.math.BigDecimal("50.0"));
        orderItem1.setSubtotal(new java.math.BigDecimal("100.0"));

        List<Order> orders = Arrays.asList(testOrder);
        Page<Order> ordersPage = new PageImpl<>(orders);
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testBuyer));
        when(orderRepository.findByBuyer_Id(1L, pageable)).thenReturn(ordersPage);
        when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(new OrderResponse(
            1L,
            1L,
            List.of(),
            100.0,
            "PENDING",
            null,
            null
        ));

        // Act
        Page<OrderResponse> result = orderService.getBuyerOrders(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals(1L, result.getContent().get(0).getBuyerId());

        // Verify repository calls
        verify(userRepository).findById(1L);
        verify(orderRepository).findByBuyer_Id(1L, pageable);
        // Note: Items are accessed via order.getItems() relationship, not explicit repository call
    }

    @Test
    void getBuyerOrders_ShouldThrowWhenBuyerNotFound() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getBuyerOrders(999L, pageable);
        });

        // Verify only buyer lookup was attempted
        verify(userRepository).findById(999L);
        verify(orderRepository, never()).findByBuyer_Id(anyLong(), any(Pageable.class));
    }
}
