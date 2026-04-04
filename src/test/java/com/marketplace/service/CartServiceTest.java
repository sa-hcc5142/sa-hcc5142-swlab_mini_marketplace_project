package com.marketplace.service;

import com.marketplace.dto.cart.CartItemRequest;
import com.marketplace.dto.cart.CartResponse;
import com.marketplace.entity.Cart;
import com.marketplace.entity.CartItem;
import com.marketplace.entity.Product;
import com.marketplace.entity.Role;
import com.marketplace.entity.User;
import com.marketplace.exception.InvalidOperationException;
import com.marketplace.repository.CartItemRepository;
import com.marketplace.repository.CartRepository;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.UserRepository;
import com.marketplace.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User buyer;
    private User seller;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        Role buyerRole = new Role();
        buyerRole.setName("BUYER");

        Role sellerRole = new Role();
        sellerRole.setName("SELLER");

        buyer = new User();
        buyer.setId(1L);
        buyer.setUsername("buyer1");
        buyer.setEmail("buyer1@marketplace.local");
        buyer.setFullName("Buyer One");
        buyer.setActive(true);
        buyer.setRoles(Set.of(buyerRole));

        seller = new User();
        seller.setId(2L);
        seller.setUsername("seller1");
        seller.setEmail("seller1@marketplace.local");
        seller.setFullName("Seller One");
        seller.setActive(true);
        seller.setRoles(Set.of(sellerRole));

        product = new Product();
        product.setId(10L);
        product.setProductName("Keyboard");
        product.setDescription("Mechanical keyboard");
        product.setPrice(100.0);
        product.setStock(10);
        product.setCategory("Electronics");
        product.setSeller(seller);

        cart = new Cart();
        cart.setId(100L);
        cart.setBuyer(buyer);
        cart.setItems(new ArrayList<>());
        cart.setCreatedAt(Instant.now());
        cart.setUpdatedAt(Instant.now());
    }

    @Test
    void getCart_CreatesCartWhenMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(cartRepository.findByBuyer_Id(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.getCart(1L);

        assertEquals(1L, response.getBuyerId());
        assertEquals(0, response.getTotalItems());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItem_NewItem_Success() {
        CartItemRequest request = new CartItemRequest(10L, 2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(cartRepository.findByBuyer_Id(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCart_IdAndProduct_Id(100L, 10L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.addItem(1L, request);

        assertEquals(1, response.getItems().size());
        assertEquals(2, response.getTotalItems());
        assertEquals(200.0, response.getCartTotal());
    }

    @Test
    void addItem_InsufficientStock_ThrowsInvalidOperation() {
        CartItemRequest request = new CartItemRequest(10L, 50);

        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(cartRepository.findByBuyer_Id(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThrows(InvalidOperationException.class, () -> cartService.addItem(1L, request));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void updateItem_ItemNotInBuyerCart_ThrowsInvalidOperation() {
        Cart otherCart = new Cart();
        otherCart.setId(200L);

        CartItem cartItem = new CartItem();
        cartItem.setId(999L);
        cartItem.setCart(otherCart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setUnitPrice(BigDecimal.valueOf(100.0));
        cartItem.calculateSubtotal();

        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(cartRepository.findByBuyer_Id(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(999L)).thenReturn(Optional.of(cartItem));

        assertThrows(InvalidOperationException.class, () -> cartService.updateItem(1L, 999L, 2));
    }

    @Test
    void removeItem_Success() {
        CartItem cartItem = new CartItem();
        cartItem.setId(501L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setUnitPrice(BigDecimal.valueOf(100.0));
        cartItem.calculateSubtotal();
        cart.getItems().add(cartItem);

        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(cartRepository.findByBuyer_Id(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(501L)).thenReturn(Optional.of(cartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.removeItem(1L, 501L);

        assertEquals(0, response.getItems().size());
        verify(cartItemRepository).delete(cartItem);
        verify(cartRepository).save(cart);
    }

    @Test
    void validateCartStock_ReturnsFalse_WhenAnyItemExceedsStock() {
        CartItem cartItem = new CartItem();
        cartItem.setId(601L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(11);
        cartItem.setUnitPrice(BigDecimal.valueOf(100.0));
        cartItem.calculateSubtotal();
        cart.setItems(new ArrayList<>());
        cart.getItems().add(cartItem);

        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(cartRepository.findByBuyer_Id(1L)).thenReturn(Optional.of(cart));

        boolean valid = cartService.validateCartStock(1L);

        assertFalse(valid);
    }
}
