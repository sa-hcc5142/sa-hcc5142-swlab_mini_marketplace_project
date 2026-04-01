package com.marketplace.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart represents a shopping cart for a buyer.
 * Contains multiple CartItems and tracks creation/update timestamps.
 */
@Entity
@Table(name = "carts", uniqueConstraints = {
    @UniqueConstraint(columnNames = "buyer_id")
})
public class Cart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "buyer_id", nullable = false, unique = true)
    private User buyer;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItem> items = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
    
    public Cart() {}
    
    public Cart(Long id, User buyer, List<CartItem> items, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.buyer = buyer;
        this.items = items;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }
    
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    /**
     * Calculate total price of all items in cart
     */
    public BigDecimal calculateTotal() {
        return this.items.stream()
            .map(item -> item.getSubtotal())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get total number of items in cart (sum of quantities)
     */
    public Integer getTotalItems() {
        return this.items.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }
    
    /**
     * Check if cart is empty
     */
    public boolean isEmpty() {
        return this.items.isEmpty();
    }
}
