package com.marketplace.service;

import com.marketplace.dto.product.ProductRequest;
import com.marketplace.dto.product.ProductResponse;
import com.marketplace.entity.Product;
import com.marketplace.entity.User;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.UnauthorizedOperationException;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.UserRepository;
import com.marketplace.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private User testSeller;
    private ProductRequest testRequest;

    @BeforeEach
    void setUp() {
        testSeller = new User();
        testSeller.setId(1L);
        testSeller.setFullName("Test Seller");
        testSeller.setEmail("seller@test.com");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setProductName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(99.99);
        testProduct.setStock(10);
        testProduct.setCategory("Electronics");
        testProduct.setSeller(testSeller);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());

        testRequest = new ProductRequest(
            "Updated Product",
            "Updated Description",
            199.99,
            20,
            "Books"
        );
    }

    /**
     * Test 1: Get all products with pagination
     */
    @Test
    void getAllProducts_ShouldReturnPaginatedList() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        // Act
        Page<ProductResponse> result = productService.getAllProducts(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Product", result.getContent().get(0).productName());
        assertEquals(99.99, result.getContent().get(0).price());
        verify(productRepository, times(1)).findAll(pageable);
    }

    /**
     * Test 2: Get product by ID (success case)
     */
    @Test
    void getProductById_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        ProductResponse result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.productName());
        assertEquals(99.99, result.price());
        assertEquals(1L, result.sellerId());
        verify(productRepository, times(1)).findById(1L);
    }

    /**
     * Test 3: Get product by ID (not found case)
     */
    @Test
    void getProductById_ShouldThrowWhenProductNotFound() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(999L));
        verify(productRepository, times(1)).findById(999L);
    }

    /**
     * Test 4: Create product successfully
     */
    @Test
    void createProduct_ShouldSaveAndReturn() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testSeller));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductResponse result = productService.createProduct(testRequest, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.sellerId());
        verify(userRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    /**
     * Test 5: Update product successfully
     */
    @Test
    void updateProduct_ShouldUpdateExistingProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductResponse result = productService.updateProduct(1L, testRequest, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    /**
     * Test 6: Update product - unauthorized user should fail
     */
    @Test
    void updateProduct_ShouldThrowWhenUserIsNotOwner() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert - Different user ID (2L) trying to update product owned by user 1L
        assertThrows(UnauthorizedOperationException.class, 
            () -> productService.updateProduct(1L, testRequest, 2L));
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    /**
     * Test 7: Delete product successfully
     */
    @Test
    void deleteProduct_ShouldRemoveProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        productService.deleteProduct(1L, 1L);

        // Assert
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).delete(testProduct);
    }

    /**
     * Test 8: Delete product - unauthorized user should fail
     */
    @Test
    void deleteProduct_ShouldThrowWhenUserIsNotOwner() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert - Different user ID (2L) trying to delete product owned by user 1L
        assertThrows(UnauthorizedOperationException.class, 
            () -> productService.deleteProduct(1L, 2L));
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).delete(any(Product.class));
    }
}
