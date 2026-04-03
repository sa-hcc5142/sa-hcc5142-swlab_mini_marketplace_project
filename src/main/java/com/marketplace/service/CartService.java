package com.marketplace.service;

import com.marketplace.dto.cart.CartItemRequest;
import com.marketplace.dto.cart.CartResponse;

/**
 * Service interface for shopping cart operations
 * Defines contract for cart management, item add/remove/update, and validation
 */
public interface CartService {
    
    /**
     * Get the current cart for a buyer
     * Creates a new cart if one doesn't exist
     * @param buyerId the buyer's user ID
     * @return CartResponse containing cart details and items
     */
    CartResponse getCart(Long buyerId);
    
    /**
     * Add an item to the cart
     * If item already exists, update its quantity
     * @param buyerId the buyer's user ID
     * @param itemRequest contains productId and quantity
     * @return updated CartResponse
     */
    CartResponse addItem(Long buyerId, CartItemRequest itemRequest);
    
    /**
     * Update quantity of an item in the cart
     * @param buyerId the buyer's user ID
     * @param cartItemId the cart item ID
     * @param quantity new quantity
     * @return updated CartResponse
     */
    CartResponse updateItem(Long buyerId, Long cartItemId, Integer quantity);
    
    /**
     * Remove an item from the cart
     * @param buyerId the buyer's user ID
     * @param cartItemId the cart item ID
     * @return updated CartResponse
     */
    CartResponse removeItem(Long buyerId, Long cartItemId);
    
    /**
     * Clear all items from the cart
     * @param buyerId the buyer's user ID
     */
    void clearCart(Long buyerId);
    
    /**
     * Validate that all items in cart have sufficient stock
     * @param buyerId the buyer's user ID
     * @return true if all items are in stock with required quantities, false otherwise
     */
    boolean validateCartStock(Long buyerId);
}
