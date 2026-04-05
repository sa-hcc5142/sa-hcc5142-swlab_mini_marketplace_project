package com.marketplace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.dto.order.OrderItemRequest;
import com.marketplace.dto.order.OrderRequest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {"/sql/test-cleanup.sql", "/sql/test-seed-roles.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
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
        buyerUser.setFullName("Buyer User");
        buyerUser.setEmail("buyer@marketplace.local");
        buyerUser.setPassword("hashed");
        buyerUser.setRole(buyerRole);
        buyerUser.setActive(true);
        buyerUser = userRepository.save(buyerUser);

        sellerUser = new User();
        sellerUser.setUsername("seller");
        sellerUser.setFullName("Seller User");
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
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order = orderRepository.save(order);
    }

    /**
     * Test Case 1: Get Orders Endpoint - User Orders
     */
    @Test
    void testGetOrdersEndpoint_UserOrders() throws Exception {
        mockMvc.perform(get("/api/orders/me")
                        .with(user(String.valueOf(buyerUser.getId())).roles("BUYER"))
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray());
    }

    /**
     * Test Case 2: Place Order Endpoint - Success
     */
    @Test
    void testPlaceOrderEndpoint_Success() throws Exception {
        OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(product.getId(), 1)));

        mockMvc.perform(post("/api/orders")
                        .with(user(String.valueOf(buyerUser.getId())).roles("BUYER"))
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("PENDING"));
    }
}
