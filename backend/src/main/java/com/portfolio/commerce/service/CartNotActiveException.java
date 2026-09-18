package com.portfolio.commerce.service;

/**
 * Thrown when an operation tries to modify a cart that is not ACTIVE
 * (e.g. CANCELLED or CHECKED_OUT). Mapped to HTTP 409 by the global
 * exception handler.
 */
public class CartNotActiveException extends RuntimeException {

    public CartNotActiveException(Long id) {
        super("Cart is not active: " + id);
    }
}
