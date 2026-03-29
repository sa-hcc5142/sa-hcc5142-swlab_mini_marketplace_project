package com.marketplace.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.marketplace.dto.auth.AuthResponse;
import com.marketplace.dto.auth.LoginRequest;
import com.marketplace.dto.auth.RegisterRequest;
import com.marketplace.entity.Role;
import com.marketplace.entity.User;
import com.marketplace.exception.InvalidCredentialsException;
import com.marketplace.exception.ResourceAlreadyExistsException;
import com.marketplace.repository.RoleRepository;
import com.marketplace.repository.UserRepository;
import com.marketplace.service.impl.AuthServiceImpl;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    void registerShouldCreateUserSuccessfully() {
        RegisterRequest request = new RegisterRequest("Buyer One", "buyer@example.com", "plain-pass", "BUYER");

        Role buyerRole = new Role();
        buyerRole.setName("BUYER");

        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("BUYER")).thenReturn(Optional.of(buyerRole));
        when(passwordEncoder.encode("plain-pass")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.register(request);

        assertEquals("Registration successful", response.message());
        assertEquals("buyer@example.com", response.email());
        assertTrue(response.roles().contains("BUYER"));
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Buyer One", "buyer@example.com", "plain-pass", "BUYER");

        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(ResourceAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void loginShouldSucceedWithValidCredentials() {
        LoginRequest request = new LoginRequest("buyer@example.com", "plain-pass");

        Role buyerRole = new Role();
        buyerRole.setName("BUYER");

        User user = new User();
        user.setEmail("buyer@example.com");
        user.setPassword("encoded-pass");
        user.setRoles(Set.of(buyerRole));

        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain-pass", "encoded-pass")).thenReturn(true);

        AuthResponse response = authService.login(request);

        assertEquals("Login successful", response.message());
        assertEquals("buyer@example.com", response.email());
    }

    @Test
    void loginShouldFailWithInvalidPassword() {
        LoginRequest request = new LoginRequest("buyer@example.com", "wrong-pass");

        User user = new User();
        user.setEmail("buyer@example.com");
        user.setPassword("encoded-pass");

        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-pass", "encoded-pass")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}
