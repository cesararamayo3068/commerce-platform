package com.portfolio.commerce.service;

/**
 * Thrown when a product is not present in a cart. Mapped to HTTP 404 by
 * the global exception handler.
 */
public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(Long cartId, Long productId) {
        super("Item not found in cart " + cartId + " for product " + productId);
    }
}
