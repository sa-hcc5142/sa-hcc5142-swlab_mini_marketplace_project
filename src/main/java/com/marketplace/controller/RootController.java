package com.marketplace.controller;

import com.marketplace.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Root controller for the Mini Marketplace API
 * Provides welcome endpoint at root path
 */
@RestController
public class RootController {

    @GetMapping("/api-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> welcome() {
        Map<String, Object> data = new HashMap<>();
        data.put("application", "Mini Marketplace");
        data.put("version", "0.0.1");
        data.put("status", "OPERATIONAL");
        data.put("timestamp", LocalDateTime.now());
        data.put("apiBaseUrl", "/api");
        data.put("endpoints", new HashMap<String, String>() {{
            put("health", "/api/actuator/health");
            put("auth.register", "POST /api/auth/register");
            put("auth.login", "POST /api/auth/login");
            put("auth.logout", "POST /api/auth/logout");
            put("products.list", "GET /api/products");
            put("products.create", "POST /api/products");
            put("orders.list", "GET /api/orders");
            put("cart.view", "GET /api/cart");
            put("reviews.list", "GET /api/products/{productId}/reviews");
        }});
        
        return ResponseEntity.ok(
            ApiResponse.success("Mini Marketplace API - Welcome!", data)
        );
    }
}
