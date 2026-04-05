package com.marketplace.controller;

import com.marketplace.entity.Role;
import com.marketplace.entity.User;
import com.marketplace.repository.RoleRepository;
import com.marketplace.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {"/sql/test-cleanup.sql", "/sql/test-seed-roles.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class AdminControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Role adminRole;
    private Role sellerRole;
    private Role buyerRole;
    private User adminUser;
    private User sellerUser;
    private User buyerUser;

    @BeforeEach
    void setUp() {
        // Create roles if they don't exist
        adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ADMIN");
                    return roleRepository.save(role);
                });

        sellerRole = roleRepository.findByName("SELLER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("SELLER");
                    return roleRepository.save(role);
                });

        buyerRole = roleRepository.findByName("BUYER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("BUYER");
                    return roleRepository.save(role);
                });

        // Create test users
        adminUser = new User();
        adminUser.setUsername("admin_test_user");
        adminUser.setFullName("Admin Test User");
        adminUser.setEmail("admin_test@marketplace.local");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setActive(true);
        adminUser.getRoles().add(adminRole);
        adminUser = userRepository.save(adminUser);

        sellerUser = new User();
        sellerUser.setUsername("seller_test_user");
        sellerUser.setFullName("Seller Test User");
        sellerUser.setEmail("seller_test@marketplace.local");
        sellerUser.setPassword(passwordEncoder.encode("password"));
        sellerUser.setActive(true);
        sellerUser.getRoles().add(sellerRole);
        sellerUser = userRepository.save(sellerUser);

        buyerUser = new User();
        buyerUser.setUsername("buyer_test_user");
        buyerUser.setFullName("Buyer Test User");
        buyerUser.setEmail("buyer_test@marketplace.local");
        buyerUser.setPassword(passwordEncoder.encode("password"));
        buyerUser.setActive(true);
        buyerUser.getRoles().add(buyerRole);
        buyerUser = userRepository.save(buyerUser);
    }

    /**
     * Test Case 1: GET /admin/users - Admin Access
     * Verifies that ADMIN role can fetch all users
     */
    @Test
    @WithMockUser(username = "admin_test_user", roles = {"ADMIN"})
    void testGetAllUsers_AdminAccess_Success() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[*].username", hasItems("admin_test_user", "seller_test_user", "buyer_test_user")));
    }

    /**
     * Test Case 2: GET /admin/users/{id} - Admin Access
     * Verifies that ADMIN role can fetch a specific user by ID
     */
    @Test
    @WithMockUser(username = "admin_test_user", roles = {"ADMIN"})
    void testGetUserById_AdminAccess_Success() throws Exception {
        mockMvc.perform(get("/api/admin/users/" + sellerUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", equalTo(sellerUser.getId().intValue())))
                .andExpect(jsonPath("$.data.username", equalTo("seller_test_user")))
                .andExpect(jsonPath("$.data.email", equalTo("seller_test@marketplace.local")));
    }

    /**
     * Test Case 3: PUT /admin/users/{id}/role - Admin Updates Role
     * Verifies that ADMIN role can successfully update a user's role
     */
    @Test
    @WithMockUser(username = "admin_test_user", roles = {"ADMIN"})
    void testUpdateUserRole_AdminAccess_Success() throws Exception {
        String requestBody = "{\"roleName\": \"BUYER\"}";

        mockMvc.perform(put("/api/admin/users/" + sellerUser.getId() + "/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id", equalTo(sellerUser.getId().intValue())))
            .andExpect(jsonPath("$.data.username", equalTo("seller_test_user")))
            .andExpect(jsonPath("$.data.roles[*]", hasItems("BUYER")));
    }

    /**
     * Test Case 4: PUT /admin/users/{id}/deactivate - Admin Deactivates User
     * Verifies that ADMIN role can deactivate a user (non-critical user)
     */
    @Test
    @WithMockUser(username = "admin_test_user", roles = {"ADMIN"})
    void testDeactivateUser_AdminAccess_Success() throws Exception {
        mockMvc.perform(put("/api/admin/users/" + buyerUser.getId() + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", equalTo(buyerUser.getId().intValue())))
                .andExpect(jsonPath("$.data.active", equalTo(false)));
    }

    /**
     * Test Case 5: GET /admin/users - Seller Access Denied (403)
     * Verifies that SELLER role CANNOT access admin endpoints
     */
    @Test
    @WithMockUser(username = "seller_test_user", roles = {"SELLER"})
    void testGetAllUsers_SellerAccess_Forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /**
     * Test Case 6: PUT /admin/users/{id}/role - Buyer Access Denied (403)
     * Verifies that BUYER role CANNOT update user roles
     */
    @Test
    @WithMockUser(username = "buyer_test_user", roles = {"BUYER"})
    void testUpdateUserRole_BuyerAccess_Forbidden() throws Exception {
        String requestBody = "{\"roleName\": \"SELLER\"}";

        mockMvc.perform(put("/api/admin/users/" + sellerUser.getId() + "/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isForbidden());
    }

    /**
     * Test Case 7: GET /admin/users - Unauthenticated Access Denied (403)
     * Verifies that unauthenticated users CANNOT access admin endpoints
     */
    @Test
    void testGetAllUsers_UnauthenticatedAccess_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
