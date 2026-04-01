package com.marketplace.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom Business Logic Exception
 * 
 * Used for domain-level business logic violations such as:
 * - Duplicate review submission
 * - Out of stock product
 * - Invalid state transitions
 * - Constraint violations
 */
public class BusinessException extends RuntimeException {

    private HttpStatus httpStatus;

    public BusinessException(String message) {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
