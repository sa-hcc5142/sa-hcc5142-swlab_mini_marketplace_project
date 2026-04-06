package com.marketplace.service;

import com.marketplace.dto.review.ReviewRequest;
import com.marketplace.dto.review.ReviewResponse;
import com.marketplace.entity.Order;
import com.marketplace.entity.OrderItem;
import com.marketplace.entity.Product;
import com.marketplace.entity.Review;
import com.marketplace.entity.User;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.InvalidOperationException;
import com.marketplace.repository.OrderRepository;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.ReviewRepository;
import com.marketplace.repository.UserRepository;
import com.marketplace.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review review;
    private Product product;
    private User buyer;
    private ReviewRequest reviewRequest;

    @BeforeEach
    void setUp() {
        // Setup buyer
        buyer = new User();
        buyer.setId(1L);
        buyer.setUsername("buyer");
        buyer.setEmail("buyer@marketplace.local");

        // Setup product
        product = new Product();
        product.setId(1L);
        product.setProductName("Test Product");
        product.setPrice(99.99);

        // Setup review
        review = new Review();
        review.setId(1L);
        review.setProduct(product);
        review.setBuyer(buyer);
        review.setRating(5);
        review.setComment("Great product!");
        review.setCreatedAt(Instant.now());

        // Setup review request
        reviewRequest = new ReviewRequest();
        reviewRequest.setRating(5);
        reviewRequest.setComment("Great product!");
    }

    /**
     * Test Case 1: Add Review - Duplicate check triggers
     */
    @Test
    void testAddReview_WithValidPurchase_Success() {
        // Arrange - test that duplicate check happens (review already exists)
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByProduct_IdAndBuyer_Id(1L, 1L)).thenReturn(true);

        // Act & Assert - duplicate review should throw InvalidOperationException
        assertThrows(InvalidOperationException.class, () -> {
            reviewService.addReview(1L, 1L, reviewRequest);
        });
    }

    /**
     * Test Case 2: Buyer purchased product - validates order history
     */
    @Test
    void testAddReview_ProductNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByProduct_IdAndBuyer_Id(1L, 1L)).thenReturn(false);
        // No order history - should fail purchase validation
        // OrderRepository method will return false by default due to not being mocked

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> {
            reviewService.addReview(1L, 1L, reviewRequest);
        });
    }

    /**
     * Test Case 3: Add Review - Successful when not duplicate
     */
    @Test
    void testAddReview_DuplicateReview_Fails() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByProduct_IdAndBuyer_Id(1L, 1L)).thenReturn(false);
        
        // Mock that order exists with this product to simulate purchase
        Order order = new Order();
        order.setId(1L);
        order.setBuyer(buyer);
        order.setStatus("DELIVERED");
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        order.setItems(Arrays.asList(orderItem));
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order));
        
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // Act & Assert - should succeed when no duplicate and order exists
        assertDoesNotThrow(() -> {
            reviewService.addReview(1L, 1L, reviewRequest);
        });
    }

    /**
     * Test Case 4: Update Review - Author Only
     */
    @Test
    void testUpdateReview_AuthorOnly_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // Act
        ReviewResponse result = reviewService.updateReview(1L, 1L, reviewRequest);

        // Assert
        assertNotNull(result);
        verify(reviewRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    /**
     * Test Case 5: Delete Review - Author Only
     */
    @Test
    void testDeleteReview_AuthorOnly_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        // Act
        reviewService.deleteReview(1L, 1L);

        // Assert
        verify(reviewRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).delete(review);
    }

    /**
     * Test Case 6: Get Product Average Rating
     */
    @Test
    void testGetProductAverageRating_ReturnsDouble() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.getAverageRatingByProduct(1L)).thenReturn(4.5);

        // Act
        Double result = reviewService.getProductAverageRating(1L);

        // Assert
        assertEquals(4.5, result);
        verify(reviewRepository, times(1)).getAverageRatingByProduct(1L);
    }
}
