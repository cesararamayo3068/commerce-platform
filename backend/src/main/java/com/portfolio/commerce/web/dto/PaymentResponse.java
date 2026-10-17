package com.portfolio.commerce.web.dto;
import com.portfolio.commerce.domain.payment.*;
import java.math.BigDecimal;
import java.time.Instant;
public record PaymentResponse(Long id,Long orderId,PaymentStatus status,BigDecimal amount,Instant createdAt,Instant updatedAt){
 public static PaymentResponse from(Payment p){return new PaymentResponse(p.getId(),p.getOrder().getId(),p.getStatus(),p.getAmount(),p.getCreatedAt(),p.getUpdatedAt());}
}
