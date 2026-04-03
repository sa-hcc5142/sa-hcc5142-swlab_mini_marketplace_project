package com.marketplace.service.impl;

import com.marketplace.dto.product.ProductRequest;
import com.marketplace.dto.product.ProductResponse;
import com.marketplace.entity.Product;
import com.marketplace.entity.User;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.UnauthorizedOperationException;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.UserRepository;
import com.marketplace.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductServiceImpl(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    @Override
    public ProductResponse createProduct(ProductRequest request, Long sellerId) {
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + sellerId));

        Product product = new Product();
        product.setProductName(request.productName());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(request.category());
        product.setSeller(seller);

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request, Long currentUserId) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Verify ownership: only seller who created the product can update it
        verifyOwnership(product, currentUserId);

        product.setProductName(request.productName());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(request.category());

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id, Long currentUserId) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Verify ownership: only seller who created the product can delete it
        verifyOwnership(product, currentUserId);

        productRepository.delete(product);
    }

    /**
     * Verify that the current user is the owner of the product (seller who created it)
     */
    private void verifyOwnership(Product product, Long currentUserId) {
        if (!product.getSeller().getId().equals(currentUserId)) {
            throw new UnauthorizedOperationException("You are not authorized to perform this operation on this product");
        }
    }

    /**
     * Map Product entity to ProductResponse DTO
     */
    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getProductName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getCategory(),
            product.getSeller().getId(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}
