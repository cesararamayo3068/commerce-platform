package com.portfolio.commerce.web.dto;

import java.math.BigDecimal;

/**
 * API representation of a single item inside a cart.
 *
 * {@code unitPrice} comes from the associated {@code Product} (no price
 * snapshot at this stage) and {@code subtotal} is {@code unitPrice * quantity}
 * computed with {@link BigDecimal}.
 */
public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
}
