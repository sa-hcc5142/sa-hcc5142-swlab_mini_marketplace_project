package com.marketplace.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Maps to HTTP 404 Not Found status.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ResourceNotFoundException notFound(String resource, Object identifier) {
        return new ResourceNotFoundException(resource + " with identifier " + identifier + " not found");
    }
}
