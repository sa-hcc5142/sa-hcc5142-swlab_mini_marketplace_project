package com.marketplace.test.util;

import com.marketplace.dto.auth.LoginRequest;
import com.marketplace.dto.auth.RegisterRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Test utilities for auth controller integration tests.
 * Provides helper methods for common MockMvc operations.
 */
public class AuthTestUtils {

    public static final String AUTH_BASE_URL = "/api/auth";

    /**
     * Build a valid RegisterRequest for testing.
     */
    public static RegisterRequest buildValidRegisterRequest() {
        return new RegisterRequest(
                "John Doe",
                "john@example.com",
                "password123",
                "BUYER"
        );
    }

    /**
     * Build a valid LoginRequest for testing.
     */
    public static LoginRequest buildValidLoginRequest() {
        return new LoginRequest(
                "john@example.com",
                "password123"
        );
    }

    /**
     * Register a user via API and return the response.
     */
    public static MvcResult registerUser(MockMvc mockMvc, String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Test User",
                email,
                password,
                "BUYER"
        );
        return mockMvc.perform(post(AUTH_BASE_URL + "/register")
                .contentType("application/json")
                .content(convertToJson(request)))
                .andReturn();
    }

    /**
     * Login a user via API and return the response.
     */
    public static MvcResult loginUser(MockMvc mockMvc, String email, String password) throws Exception {
        LoginRequest request = new LoginRequest(email, password);
        return mockMvc.perform(post(AUTH_BASE_URL + "/login")
                .contentType("application/json")
                .content(convertToJson(request)))
                .andReturn();
    }

    /**
     * Logout a user via API.
     */
    public static MvcResult logoutUser(MockMvc mockMvc) throws Exception {
        return mockMvc.perform(post(AUTH_BASE_URL + "/logout"))
                .andReturn();
    }

    /**
     * Simple JSON converter for test objects (uses basic toString/field mapping).
     * In production tests, use ObjectMapper for robust serialization.
     */
    public static String convertToJson(Object object) {
        // Simplified version; in real tests, use:
        // new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(object)
        if (object instanceof RegisterRequest request) {
            return String.format(
                    "{\"fullName\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\"}",
                    request.fullName(), request.email(), request.password(), request.role()
            );
        } else if (object instanceof LoginRequest request) {
            return String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\"}",
                    request.email(), request.password()
            );
        }
        return "{}";
    }
}
