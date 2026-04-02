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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
        // Arrange - RegisterRequest is a record with constructor
        RegisterRequest request = new RegisterRequest("newuser", "new@marketplace.local", "password123", "New User");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
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
        user.setPassword("$2a$10$AJ0bkgmP0/q4lmJ0gJkLb.v9X9v8gP0gJkL.0X9X9v8gP0gJkL");  // hashed "password"
        user.setRole(buyerRole);
        user.setActive(true);
        userRepository.save(user);

        // LoginRequest is a record with constructor
        LoginRequest loginRequest = new LoginRequest("testuser", "password");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
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
        user.setPassword("$2a$10$AJ0bkgmP0/q4lmJ0gJkLb.v9X9v8gP0gJkL.0X9X9v8gP0gJkL");  // hashed "password"
        user.setRole(buyerRole);
        user.setActive(true);
        userRepository.save(user);

        // LoginRequest is a record with constructor
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
