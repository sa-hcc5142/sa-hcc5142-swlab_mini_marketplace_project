package com.marketplace.repository;

import com.marketplace.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for Cart entity
 * Provides database access for shopping carts
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    /**
     * Find cart by buyer ID
     * @param buyerId the buyer's user ID
     * @return Optional containing the cart if found
     */
    Optional<Cart> findByBuyer_Id(Long buyerId);
    
    /**
     * Check if a cart exists for a buyer
     * @param buyerId the buyer's user ID
     * @return true if cart exists, false otherwise
     */
    boolean existsByBuyer_Id(Long buyerId);
}
