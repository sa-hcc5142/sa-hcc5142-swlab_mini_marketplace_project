package com.marketplace.mapper;

import com.marketplace.dto.order.OrderItemResponse;
import com.marketplace.dto.order.OrderResponse;
import com.marketplace.entity.Order;
import com.marketplace.entity.OrderItem;
import com.marketplace.entity.Product;
import com.marketplace.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrderMapperTest {

    private final OrderMapper orderMapper = new OrderMapper();

    @Test
    void toOrderResponse_ReturnsNull_WhenOrderIsNull() {
        assertNull(orderMapper.toOrderResponse(null));
    }

    @Test
    void toOrderItemResponse_ReturnsNull_WhenOrderItemIsNull() {
        assertNull(orderMapper.toOrderItemResponse(null));
    }

    @Test
    void toOrderResponse_MapsOrderWithItems() {
        User buyer = new User();
        buyer.setId(11L);

        Product product = new Product();
        product.setId(101L);
        product.setProductName("Mechanical Keyboard");

        OrderItem item = new OrderItem();
        item.setId(501L);
        item.setProduct(product);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("50.00"));
        item.setSubtotal(new BigDecimal("100.00"));

        Order order = new Order();
        order.setId(1001L);
        order.setBuyer(buyer);
        order.setStatus("PENDING");
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setCreatedAt(Instant.parse("2026-04-04T10:00:00Z"));
        order.setUpdatedAt(Instant.parse("2026-04-04T10:05:00Z"));
        order.setItems(List.of(item));

        OrderResponse response = orderMapper.toOrderResponse(order);

        assertNotNull(response);
        assertEquals(1001L, response.getId());
        assertEquals(11L, response.getBuyerId());
        assertEquals("PENDING", response.getStatus());
        assertEquals(100.0, response.getTotalPrice());
        assertNotNull(response.getItems());
        assertEquals(1, response.getItems().size());

        OrderItemResponse mappedItem = response.getItems().get(0);
        assertEquals(501L, mappedItem.getId());
        assertEquals(101L, mappedItem.getProductId());
        assertEquals("Mechanical Keyboard", mappedItem.getProductName());
        assertEquals(2, mappedItem.getQuantity());
        assertEquals(50.0, mappedItem.getPricePerUnit());
        assertEquals(100.0, mappedItem.getSubtotal());
    }

    @Test
    void toOrderResponse_MapsSafely_WhenNestedFieldsAreMissing() {
        OrderItem item = new OrderItem();
        item.setId(9L);
        item.setQuantity(1);

        Order order = new Order();
        order.setId(77L);
        order.setStatus("PENDING");
        order.setItems(List.of(item));

        OrderResponse response = orderMapper.toOrderResponse(order);

        assertNotNull(response);
        assertEquals(77L, response.getId());
        assertNull(response.getBuyerId());
        assertEquals(0.0, response.getTotalPrice());
        assertEquals(1, response.getItems().size());
        assertNull(response.getItems().get(0).getProductId());
    }
}
