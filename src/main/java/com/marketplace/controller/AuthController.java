package com.marketplace.controller;

import com.marketplace.dto.auth.AuthResponse;
import com.marketplace.dto.auth.LoginRequest;
import com.marketplace.dto.auth.RegisterRequest;
import com.marketplace.exception.InvalidCredentialsException;
import com.marketplace.security.CustomUserDetailsService;
import com.marketplace.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final CustomUserDetailsService customUserDetailsService;

	public AuthController(AuthService authService, CustomUserDetailsService customUserDetailsService) {
		this.authService = authService;
		this.customUserDetailsService = customUserDetailsService;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
		AuthResponse response = authService.login(request);

		UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.email());
		Authentication authentication = org.springframework.security.authentication.UsernamePasswordAuthenticationToken
				.authenticated(userDetails, null, userDetails.getAuthorities());

		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(authentication);
		SecurityContextHolder.setContext(securityContext);
		httpServletRequest.getSession(true)
				.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/logout")
	public ResponseEntity<AuthResponse> logout(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) {
		new SecurityContextLogoutHandler().logout(request, response, authentication);
		return ResponseEntity.ok(authService.logout());
	}

	@GetMapping("/me")
	public ResponseEntity<AuthResponse> me(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			throw new InvalidCredentialsException("User is not authenticated");
		}

		String identifier = extractIdentifier(authentication);
		return ResponseEntity.ok(authService.me(identifier));
	}

	private String extractIdentifier(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof UserDetails details) {
			return details.getUsername();
		}
		if (principal instanceof User userDetails) {
			return userDetails.getUsername();
		}
		if (principal instanceof String value) {
			return value;
		}
		throw new IllegalStateException("Unsupported authentication principal");
	}
}
