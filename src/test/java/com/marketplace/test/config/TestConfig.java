package com.marketplace.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test configuration providing reusable beans for integration tests.
 */
@TestConfiguration
public class TestConfig {

    /**
     * Provide ObjectMapper bean for JSON serialization in tests.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
