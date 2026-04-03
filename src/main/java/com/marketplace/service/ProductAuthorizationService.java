package com.marketplace.service;

import com.marketplace.entity.Product;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.UnauthorizedOperationException;
import com.marketplace.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductAuthorizationService {

    private final ProductRepository productRepository;

    public ProductAuthorizationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Verify that the current user is the owner of the product.
     * Throws UnauthorizedOperationException if user is not the product owner.
     *
     * @param productId the ID of the product
     * @param currentUserId the ID of the current user
     * @throws ResourceNotFoundException if product not found
     * @throws UnauthorizedOperationException if user is not the product owner
     */
    public void verifyProductOwner(Long productId, Long currentUserId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!isProductOwner(productId, currentUserId)) {
            throw new UnauthorizedOperationException(
                "You are not authorized to perform this operation on this product. " +
                "Only the product seller can edit or delete their products."
            );
        }
    }

    /**
     * Check if the current user is the owner of the product.
     * Returns false if product not found (product existence check should happen separately).
     *
     * @param productId the ID of the product
     * @param currentUserId the ID of the current user
     * @return true if user is the product owner, false otherwise
     */
    public boolean isProductOwner(Long productId, Long currentUserId) {
        return productRepository.findById(productId)
            .map(product -> product.getSeller().getId().equals(currentUserId))
            .orElse(false);
    }

    /**
     * Get product owner ID for a given product.
     * Useful for logging and auditing purposes.
     *
     * @param productId the ID of the product
     * @return the ID of the product owner (seller)
     * @throws ResourceNotFoundException if product not found
     */
    public Long getProductOwnerId(Long productId) {
        return productRepository.findById(productId)
            .map(product -> product.getSeller().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }
}
