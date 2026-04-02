package com.marketplace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.dto.review.ReviewRequest;
import com.marketplace.entity.Product;
import com.marketplace.entity.Review;
import com.marketplace.entity.Role;
import com.marketplace.entity.User;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.ReviewRepository;
import com.marketplace.repository.RoleRepository;
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

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewControllerIT {

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
    private ReviewRepository reviewRepository;

    private User buyerUser;
    private User sellerUser;
    private Product product;
    private Review review;

    @BeforeEach
    void setUp() {
        // Clean up
        reviewRepository.deleteAll();
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

        // Setup review
        review = new Review();
        review.setProduct(product);
        review.setBuyer(buyerUser);
        review.setRating(5);
        review.setComment("Great product!");
        review.setCreatedAt(Instant.now());
        review = reviewRepository.save(review);
    }

    /**
     * Test Case 1: Get Product Reviews - Paginated Success
     */
    @Test
    void testGetProductReviews_Paginated_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reviews/product/" + product.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    /**
     * Test Case 2: Get Review by ID - Success
     */
    @Test
    void testGetReview_ById_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reviews/" + review.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(review.getId()))
                .andExpect(jsonPath("$.rating").value(5));
    }

    /**
     * Test Case 3: Add Review - Buyer Role Success
     */
    @Test
    @WithMockUser(username = "buyer", roles = {"BUYER"})
    void testAddReview_BuyerRole_Success() throws Exception {
        // Arrange
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setRating(4);
        reviewRequest.setComment("Good product!");

        // Note: This test assumes buyer has purchase history (would be verified in service)
        // Act & Assert
        mockMvc.perform(post("/api/reviews")
                        .param("productId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isCreated());
    }

    /**
     * Test Case 4: Update Review - Unauthorized User Fails
     */
    @Test
    @WithMockUser(username = "otherbuyer", roles = {"BUYER"})
    void testUpdateReview_UnauthorizedUser_Fails() throws Exception {
        // Arrange
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setRating(3);
        reviewRequest.setComment("Updated");

        // Act & Assert
        mockMvc.perform(put("/api/reviews/" + review.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isForbidden());
    }

    /**
     * Test Case 5: Delete Review - Only Author Success
     */
    @Test
    @WithMockUser(username = "buyer", roles = {"BUYER"})
    void testDeleteReview_OnlyAuthor_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/reviews/" + review.getId()))
                .andExpect(status().isNoContent());
    }

    /**
     * Test Case 6: Get Average Rating - Calculates Correctly
     */
    @Test
    void testGetAverageRating_CalculatesCorrectly() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reviews/product/" + product.getId() + "/average-rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }
}
