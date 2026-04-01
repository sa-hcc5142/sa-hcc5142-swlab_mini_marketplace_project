package com.marketplace.dto.cart;

import java.time.LocalDateTime;

/**
 * DTO for cart item response
 * Contains product details, quantity, and pricing information
 */
public class CartItemResponse {
    
    private Long id;
    private Long productId;
    private String productName;
    private String productDescription;
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;
    private Integer availableStock;
    private LocalDateTime addedAt;
    
    public CartItemResponse() {}
    
    public CartItemResponse(Long id, Long productId, String productName, String productDescription,
                           Integer quantity, Double unitPrice, Double subtotal, Integer availableStock,
                           LocalDateTime addedAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.availableStock = availableStock;
        this.addedAt = addedAt;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    
    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
