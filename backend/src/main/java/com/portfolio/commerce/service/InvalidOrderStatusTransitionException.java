package com.portfolio.commerce.service;

import com.portfolio.commerce.domain.order.OrderStatus;

public class InvalidOrderStatusTransitionException extends RuntimeException {
    public InvalidOrderStatusTransitionException(OrderStatus current, OrderStatus requested) {
        super("Invalid order status transition: " + current + " -> " + requested);
    }
}
