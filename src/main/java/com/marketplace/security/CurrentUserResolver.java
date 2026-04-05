package com.marketplace.security;

import com.marketplace.entity.User;
import com.marketplace.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    private final UserRepository userRepository;

    public CurrentUserResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long resolveUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("Missing authentication principal");
        }

        String identifier = extractIdentifier(authentication.getPrincipal());

        // Allow numeric principal style used by tests and custom security contexts.
        try {
            Long parsedId = Long.parseLong(identifier);
            if (userRepository.existsById(parsedId)) {
                return parsedId;
            }
        } catch (NumberFormatException ignored) {
            // Not a numeric principal, fall back to repository lookup.
        }

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new IllegalStateException("Unable to resolve authenticated user: " + identifier));

        return user.getId();
    }

    private String extractIdentifier(Object principal) {
        if (principal instanceof UserDetails details) {
            return details.getUsername();
        }

        if (principal instanceof String value) {
            return value;
        }

        throw new IllegalStateException("Unsupported authentication principal type: " + principal.getClass().getName());
    }
}
