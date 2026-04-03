package com.marketplace.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name should be between 2 and 100 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password should be at least 6 characters")
        String password,

        @NotBlank(message = "Role is required")
        String role
) {
}
