package com.marketplace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.dto.product.ProductRequest;
import com.marketplace.entity.Product;
import com.marketplace.entity.User;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {"/sql/test-cleanup.sql", "/sql/test-seed-roles.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User seller;
    private Product product;

    @BeforeEach
    void setUp() {
        // Create and save seller user
        seller = new User();
        seller.setUsername("seller1");
        seller.setFullName("Seller One");
        seller.setEmail("seller1@example.com");
        seller.setPassword("password123");
        seller = userRepository.save(seller);

        // Create and save product
        product = new Product();
        product.setProductName("Test Product");
        product.setDescription("A test product");
        product.setPrice(99.99);
        product.setStock(10);
        product.setCategory("Electronics");
        product.setSeller(seller);
        product = productRepository.save(product);
    }

    /**
     * Test Case 1: Get Products Endpoint - Public access
     */
    @Test
    void testGetProductsEndpoint_PublicAccess_Success() throws Exception {
        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].productName").value("Test Product"));
    }

    /**
     * Test Case 2: Get Product By ID Endpoint - Public access
     */
    @Test
    void testGetProductByIdEndpoint_PublicAccess_Success() throws Exception {
        mockMvc.perform(get("/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productName").value("Test Product"))
                .andExpect(jsonPath("$.data.price").value(99.99));
    }

    /**
     * Test Case 3: Create Product Endpoint - SELLER only
     */
    @Test
    void testCreateProductEndpoint_SellerRole_Success() throws Exception {
        ProductRequest productRequest = new ProductRequest(
                "New Product",
                "A new product description for testing",
                199.99,
                20,
                "Electronics"
        );

        mockMvc.perform(post("/products")
                .with(user(String.valueOf(seller.getId())).roles("SELLER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.productName").value("New Product"))
                .andExpect(jsonPath("$.data.price").value(199.99));
    }

    /**
     * Test Case 4: Create Product Endpoint - BUYER unauthorized
     */
    @Test
    void testCreateProductEndpoint_BuyerRole_Unauthorized() throws Exception {
        ProductRequest productRequest = new ProductRequest(
                "New Product",
                "A new product description for testing",
                199.99,
                20,
                "Electronics"
        );

        mockMvc.perform(post("/products")
                .with(user("buyer").roles("BUYER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isForbidden());
    }

    /**
     * Test Case 5: Update Product Endpoint - SELLER owner only
     */
    @Test
    void testUpdateProductEndpoint_SellerOwner_Success() throws Exception {
        ProductRequest updateRequest = new ProductRequest(
                "Updated Product",
                "Updated product description for testing",
                149.99,
                15,
                "Electronics"
        );

        mockMvc.perform(put("/products/" + product.getId())
                .with(user(String.valueOf(seller.getId())).roles("SELLER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productName").value("Updated Product"));
    }

    /**
     * Test Case 6: Delete Product Endpoint - SELLER owner only
     */
    @Test
    void testDeleteProductEndpoint_SellerOwner_Success() throws Exception {
        mockMvc.perform(delete("/products/" + product.getId())
                                .with(user(String.valueOf(seller.getId())).roles("SELLER"))
                .with(csrf()))
                .andExpect(status().isOk());
    }

    /**
     * Test Case 7: Get Non-existent Product - 404
     */
    @Test
    void testGetProductByIdEndpoint_NotFound() throws Exception {
                mockMvc.perform(get("/products/999"))
                .andExpect(status().isNotFound());
    }
}
