package com.marketplace.service;

import com.marketplace.dto.admin.AdminUserResponse;
import com.marketplace.dto.admin.UpdateRoleRequest;
import com.marketplace.dto.admin.AdminUserRequest;
import com.marketplace.dto.admin.AdminUpdateUserRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Admin Service Interface
 * 
 * Defines administrative operations:
 * - User management (list, get, role updates)
 * - User deactivation
 * 
 * All operations require ADMIN role
 */
public interface AdminService {

    /**
     * Get all users with pagination
     * 
     * @param pageable pagination parameters (page, size, sort)
     * @return Page of AdminUserResponse containing user details
     */
    Page<AdminUserResponse> getAllUsers(Pageable pageable);

    /**
     * Get single user by ID
     * 
     * @param userId the user ID to retrieve
     * @return AdminUserResponse with user details
     */
    AdminUserResponse getUserById(Long userId);

    /**
     * Update user's role
     * 
     * @param userId the user to update
     * @param request UpdateRoleRequest with new role
     * @return updated AdminUserResponse
     */
    AdminUserResponse updateUserRole(Long userId, UpdateRoleRequest request);

    /**
     * Deactivate user (soft delete)
     * Sets user's active flag to false without deleting data
     * 
     * @param userId the user to deactivate
     */
    void deactivateUser(Long userId);

    /**
     * Create a new user (Admin version)
     * 
     * @param request AdminUserRequest with user details
     * @return AdminUserResponse
     */
    AdminUserResponse createUser(AdminUserRequest request);

    /**
     * Update user details (Admin version)
     * 
     * @param userId the user ID to update
     * @param request AdminUpdateUserRequest with user details
     * @return updated AdminUserResponse
     */
    AdminUserResponse updateUser(Long userId, AdminUpdateUserRequest request);

    /**
     * Permanently delete user
     * 
     * @param userId the user to delete
     */
    void deleteUser(Long userId);
}
