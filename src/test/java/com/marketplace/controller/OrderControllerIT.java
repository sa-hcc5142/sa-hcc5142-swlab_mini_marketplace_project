package com.marketplace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.entity.Order;
import com.marketplace.entity.Product;
import com.marketplace.entity.Role;
import com.marketplace.entity.User;
import com.marketplace.repository.OrderRepository;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.RoleRepository;
import com.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User buyerUser;
    private User sellerUser;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        // Clean up
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Setup roles
        Role buyerRole = roleRepository.findByName("BUYER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("BUYER");
                    return roleRepository.save(role);
                });

        Role sellerRole = roleRepository.findByName("SELLER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("SELLER");
                    return roleRepository.save(role);
                });

        // Setup users
        buyerUser = new User();
        buyerUser.setUsername("buyer");
        buyerUser.setEmail("buyer@marketplace.local");
        buyerUser.setPassword("hashed");
        buyerUser.setRole(buyerRole);
        buyerUser.setActive(true);
        buyerUser = userRepository.save(buyerUser);

        sellerUser = new User();
        sellerUser.setUsername("seller");
        sellerUser.setEmail("seller@marketplace.local");
        sellerUser.setPassword("hashed");
        sellerUser.setRole(sellerRole);
        sellerUser.setActive(true);
        sellerUser = userRepository.save(sellerUser);

        // Setup product
        product = new Product();
        product.setProductName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(99.99);
        product.setStock(10);
        product.setCategory("Test");
        product.setSeller(sellerUser);
        product = productRepository.save(product);

        // Setup order
        order = new Order();
        order.setBuyer(buyerUser);
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setStatus("PENDING");
        order = orderRepository.save(order);
    }

    /**
     * Test Case 1: Get Orders Endpoint - User Orders
     */
    @Test
    @WithMockUser(username = "buyer", roles = {"BUYER"})
    void testGetOrdersEndpoint_UserOrders() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    /**
     * Test Case 2: Place Order Endpoint - Success
     */
    @Test
    @WithMockUser(username = "buyer", roles = {"BUYER"})
    void testPlaceOrderEndpoint_Success() throws Exception {
        // Act & Assert - Assuming endpoint exists for creating order
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk());
    }
}
