package com.portfolio.commerce.service;

/**
 * Thrown when a product exists but is not active (active=false), so it
 * cannot be added to a cart. Mapped to HTTP 409 by the global exception
 * handler as a business state conflict.
 */
public class ProductNotActiveException extends RuntimeException {

    public ProductNotActiveException(Long id) {
        super("Product is not active: " + id);
    }
}
