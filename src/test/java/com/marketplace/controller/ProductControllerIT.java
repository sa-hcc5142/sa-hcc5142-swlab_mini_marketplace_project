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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
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
        seller.setEmail("seller1@example.com");
        seller.setPassword("password123");
        seller = userRepository.save(seller);

        // Create and save product
        product = new Product();
        product.setProductName("Test Product");
        product.setDescription("A test product");
        product.setPrice(99.99);
        product.setStock(10);
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
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].productName").value("Test Product"));
    }

    /**
     * Test Case 2: Get Product By ID Endpoint - Public access
     */
    @Test
    void testGetProductByIdEndpoint_PublicAccess_Success() throws Exception {
        mockMvc.perform(get("/api/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));
    }

    /**
     * Test Case 3: Create Product Endpoint - SELLER only
     */
    @Test
    @WithMockUser(username = "seller1", roles = {"SELLER"})
    void testCreateProductEndpoint_SellerRole_Success() throws Exception {
        ProductRequest productRequest = new ProductRequest(
                "New Product",
                "A new product description for testing",
                199.99,
                20,
                "Electronics"
        );

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("New Product"))
                .andExpect(jsonPath("$.price").value(199.99));
    }

    /**
     * Test Case 4: Create Product Endpoint - BUYER unauthorized
     */
    @Test
    @WithMockUser(username = "buyer1", roles = {"BUYER"})
    void testCreateProductEndpoint_BuyerRole_Unauthorized() throws Exception {
        ProductRequest productRequest = new ProductRequest(
                "New Product",
                "A new product description for testing",
                199.99,
                20,
                "Electronics"
        );

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isForbidden());
    }

    /**
     * Test Case 5: Update Product Endpoint - SELLER owner only
     */
    @Test
    @WithMockUser(username = "seller1", roles = {"SELLER"})
    void testUpdateProductEndpoint_SellerOwner_Success() throws Exception {
        ProductRequest updateRequest = new ProductRequest(
                "Updated Product",
                "Updated product description for testing",
                149.99,
                15,
                "Electronics"
        );

        mockMvc.perform(put("/api/products/" + product.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Updated Product"));
    }

    /**
     * Test Case 6: Delete Product Endpoint - SELLER owner only
     */
    @Test
    @WithMockUser(username = "seller1", roles = {"SELLER"})
    void testDeleteProductEndpoint_SellerOwner_Success() throws Exception {
        mockMvc.perform(delete("/api/products/" + product.getId())
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    /**
     * Test Case 7: Get Non-existent Product - 404
     */
    @Test
    void testGetProductByIdEndpoint_NotFound() throws Exception {
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }
}
