package com.marketplace.exception;

/**
 * Exception thrown when an invalid operation is attempted.
 * Examples: insufficient stock, invalid state transitions, etc.
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
