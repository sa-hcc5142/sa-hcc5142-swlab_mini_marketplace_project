package com.marketplace.controller;

import com.marketplace.dto.ApiResponse;
import com.marketplace.dto.admin.AdminUserResponse;
import com.marketplace.dto.admin.UpdateRoleRequest;
import com.marketplace.dto.admin.AdminUserRequest;
import com.marketplace.dto.admin.AdminUpdateUserRequest;
import com.marketplace.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final AdminService adminService;

	public AdminController(AdminService adminService) {
		this.adminService = adminService;
	}

	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getAllUsers(Pageable pageable) {
		Page<AdminUserResponse> users = adminService.getAllUsers(pageable);
		return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
	}

	@GetMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<AdminUserResponse>> getUserById(@PathVariable Long id) {
		AdminUserResponse user = adminService.getUserById(id);
		return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
	}

	@PutMapping("/users/{id}/role")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserRole(
			@PathVariable Long id,
			@Valid @RequestBody UpdateRoleRequest request) {
		AdminUserResponse user = adminService.updateUserRole(id, request);
		return ResponseEntity.ok(ApiResponse.success("User role updated successfully", user));
	}

	@PutMapping("/users/{id}/deactivate")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<AdminUserResponse>> deactivateUser(@PathVariable Long id) {
		adminService.deactivateUser(id);
		AdminUserResponse updatedUser = adminService.getUserById(id);
		return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", updatedUser));
	}

	@PostMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<AdminUserResponse>> createUser(@Valid @RequestBody AdminUserRequest request) {
		AdminUserResponse user = adminService.createUser(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("User created successfully", user));
	}

	@PutMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<AdminUserResponse>> updateUser(
			@PathVariable Long id,
			@Valid @RequestBody AdminUpdateUserRequest request) {
		AdminUserResponse user = adminService.updateUser(id, request);
		return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
	}

	@DeleteMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
		adminService.deleteUser(id);
		return ResponseEntity.ok(ApiResponse.success("User deleted permanently"));
	}
}
