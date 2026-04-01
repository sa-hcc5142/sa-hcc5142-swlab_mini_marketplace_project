package com.marketplace.exception;

/**
 * Entity Not Found Exception
 * 
 * Thrown when a requested entity (User, Product, Order, Review, etc.) 
 * is not found in the database
 * Results in HTTP 404 Not Found response
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Factory method for consistent error messages
     * 
     * @param entityType the type of entity (e.g., "User", "Product", "Order")
     * @param id the entity ID that was not found
     * @return EntityNotFoundException with formatted message
     */
    public static EntityNotFoundException notFound(String entityType, Long id) {
        return new EntityNotFoundException(entityType + " not found with id: " + id);
    }

    /**
     * Factory method for consistent error messages with identifier
     * 
     * @param entityType the type of entity (e.g., "User", "Product", "Order")
     * @param identifier the entity identifier (e.g., username, email)
     * @return EntityNotFoundException with formatted message
     */
    public static EntityNotFoundException notFound(String entityType, String identifier) {
        return new EntityNotFoundException(entityType + " not found: " + identifier);
    }
}
