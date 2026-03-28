package com.marketplace.dto.auth;

public record RegisterRequest(String fullName, String email, String password, String role) {
}
