package com.marketplace.repository;

import com.marketplace.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for CartItem entity
 * Provides database access for items in shopping carts
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    /**
     * Find all items in a specific cart
     * @param cartId the cart ID
     * @return list of CartItem entities
     */
    List<CartItem> findByCart_Id(Long cartId);
    
    /**
     * Find a specific cart item by cart ID and product ID
     * @param cartId the cart ID
     * @param productId the product ID
     * @return Optional containing the CartItem if found
     */
    Optional<CartItem> findByCart_IdAndProduct_Id(Long cartId, Long productId);
    
    /**
     * Delete all items from a cart
     * @param cartId the cart ID
     */
    void deleteByCart_Id(Long cartId);
}
