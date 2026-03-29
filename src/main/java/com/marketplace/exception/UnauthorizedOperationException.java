package com.marketplace.exception;

/**
 * Exception thrown when a user attempts an operation without proper authorization.
 * Maps to HTTP 403 Forbidden status.
 */
public class UnauthorizedOperationException extends RuntimeException {
    public UnauthorizedOperationException(String message) {
        super(message);
    }

    public UnauthorizedOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
