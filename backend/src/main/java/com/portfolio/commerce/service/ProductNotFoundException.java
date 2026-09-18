package com.portfolio.commerce.service;

/**
 * Thrown when a product does not exist. Mapped to HTTP 404 by the
 * global exception handler.
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
}
