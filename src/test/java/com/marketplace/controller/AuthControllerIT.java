package com.marketplace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.dto.auth.LoginRequest;
import com.marketplace.dto.auth.RegisterRequest;
import com.marketplace.entity.Role;
import com.marketplace.entity.User;
import com.marketplace.repository.RoleRepository;
import com.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {"/sql/test-cleanup.sql", "/sql/test-seed-roles.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

    private Role buyerRole;

    @BeforeEach
    void setUp() {
        // Clean up users
        userRepository.deleteAll();

        // Setup buyer role
        buyerRole = roleRepository.findByName("BUYER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("BUYER");
                    return roleRepository.save(role);
                });
    }

    /**
     * Test Case 1: Register User Endpoint - Success
     */
    @Test
    void testRegisterEndpoint_Success() throws Exception {
                RegisterRequest request = new RegisterRequest("New User", "new@marketplace.local", "password123", "BUYER");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    /**
     * Test Case 2: Login Endpoint - Valid Credentials
     */
    @Test
    void testLoginEndpoint_ValidCredentials() throws Exception {
        // Arrange - Create user first
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@marketplace.local");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(buyerRole);
        user.setActive(true);
        user.setFullName("Test User");
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("test@marketplace.local", "password");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    /**
     * Test Case 3: Login Endpoint - Invalid Credentials
     */
    @Test
    void testLoginEndpoint_InvalidCredentials() throws Exception {
        // Arrange - Create user first
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@marketplace.local");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(buyerRole);
        user.setActive(true);
        user.setFullName("Test User");
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("test@marketplace.local", "wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

        /**
         * Test Case 4: Me Endpoint - Authenticated User
         */
        @Test
        void testMeEndpoint_AuthenticatedUser() throws Exception {
                User userEntity = new User();
                userEntity.setUsername("testuser");
                userEntity.setEmail("test@marketplace.local");
                userEntity.setPassword(passwordEncoder.encode("password"));
                userEntity.setRole(buyerRole);
                userEntity.setActive(true);
                userEntity.setFullName("Test User");
                userRepository.save(userEntity);

                mockMvc.perform(get("/api/auth/me")
                                .with(user("test@marketplace.local").roles("BUYER")))
                                .andExpect(status().isOk());
        }
}
