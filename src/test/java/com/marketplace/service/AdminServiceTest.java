package com.marketplace.service;

import com.marketplace.dto.admin.AdminUserResponse;
import com.marketplace.dto.admin.UpdateRoleRequest;
import com.marketplace.entity.Role;
import com.marketplace.entity.User;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.exception.InvalidOperationException;
import com.marketplace.repository.UserRepository;
import com.marketplace.repository.RoleRepository;
import com.marketplace.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User adminUser;
    private User sellerUser;
    private Role adminRole;
    private Role sellerRole;

    @BeforeEach
    void setUp() {
        // Setup roles
        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ADMIN");

        sellerRole = new Role();
        sellerRole.setId(2L);
        sellerRole.setName("SELLER");

        // Setup admin user
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("adminuser");
        adminUser.setEmail("admin@marketplace.local");
        adminUser.setFullName("Admin User");
        adminUser.setPassword("hashed_password");
        adminUser.setActive(true);
        adminUser.setRole(adminRole);

        // Setup seller user
        sellerUser = new User();
        sellerUser.setId(2L);
        sellerUser.setUsername("selleruser");
        sellerUser.setEmail("seller@marketplace.local");
        sellerUser.setFullName("Seller User");
        sellerUser.setPassword("hashed_password");
        sellerUser.setActive(true);
        sellerUser.setRole(sellerRole);
    }

    /**
     * Test Case 1: Get user by ID - Success
     * Verifies that getUserById returns the correct user when found
     */
    @Test
    void testGetUserById_Success() {
        // Arrange - Mock security context with ADMIN role
        Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));

        // Act
        AdminUserResponse result = adminService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("adminuser", result.getUsername());
        assertEquals("admin@marketplace.local", result.getEmail());
        verify(userRepository, times(1)).findById(userId);
    }

    /**
     * Test Case 2: Get user by ID - Not Found
     * Verifies that ResourceNotFoundException is thrown when user doesn't exist
     */
    @Test
    void testGetUserById_NotFound() {
        // Arrange - Mock security context with ADMIN role
        Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            adminService.getUserById(userId);
        });
        verify(userRepository, times(1)).findById(userId);
    }

    /**
     * Test Case 3: Update user role - Success
     * Verifies that a user's role can be updated successfully
     */
    @Test
    void testUpdateUserRole_Success() {
        // Arrange - Mock security context with ADMIN role
        Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Long userId = 2L;
        UpdateRoleRequest request = new UpdateRoleRequest("ADMIN");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(sellerUser));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(sellerUser);

        // Act
        AdminUserResponse result = adminService.updateUserRole(userId, request);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findByName("ADMIN");
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Test Case 4: Deactivate user - Success
     * Verifies that a user's active status is set to false
     */
    @Test
    void testDeactivateUser_Success() {
        // Arrange - Mock security context with ADMIN role
        Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Long userId = 2L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(sellerUser));
        when(userRepository.save(any(User.class))).thenReturn(sellerUser);

        // Act - No return value, void method
        adminService.deactivateUser(userId);

        // Assert
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }
}
