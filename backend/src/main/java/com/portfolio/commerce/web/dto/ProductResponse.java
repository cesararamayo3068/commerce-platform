package com.portfolio.commerce.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id, String name, String description, String brand, String category, String imageUrl,
        BigDecimal price, int stock, boolean active, Instant createdAt, Instant updatedAt) {}
