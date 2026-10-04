package com.portfolio.commerce.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotBlank(message = "name is required") @Size(max = 150) String name,
        String description,
        @Size(max = 100) String brand,
        @Size(max = 100) String category,
        @Size(max = 500) String imageUrl,
        @NotNull(message = "price is required") @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
        @NotNull(message = "stock is required") @Min(value = 0, message = "stock must be greater than or equal to 0") Integer stock) {}
