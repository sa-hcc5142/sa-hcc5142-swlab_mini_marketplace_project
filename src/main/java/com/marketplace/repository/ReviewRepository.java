package com.marketplace.repository;

import com.marketplace.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Review entity
 * Handles database operations for product reviews
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * Find all reviews for a specific product with pagination
     */
    Page<Review> findByProduct_Id(Long productId, Pageable pageable);
    
    /**
     * Check if a buyer has already reviewed a product
     */
    boolean existsByProduct_IdAndBuyer_Id(Long productId, Long buyerId);
    
    /**
     * Find a review by product and buyer
     */
    Review findByProduct_IdAndBuyer_Id(Long productId, Long buyerId);
    
    /**
     * Get average rating for a product
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingByProduct(@Param("productId") Long productId);
}
