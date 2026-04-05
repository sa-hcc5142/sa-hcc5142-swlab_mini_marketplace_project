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
                .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/logout").permitAll()
                .requestMatchers(
                        "/",
                    "/dashboard",
                    "/login",
                    "/register",
                    "/products/view",
                    "/products/view/**",
                        "/css/**",
                        "/js/**",
                        "/error"
                ).permitAll()
                .requestMatchers("/cart/view", "/orders/view").hasAnyRole("BUYER", "ADMIN")
                .requestMatchers("/seller/dashboard").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers("/admin/dashboard").hasRole("ADMIN")
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/products/*/reviews/**").hasAnyRole("BUYER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/products/*/reviews/**").hasAnyRole("BUYER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/products/*/reviews/**").hasAnyRole("BUYER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/products/**").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/products/**").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/products/**").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/orders/**").hasAnyRole("BUYER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout.logoutUrl("/auth/logout").permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
