package com.portfolio.commerce.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for adding a product to a cart.
 *
 * {@code quantity} must be strictly positive. If the product is already in
 * the cart, the existing quantity is incremented by this value.
 */
public record CartItemRequest(

        @NotNull(message = "productId is required")
        Long productId,

        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be greater than 0")
        Integer quantity
) {
}
