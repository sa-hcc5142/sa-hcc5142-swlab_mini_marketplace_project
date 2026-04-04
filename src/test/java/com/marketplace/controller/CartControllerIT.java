package com.marketplace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.dto.cart.CartItemRequest;
import com.marketplace.entity.Cart;
import com.marketplace.entity.CartItem;
import com.marketplace.entity.Product;
import com.marketplace.entity.Role;
import com.marketplace.entity.User;
import com.marketplace.repository.CartItemRepository;
import com.marketplace.repository.CartRepository;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.RoleRepository;
import com.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {"/sql/test-cleanup.sql", "/sql/test-seed-roles.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class CartControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private User buyer;
    private User seller;
    private Product product;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        Role buyerRole = roleRepository.findByName("BUYER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("BUYER");
                    return roleRepository.save(role);
                });

        Role sellerRole = roleRepository.findByName("SELLER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("SELLER");
                    return roleRepository.save(role);
                });

        buyer = new User();
        buyer.setUsername("buyer");
        buyer.setFullName("Buyer User");
        buyer.setEmail("buyer@marketplace.local");
        buyer.setPassword("hashed");
        buyer.setRole(buyerRole);
        buyer.setActive(true);
        buyer = userRepository.save(buyer);

        seller = new User();
        seller.setUsername("seller");
        seller.setFullName("Seller User");
        seller.setEmail("seller@marketplace.local");
        seller.setPassword("hashed");
        seller.setRole(sellerRole);
        seller.setActive(true);
        seller = userRepository.save(seller);

        product = new Product();
        product.setProductName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(99.99);
        product.setStock(10);
        product.setCategory("Test");
        product.setSeller(seller);
        product = productRepository.save(product);

        cart = new Cart();
        cart.setBuyer(buyer);
        cart.setCreatedAt(Instant.now());
        cart.setUpdatedAt(Instant.now());
        cart = cartRepository.save(cart);

        cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setUnitPrice(BigDecimal.valueOf(product.getPrice()));
        cartItem.calculateSubtotal();
        cartItem.setAddedAt(Instant.now());
        cartItem = cartItemRepository.save(cartItem);
        cart.getItems().add(cartItem);
        cartRepository.save(cart);
    }

    @Test
    void testGetCart_Success() throws Exception {
        mockMvc.perform(get("/cart/me")
                .with(user(String.valueOf(buyer.getId())).roles("BUYER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.totalItems").value(1));
    }

    @Test
    void testAddItemToCart_Success() throws Exception {
        CartItemRequest request = new CartItemRequest(product.getId(), 2);

        mockMvc.perform(post("/cart/me/items")
                        .with(user(String.valueOf(buyer.getId())).roles("BUYER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.totalItems").value(3));
    }

    @Test
    void testClearCart_Success() throws Exception {
        mockMvc.perform(delete("/cart/me/items")
                        .with(user(String.valueOf(buyer.getId())).roles("BUYER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetCart_SellerForbidden() throws Exception {
        mockMvc.perform(get("/cart/me")
                        .with(user(String.valueOf(seller.getId())).roles("SELLER")))
                .andExpect(status().isForbidden());
    }
}
