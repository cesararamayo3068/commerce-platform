package com.portfolio.commerce.service;

public class OrderAlreadyExistsException extends RuntimeException {
    public OrderAlreadyExistsException(Long cartId) { super("Cart " + cartId + " was already checked out"); }
}
