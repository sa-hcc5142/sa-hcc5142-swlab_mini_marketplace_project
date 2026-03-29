package com.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Generic API response wrapper for consistent REST API responses.
 * Wraps both successful and error responses across all endpoints.
 *
 * @param <T> The type of data in the response payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        String timestamp
) {

    /**
     * Create a successful response with data.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, getCurrentTimestamp());
    }

    /**
     * Create a successful response without data.
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, getCurrentTimestamp());
    }

    /**
     * Create an error response.
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, getCurrentTimestamp());
    }

    private static String getCurrentTimestamp() {
        return java.time.Instant.now().toString();
    }
}
