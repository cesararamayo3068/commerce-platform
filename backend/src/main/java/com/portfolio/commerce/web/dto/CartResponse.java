package com.portfolio.commerce.web.dto;

import com.portfolio.commerce.domain.cart.CartStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * API representation of a cart. Never exposes the JPA entity directly.
 *
 * {@code total} is the sum of the item subtotals; no discounts are applied
 * at this stage. The total is computed on the fly and never persisted.
 */
public record CartResponse(
        Long id,
        Long userId,
        CartStatus status,
        List<CartItemResponse> items,
        BigDecimal total,
        Instant createdAt,
        Instant updatedAt
) {
}
