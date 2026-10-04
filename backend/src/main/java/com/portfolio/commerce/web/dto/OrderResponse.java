package com.portfolio.commerce.web.dto;

import com.portfolio.commerce.domain.order.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id, Long userId, Long cartId, OrderStatus status, List<OrderItemResponse> items,
        BigDecimal subtotal, BigDecimal discount, BigDecimal total, String promotionCode,
        Instant createdAt, Instant updatedAt) {}
