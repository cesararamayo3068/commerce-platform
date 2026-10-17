package com.portfolio.commerce.web.dto;

import com.portfolio.commerce.domain.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(@NotNull OrderStatus status) {}
