package com.marketplace.service.impl;

import com.marketplace.dto.auth.AuthResponse;
import com.marketplace.dto.auth.LoginRequest;
import com.marketplace.dto.auth.RegisterRequest;
import com.marketplace.entity.Role;
import com.marketplace.entity.User;
import com.marketplace.exception.InvalidCredentialsException;
import com.marketplace.exception.ResourceAlreadyExistsException;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.repository.RoleRepository;
import com.marketplace.repository.UserRepository;
import com.marketplace.service.AuthService;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResourceAlreadyExistsException("User already exists with email: " + email);
        }

        Role assignedRole = roleRepository.findByName(resolveRoleName(request.role()))
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(email);
        user.setUsername(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Collections.singleton(assignedRole));

        User saved = userRepository.save(user);
        return new AuthResponse("Registration successful", saved.getEmail(), extractRoles(saved));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User doesn't exist, Register now!"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        return new AuthResponse("Login successful", user.getEmail(), extractRoles(user));
    }

    @Override
    public AuthResponse logout() {
        return new AuthResponse("Logout successful", null, Collections.emptySet());
    }

    @Override
    public AuthResponse me(String identifier) {
        User user = userRepository.findByEmail(normalizeEmail(identifier))
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        return new AuthResponse("Current user retrieved successfully", user.getEmail(), extractRoles(user));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveRoleName(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return "BUYER";
        }
        return rawRole.trim().toUpperCase(Locale.ROOT);
    }

    private Set<String> extractRoles(User user) {
        return user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    }
}
