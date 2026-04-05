package com.marketplace.controller;

import com.marketplace.dto.ApiResponse;
import com.marketplace.dto.review.ReviewRequest;
import com.marketplace.dto.review.ReviewResponse;
import com.marketplace.security.CurrentUserResolver;
import com.marketplace.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST Controller for product reviews
 * Handles review creation, retrieval, updates, and deletion
 */
@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ReviewController {
    
    private final ReviewService reviewService;
    private final CurrentUserResolver currentUserResolver;
    
    public ReviewController(ReviewService reviewService, CurrentUserResolver currentUserResolver) {
        this.reviewService = reviewService;
        this.currentUserResolver = currentUserResolver;
    }
    
    /**
     * Get all reviews for a product with pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> reviews = reviewService.getProductReviews(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved successfully", reviews));
    }
    
    /**
     * Get a specific review by ID
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId) {
        
        ReviewResponse review = reviewService.getReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review retrieved successfully", review));
    }
    
    /**
     * Add a review for a product (BUYER only)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest reviewRequest,
            Authentication authentication) {
        
        Long buyerId = currentUserResolver.resolveUserId(authentication);
        ReviewResponse review = reviewService.addReview(productId, buyerId, reviewRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review added successfully", review));
    }
    
    /**
     * Update your own review (BUYER only)
     */
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest reviewRequest,
            Authentication authentication) {
        
        Long buyerId = currentUserResolver.resolveUserId(authentication);
        ReviewResponse review = reviewService.updateReview(reviewId, buyerId, reviewRequest);
        return ResponseEntity.ok(ApiResponse.success("Review updated successfully", review));
    }
    
    /**
     * Delete your own review (BUYER only)
     */
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            Authentication authentication) {
        
        Long buyerId = currentUserResolver.resolveUserId(authentication);
        reviewService.deleteReview(reviewId, buyerId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully"));
    }
    
    /**
     * Get average rating for a product
     */
    @GetMapping("/rating/average")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(@PathVariable Long productId) {
        Double averageRating = reviewService.getProductAverageRating(productId);
        return ResponseEntity.ok(ApiResponse.success("Average rating retrieved successfully", averageRating));
    }
}
