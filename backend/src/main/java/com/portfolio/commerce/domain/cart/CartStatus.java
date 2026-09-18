package com.portfolio.commerce.domain.cart;

/**
 * Lifecycle state of a {@link Cart}.
 *
 * Persisted as STRING (never ordinal) so the stored value is stable and
 * readable. Promotions are NOT part of the cart state: they will be
 * implemented later with the Strategy pattern.
 */
public enum CartStatus {
    ACTIVE,
    CHECKED_OUT,
    CANCELLED
}
