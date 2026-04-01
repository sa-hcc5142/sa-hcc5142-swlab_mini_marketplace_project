package com.marketplace.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update Role Request DTO
 * 
 * Used for updating a user's role
 * Validates that a role name is provided
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {

    @NotBlank(message = "Role is required")
    private String roleName;
}
