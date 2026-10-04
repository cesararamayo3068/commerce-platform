package com.portfolio.commerce.service;

public class EmptyCartException extends RuntimeException {
    public EmptyCartException(Long cartId) { super("Cart " + cartId + " is empty"); }
}
