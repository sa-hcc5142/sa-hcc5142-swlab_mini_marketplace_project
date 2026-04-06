package com.marketplace.config;

import com.marketplace.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .userDetailsService(customUserDetailsService)
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/logout").permitAll()
                .requestMatchers(
                    "/",
                    "/dashboard",
                    "/login",
                    "/register",
                    "/products/view",
                    "/products/view/**",
                    "/css/**",
                    "/js/**",
                    "/error",
                    "/actuator/health"
                ).permitAll()
                .requestMatchers("/cart/view", "/orders/view").hasRole("BUYER")
                .requestMatchers("/seller/dashboard").hasRole("SELLER")
                .requestMatchers("/admin/dashboard").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/products/*/reviews/**").hasRole("BUYER")
                .requestMatchers(HttpMethod.PUT, "/api/products/*/reviews/**").hasRole("BUYER")
                .requestMatchers(HttpMethod.DELETE, "/api/products/*/reviews/**").hasAnyRole("BUYER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("SELLER")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("SELLER")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("SELLER")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/orders/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/orders/me/**").hasRole("BUYER")
                .requestMatchers("/api/orders/**").hasAnyRole("BUYER", "ADMIN")
                .requestMatchers("/api/cart/**").hasRole("BUYER")
                .anyRequest().authenticated()
                )
                .logout(logout -> logout.logoutUrl("/api/auth/logout").permitAll())
                .httpBasic(basic -> basic.disable())
                .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Unauthorized - Please log in\"}");
                    })
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
