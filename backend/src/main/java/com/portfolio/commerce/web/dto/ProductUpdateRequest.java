package com.portfolio.commerce.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request body for updating a product (PUT semantics).
 *
 * {@code active} is optional: when {@code null}, the current value is kept.
 * The client cannot set id, createdAt or updatedAt.
 */
public record ProductUpdateRequest(

        @NotBlank(message = "name is required")
        @Size(max = 150, message = "name must be at most 150 characters")
        String name,

        String description,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "price must be greater than or equal to 0")
        BigDecimal price,

        Boolean active
) {
}
