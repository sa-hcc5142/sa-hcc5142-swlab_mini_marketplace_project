package com.marketplace.service.impl;

import com.marketplace.dto.review.ReviewRequest;
import com.marketplace.dto.review.ReviewResponse;
import com.marketplace.entity.Review;
import com.marketplace.entity.Product;
import com.marketplace.entity.User;
import com.marketplace.entity.Order;
import com.marketplace.entity.OrderItem;
import com.marketplace.exception.InvalidOperationException;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.repository.ReviewRepository;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.UserRepository;
import com.marketplace.repository.OrderRepository;
import com.marketplace.repository.OrderItemRepository;
import com.marketplace.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ReviewService
 * Handles product reviews, ratings, and review moderation
 */
@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    
    public ReviewServiceImpl(ReviewRepository reviewRepository, ProductRepository productRepository,
                           UserRepository userRepository, OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }
    
    @Override
    public ReviewResponse addReview(Long productId, Long buyerId, ReviewRequest reviewRequest) {
        // Verify product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // Verify buyer exists
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + buyerId));
        
        // Check if buyer has already reviewed this product
        if (reviewRepository.existsByProduct_IdAndBuyer_Id(productId, buyerId)) {
            throw new InvalidOperationException("Buyer has already reviewed this product");
        }
        
        // Verify buyer has purchased AND received this product (check if any DELIVERED order from this buyer contains this product)
        boolean hasReceivedPurchased = orderRepository.findAll().stream()
                .filter(order -> order.getBuyer().getId().equals(buyerId))
                .filter(order -> order.getStatus().equalsIgnoreCase("DELIVERED"))
                .anyMatch(order -> order.getItems().stream()
                        .anyMatch(item -> item.getProduct().getId().equals(productId)));
        
        if (!hasReceivedPurchased) {
            throw new InvalidOperationException("Buyer must have a DELIVERED order for this product before reviewing");
        }
        
        // Validate rating
        if (reviewRequest.getRating() < 1 || reviewRequest.getRating() > 5) {
            throw new InvalidOperationException("Rating must be between 1 and 5");
        }
        
        // Create review
        Review review = new Review();
        review.setProduct(product);
        review.setBuyer(buyer);
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());
        review.setCreatedAt(Instant.now());
        review.setUpdatedAt(Instant.now());
        
        Review savedReview = reviewRepository.save(review);
        return mapToReviewResponse(savedReview);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        // Verify product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // Fetch reviews with pagination
        Page<Review> reviewsPage = reviewRepository.findByProduct_Id(productId, pageable);
        
        List<ReviewResponse> responses = reviewsPage.getContent().stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(responses, pageable, reviewsPage.getTotalElements());
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        return mapToReviewResponse(review);
    }
    
    @Override
    public ReviewResponse updateReview(Long reviewId, Long buyerId, ReviewRequest reviewRequest) {
        // Verify buyer exists
        userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + buyerId));
        
        // Find review
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        // Verify buyer is the review author
        if (!review.getBuyer().getId().equals(buyerId)) {
            throw new InvalidOperationException("Only the review author can update this review");
        }
        
        // Validate rating
        if (reviewRequest.getRating() < 1 || reviewRequest.getRating() > 5) {
            throw new InvalidOperationException("Rating must be between 1 and 5");
        }
        
        // Update review
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());
        review.setUpdatedAt(Instant.now());
        
        Review updatedReview = reviewRepository.save(review);
        return mapToReviewResponse(updatedReview);
    }
    
    @Override
    public void deleteReview(Long reviewId, Long buyerId) {
        // Verify buyer exists
        User caller = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + buyerId));
        
        // Find review
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        // Check if admin bypass is allowed
        boolean isAdmin = caller.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMIN") || r.getName().equals("ROLE_ADMIN"));

        // Verify buyer is the author or admin
        if (!isAdmin && !review.getBuyer().getId().equals(buyerId)) {
            throw new InvalidOperationException("Only the review author or an ADMIN can delete this review");
        }
        
        reviewRepository.delete(review);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double getProductAverageRating(Long productId) {
        // Verify product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        Double averageRating = reviewRepository.getAverageRatingByProduct(productId);
        return averageRating != null ? averageRating : 0.0;
    }
    
    /**
     * Convert Review entity to ReviewResponse DTO
     */
    private ReviewResponse mapToReviewResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setProductId(review.getProduct().getId());
        response.setProductName(review.getProduct().getProductName());
        response.setBuyerId(review.getBuyer().getId());
        response.setBuyerUsername(review.getBuyer().getEmail());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(instantToLocalDateTime(review.getCreatedAt()));
        response.setUpdatedAt(instantToLocalDateTime(review.getUpdatedAt()));
        
        return response;
    }
    
    /**
     * Convert Instant to LocalDateTime for API response
     */
    private LocalDateTime instantToLocalDateTime(Instant instant) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
