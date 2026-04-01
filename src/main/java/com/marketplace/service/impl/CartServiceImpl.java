package com.marketplace.service.impl;

import com.marketplace.dto.cart.CartItemRequest;
import com.marketplace.dto.cart.CartItemResponse;
import com.marketplace.dto.cart.CartResponse;
import com.marketplace.entity.Cart;
import com.marketplace.entity.CartItem;
import com.marketplace.entity.Product;
import com.marketplace.entity.User;
import com.marketplace.exception.InvalidOperationException;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.repository.CartItemRepository;
import com.marketplace.repository.CartRepository;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.UserRepository;
import com.marketplace.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of CartService
 * Handles shopping cart operations including add, remove, update items, and stock validation
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository,
                         ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long buyerId) {
        // Verify buyer exists
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + buyerId));
        
        // Get or create cart
        Cart cart = cartRepository.findByBuyer_Id(buyerId)
                .orElseGet(() -> createNewCart(buyer));
        
        return mapToCartResponse(cart);
    }
    
    @Override
    public CartResponse addItem(Long buyerId, CartItemRequest itemRequest) {
        // Verify buyer exists
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + buyerId));
        
        // Get or create cart
        Cart cart = cartRepository.findByBuyer_Id(buyerId)
                .orElseGet(() -> createNewCart(buyer));
        
        // Get product
        Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + itemRequest.getProductId()));
        
        // Validate stock availability
        if (product.getStock() < itemRequest.getQuantity()) {
            throw new InvalidOperationException(
                    "Insufficient stock for product: " + product.getProductName() +
                    ". Available: " + product.getStock() + ", Requested: " + itemRequest.getQuantity());
        }
        
        // Check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCart_IdAndProduct_Id(
                cart.getId(), itemRequest.getProductId());
        
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + itemRequest.getQuantity();
            
            // Validate total quantity against stock
            if (product.getStock() < newQuantity) {
                throw new InvalidOperationException(
                        "Insufficient stock for product: " + product.getProductName() +
                        ". Available: " + product.getStock() + ", Total requested: " + newQuantity);
            }
            
            item.setQuantity(newQuantity);
            item.calculateSubtotal();
        } else {
            // Create new cart item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(itemRequest.getQuantity());
            newItem.setUnitPrice(new BigDecimal(product.getPrice()));
            newItem.calculateSubtotal();
            cart.getItems().add(newItem);
        }
        
        cartRepository.save(cart);
        return mapToCartResponse(cart);
    }
    
    @Override
    public CartResponse updateItem(Long buyerId, Long cartItemId, Integer quantity) {
        // Verify buyer exists
        userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + buyerId));
        
        // Get cart
        Cart cart = cartRepository.findByBuyer_Id(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for buyer: " + buyerId));
        
        // Find cart item
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));
        
        // Verify item belongs to this cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new InvalidOperationException("Cart item does not belong to this cart");
        }
        
        // Validate quantity
        if (quantity <= 0) {
            throw new InvalidOperationException("Quantity must be greater than 0");
        }
        
        // Validate stock
        Product product = cartItem.getProduct();
        if (product.getStock() < quantity) {
            throw new InvalidOperationException(
                    "Insufficient stock for product: " + product.getProductName() +
                    ". Available: " + product.getStock() + ", Requested: " + quantity);
        }
        
        // Update quantity and recalculate subtotal
        cartItem.setQuantity(quantity);
        cartItem.calculateSubtotal();
        cartItemRepository.save(cartItem);
        
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
        
        return mapToCartResponse(cart);
    }
    
    @Override
    public CartResponse removeItem(Long buyerId, Long cartItemId) {
        // Verify buyer exists
        userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + buyerId));
        
        // Get cart
        Cart cart = cartRepository.findByBuyer_Id(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for buyer: " + buyerId));
        
        // Find cart item
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));
        
        // Verify item belongs to this cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new InvalidOperationException("Cart item does not belong to this cart");
        }
        
        // Remove item
        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
        
        return mapToCartResponse(cart);
    }
    
    @Override
    public void clearCart(Long buyerId) {
        // Verify buyer exists
        userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + buyerId));
        
        // Get cart
        Cart cart = cartRepository.findByBuyer_Id(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for buyer: " + buyerId));
        
        // Clear all items
        cartItemRepository.deleteByCart_Id(cart.getId());
        cart.getItems().clear();
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateCartStock(Long buyerId) {
        // Verify buyer exists
        userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + buyerId));
        
        // Get cart
        Cart cart = cartRepository.findByBuyer_Id(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for buyer: " + buyerId));
        
        // Check if all items have sufficient stock
        return cart.getItems().stream()
                .allMatch(item -> item.getProduct().getStock() >= item.getQuantity());
    }
    
    /**
     * Create a new cart for a buyer
     */
    private Cart createNewCart(User buyer) {
        Cart cart = new Cart();
        cart.setBuyer(buyer);
        cart.setCreatedAt(Instant.now());
        cart.setUpdatedAt(Instant.now());
        return cartRepository.save(cart);
    }
    
    /**
     * Convert Cart entity to CartResponse DTO
     */
    private CartResponse mapToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setBuyerId(cart.getBuyer().getId());
        
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);
        
        response.setCartTotal(cart.calculateTotal().doubleValue());
        response.setTotalItems(cart.getTotalItems());
        response.setCreatedAt(instantToLocalDateTime(cart.getCreatedAt()));
        response.setUpdatedAt(instantToLocalDateTime(cart.getUpdatedAt()));
        
        return response;
    }
    
    /**
     * Convert CartItem entity to CartItemResponse DTO
     */
    private CartItemResponse mapToCartItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getProductName());
        response.setProductDescription(item.getProduct().getDescription());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice().doubleValue());
        response.setSubtotal(item.getSubtotal().doubleValue());
        response.setAvailableStock(item.getProduct().getStock());
        response.setAddedAt(instantToLocalDateTime(item.getAddedAt()));
        
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
