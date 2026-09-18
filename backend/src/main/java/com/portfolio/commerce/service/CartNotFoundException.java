package com.portfolio.commerce.service;

/**
 * Thrown when a cart does not exist. Mapped to HTTP 404 by the
 * global exception handler.
 */
public class CartNotFoundException extends RuntimeException {

    public CartNotFoundException(Long id) {
        super("Cart not found with id: " + id);
    }
}
