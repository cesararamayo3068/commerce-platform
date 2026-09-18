package com.portfolio.commerce.service;

/**
 * Thrown when a user does not exist. Mapped to HTTP 404 by the
 * global exception handler.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
}
