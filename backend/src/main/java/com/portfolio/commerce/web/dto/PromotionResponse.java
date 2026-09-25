package com.portfolio.commerce.web.dto;
import com.portfolio.commerce.domain.promotion.*;
import java.math.BigDecimal;
import java.time.Instant;
public record PromotionResponse(Long id,String code,DiscountType discountType,BigDecimal amount,Instant startsAt,Instant endsAt,boolean active){
 public static PromotionResponse from(Promotion p){return new PromotionResponse(p.getId(),p.getCode(),p.getDiscountType(),p.getAmount(),p.getStartsAt(),p.getEndsAt(),p.isActive());}
}
