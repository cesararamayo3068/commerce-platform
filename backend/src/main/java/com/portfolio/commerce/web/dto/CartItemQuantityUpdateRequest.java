package com.portfolio.commerce.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for updating the quantity of an item (PUT semantics).
 *
 * The quantity is set to the exact value provided, never interpreted as
 * an increment. Must be strictly positive.
 */
public record CartItemQuantityUpdateRequest(

        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be greater than 0")
        Integer quantity
) {
}
