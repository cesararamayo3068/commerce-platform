package com.portfolio.commerce.web.dto;
import com.portfolio.commerce.domain.promotion.DiscountType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
public record PromotionCreateRequest(@NotBlank @Pattern(regexp="[A-Za-z0-9_-]{3,40}") String code,@NotNull DiscountType discountType,@NotNull @DecimalMin("0.01") BigDecimal amount,@NotNull Instant startsAt,@NotNull Instant endsAt){}
