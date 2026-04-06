package com.marketplace.service.impl;

import com.marketplace.dto.admin.AdminUserResponse;
import com.marketplace.dto.admin.UpdateRoleRequest;
import com.marketplace.dto.admin.AdminUserRequest;
import com.marketplace.dto.admin.AdminUpdateUserRequest;
import com.marketplace.entity.Role;
import com.marketplace.entity.User;
import com.marketplace.exception.InvalidOperationException;
import com.marketplace.exception.ResourceNotFoundException;
import com.marketplace.repository.RoleRepository;
import com.marketplace.repository.UserRepository;
import com.marketplace.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin Service Implementation
 * 
 * Provides administrative operations with authorization checks
 * and business logic constraints for user management
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all users with pagination (ADMIN only)
     */
    @Override
    public Page<AdminUserResponse> getAllUsers(Pageable pageable) {
        verifyAdminRole();
        return userRepository.findAll(pageable).map(this::mapToAdminResponse);
    }

    /**
     * Get single user by ID (ADMIN only)
     */
    @Override
    public AdminUserResponse getUserById(Long userId) {
        verifyAdminRole();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("User", userId));
        return mapToAdminResponse(user);
    }

    /**
     * Update user's role with constraint validation
     * - Cannot demote last ADMIN
     * - Role must exist in database
     * - Caller must be ADMIN
     */
    @Override
    public AdminUserResponse updateUserRole(Long userId, UpdateRoleRequest request) {
        verifyAdminRole();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("User", userId));

        Role newRole = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> ResourceNotFoundException.notFound("Role", request.getRoleName()));

        // Check if this is last admin being demoted
        if (!(newRole.getName().equals("ADMIN") || newRole.getName().equals("ROLE_ADMIN")) && isLastAdminBeingDemoted(user, newRole)) {
            throw new InvalidOperationException("Cannot demote the last ADMIN user");
        }

        user.setRole(newRole);
        userRepository.save(user);

        return mapToAdminResponse(user);
    }

    /**
     * Deactivate user (soft delete)
     * - Prevents self-deactivation
     * - Prevents deactivating only admin
     * - Preserves user data for audit trail
     */
    @Override
    public void deactivateUser(Long userId) {
        verifyAdminRole();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("User", userId));

        // Prevent self-deactivation
        String currentUsername = getCurrentUsername();
        if (user.getUsername().equals(currentUsername)) {
            throw new InvalidOperationException("Cannot deactivate your own account");
        }

        // Prevent deactivating only admin
        if ((user.getRole().getName().equals("ADMIN") || user.getRole().getName().equals("ROLE_ADMIN")) && isOnlyAdmin(user)) {
            throw new InvalidOperationException("Cannot deactivate the only ADMIN user");
        }

        user.setActive(false);
        userRepository.save(user);
    }

    /**
     * Create a new user (Admin version with random password)
     */
    @Override
    public AdminUserResponse createUser(AdminUserRequest request) {
        verifyAdminRole();

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new InvalidOperationException("User already exists with email: " + request.email());
        }

        Role assignedRole = roleRepository.findByName(request.role())
                .orElseThrow(() -> ResourceNotFoundException.notFound("Role", request.role()));

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setUsername(request.email());
        // Assign a random password as Admin cannot set one
        user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        user.setRoles(java.util.Collections.singleton(assignedRole));
        user.setActive(true);

        User saved = userRepository.save(user);
        return mapToAdminResponse(saved);
    }

    /**
     * Update user details (Admin version)
     */
    @Override
    public AdminUserResponse updateUser(Long userId, AdminUpdateUserRequest request) {
        verifyAdminRole();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("User", userId));

        Role newRole = roleRepository.findByName(request.role())
                .orElseThrow(() -> ResourceNotFoundException.notFound("Role", request.role()));

        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setUsername(request.email());
        user.setRole(newRole);
        if (request.active() != null) {
            user.setActive(request.active());
        }

        User updated = userRepository.save(user);
        return mapToAdminResponse(updated);
    }

    /**
     * Permanently delete user
     */
    @Override
    public void deleteUser(Long userId) {
        verifyAdminRole();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.notFound("User", userId));

        // Prevent self-deletion
        if (user.getUsername().equals(getCurrentUsername())) {
            throw new InvalidOperationException("Cannot delete your own account");
        }

        userRepository.delete(user);
    }

    // ===== Helper Methods =====

    /**
     * Verify caller has ADMIN role
     * Throws AccessDeniedException if not ADMIN
     */
    private void verifyAdminRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Admin role required for this operation");
        }
    }

    /**
     * Get username from security context
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    /**
     * Check if user would be last admin if demoted
     */
    private boolean isLastAdminBeingDemoted(User user, Role newRole) {
        if (newRole.getName().equals("ADMIN") || newRole.getName().equals("ROLE_ADMIN")) {
            return false; // Not being demoted
        }

        // Check if this user is currently ADMIN
        if (!(user.getRole().getName().equals("ADMIN") || user.getRole().getName().equals("ROLE_ADMIN"))) {
            return false; // User isn't admin, can't demote non-admin
        }

        return isOnlyAdmin(user);
    }

    /**
     * Check if user is the only active ADMIN
     */
    private boolean isOnlyAdmin(User user) {
        long adminCount = userRepository.findAll().stream()
                .filter(u -> u.isActive())
                .filter(u -> u.getRole().getName().equals("ADMIN") || u.getRole().getName().equals("ROLE_ADMIN"))
                .count();

        return adminCount == 1;
    }

    /**
     * Map User entity to AdminUserResponse DTO
     */
    private AdminUserResponse mapToAdminResponse(User user) {
        AdminUserResponse response = new AdminUserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setActive(user.isActive());
        response.setRoles(java.util.Collections.singleton(user.getRole().getName()));
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
