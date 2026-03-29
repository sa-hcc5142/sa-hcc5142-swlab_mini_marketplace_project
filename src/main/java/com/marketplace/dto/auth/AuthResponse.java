package com.marketplace.dto.auth;

import java.util.Set;

public record AuthResponse(String message, String email, Set<String> roles) {
}
