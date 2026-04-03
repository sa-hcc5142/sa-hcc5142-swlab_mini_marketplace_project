package com.marketplace.dto.cart;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for complete shopping cart response
 * Contains cart items, total, and metadata
 */
public class CartResponse {
    
    private Long id;
    private Long buyerId;
    private List<CartItemResponse> items;
    private Double cartTotal;
    private Integer totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public CartResponse() {}
    
    public CartResponse(Long id, Long buyerId, List<CartItemResponse> items, Double cartTotal,
                       Integer totalItems, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.buyerId = buyerId;
        this.items = items;
        this.cartTotal = cartTotal;
        this.totalItems = totalItems;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    
    public List<CartItemResponse> getItems() { return items; }
    public void setItems(List<CartItemResponse> items) { this.items = items; }
    
    public Double getCartTotal() { return cartTotal; }
    public void setCartTotal(Double cartTotal) { this.cartTotal = cartTotal; }
    
    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
