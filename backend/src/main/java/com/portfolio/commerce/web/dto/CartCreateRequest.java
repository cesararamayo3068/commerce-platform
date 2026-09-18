package com.portfolio.commerce.web.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for creating a cart.
 *
 * The client only provides the owner user id; the cart is created with
 * status ACTIVE and no items.
 */
public record CartCreateRequest(

        @NotNull(message = "userId is required")
        Long userId
) {
}
