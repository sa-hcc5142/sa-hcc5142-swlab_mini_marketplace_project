package com.marketplace.service;

import com.marketplace.dto.review.ReviewRequest;
import com.marketplace.dto.review.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for review management
 * Handles product reviews, ratings, and review moderation
 */
public interface ReviewService {
    
    /**
     * Add a review for a product
     * @param productId the product ID
     * @param buyerId the buyer ID  
     * @param reviewRequest the review details
     * @return ReviewResponse with review details
     * @throws ResourceNotFoundException if product or buyer not found
     * @throws InvalidOperationException if buyer hasn't purchased the product
     */
    ReviewResponse addReview(Long productId, Long buyerId, ReviewRequest reviewRequest);
    
    /**
     * Get all reviews for a product with pagination
     * @param productId the product ID
     * @param pageable pagination info
     * @return Page of ReviewResponse
     * @throws ResourceNotFoundException if product not found
     */
    Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable);
    
    /**
     * Get a specific review by ID
     * @param reviewId the review ID
     * @return ReviewResponse
     * @throws ResourceNotFoundException if review not found
     */
    ReviewResponse getReview(Long reviewId);
    
    /**
     * Update a review
     * @param reviewId the review ID
     * @param buyerId the buyer ID (must be the review author)
     * @param reviewRequest updated review details
     * @return ReviewResponse
     * @throws ResourceNotFoundException if review not found
     * @throws InvalidOperationException if buyer is not the review author
     */
    ReviewResponse updateReview(Long reviewId, Long buyerId, ReviewRequest reviewRequest);
    
    /**
     * Delete a review
     * @param reviewId the review ID
     * @param buyerId the buyer ID (must be the review author or admin)
     * @throws ResourceNotFoundException if review not found
     * @throws InvalidOperationException if buyer is not the review author
     */
    void deleteReview(Long reviewId, Long buyerId);
    
    /**
     * Get average rating for a product
     * @param productId the product ID
     * @return average rating (0-5)
     * @throws ResourceNotFoundException if product not found
     */
    Double getProductAverageRating(Long productId);
}
