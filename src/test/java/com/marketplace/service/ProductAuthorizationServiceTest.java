package com.marketplace.service;

import com.marketplace.entity.Product;
import com.marketplace.entity.User;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.UnauthorizedOperationException;
import com.marketplace.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductAuthorizationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductAuthorizationService authorizationService;

    private Product testProduct;
    private User testOwner;
    private User testOtherUser;

    @BeforeEach
    void setUp() {
        // Setup product owner
        testOwner = new User();
        testOwner.setId(1L);
        testOwner.setFullName("Product Owner");
        testOwner.setEmail("owner@test.com");

        // Setup other user (non-owner)
        testOtherUser = new User();
        testOtherUser.setId(2L);
        testOtherUser.setFullName("Other User");
        testOtherUser.setEmail("other@test.com");

        // Setup test product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setProductName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(99.99);
        testProduct.setStock(10);
        testProduct.setCategory("Electronics");
        testProduct.setSeller(testOwner);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Test 1: verifyProductOwner should succeed when user is the owner
     */
    @Test
    void verifyProductOwner_ShouldSucceedForOwner() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> authorizationService.verifyProductOwner(1L, 1L));
        // Called twice: once in verifyProductOwner, once in isProductOwner
        verify(productRepository, times(2)).findById(1L);
    }

    /**
     * Test 2: verifyProductOwner should throw when user is not the owner
     */
    @Test
    void verifyProductOwner_ShouldThrowForNonOwner() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert - Should throw UnauthorizedOperationException
        assertThrows(UnauthorizedOperationException.class, 
            () -> authorizationService.verifyProductOwner(1L, 2L));
        // Called twice: once in verifyProductOwner, once in isProductOwner
        verify(productRepository, times(2)).findById(1L);
    }

    /**
     * Test 3: verifyProductOwner should throw when product doesn't exist
     */
    @Test
    void verifyProductOwner_ShouldThrowWhenProductNotFound() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert - Should throw ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, 
            () -> authorizationService.verifyProductOwner(999L, 1L));
        verify(productRepository, times(1)).findById(999L);
    }

    /**
     * Test 4: isProductOwner should return true when user is the owner
     */
    @Test
    void isProductOwner_ShouldReturnTrueForOwner() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        boolean result = authorizationService.isProductOwner(1L, 1L);

        // Assert
        assertTrue(result);
        verify(productRepository, times(1)).findById(1L);
    }

    /**
     * Test 5: isProductOwner should return false when user is not the owner
     */
    @Test
    void isProductOwner_ShouldReturnFalseForNonOwner() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        boolean result = authorizationService.isProductOwner(1L, 2L);

        // Assert
        assertFalse(result);
        verify(productRepository, times(1)).findById(1L);
    }

    /**
     * Test 6: isProductOwner should return false when product doesn't exist
     */
    @Test
    void isProductOwner_ShouldReturnFalseWhenProductNotFound() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        boolean result = authorizationService.isProductOwner(999L, 1L);

        // Assert
        assertFalse(result);
        verify(productRepository, times(1)).findById(999L);
    }

    /**
     * Test 7: getProductOwnerId should return owner ID when product exists
     */
    @Test
    void getProductOwnerId_ShouldReturnOwnerIdWhenProductExists() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        Long ownerId = authorizationService.getProductOwnerId(1L);

        // Assert
        assertEquals(1L, ownerId);
        verify(productRepository, times(1)).findById(1L);
    }

    /**
     * Test 8: getProductOwnerId should throw when product doesn't exist
     */
    @Test
    void getProductOwnerId_ShouldThrowWhenProductNotFound() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> authorizationService.getProductOwnerId(999L));
        verify(productRepository, times(1)).findById(999L);
    }

    /**
     * Test 9: Authorization error message is clear and informative
     */
    @Test
    void verifyProductOwner_ShouldProvideDetailedErrorMessage() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        UnauthorizedOperationException exception = assertThrows(UnauthorizedOperationException.class, 
            () -> authorizationService.verifyProductOwner(1L, 2L));
        
        assertTrue(exception.getMessage().contains("not authorized"));
        assertTrue(exception.getMessage().contains("seller can edit or delete"));
        // Called twice: once in verifyProductOwner, once in isProductOwner
        verify(productRepository, times(2)).findById(1L);
    }
}
