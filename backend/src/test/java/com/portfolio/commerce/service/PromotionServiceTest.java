package com.portfolio.commerce.service;
import com.portfolio.commerce.domain.promotion.*;
import com.portfolio.commerce.web.dto.PromotionCreateRequest;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class PromotionServiceTest {
 private final PromotionRepository repository=mock(PromotionRepository.class);
 private final PromotionService service=new PromotionService(repository);
 @Test void percentDiscountRoundsToCents(){
  Promotion p=new Promotion("SAVE15",DiscountType.PERCENT,new BigDecimal("15"),Instant.now().minusSeconds(60),Instant.now().plusSeconds(3600));
  assertThat(service.discount(p,new BigDecimal("19.99"))).isEqualByComparingTo("3.00");
 }
 @Test void fixedDiscountNeverExceedsSubtotal(){
  Promotion p=new Promotion("SAVE50",DiscountType.FIXED,new BigDecimal("50"),Instant.now().minusSeconds(60),Instant.now().plusSeconds(3600));
  assertThat(service.discount(p,new BigDecimal("12.00"))).isEqualByComparingTo("12.00");
 }
 @Test void expiredCouponDoesNotDiscount(){
  Promotion p=new Promotion("OLD",DiscountType.PERCENT,new BigDecimal("20"),Instant.now().minusSeconds(7200),Instant.now().minusSeconds(3600));
  assertThat(service.discount(p,new BigDecimal("100.00"))).isEqualByComparingTo("0");
 }
 @Test void percentageOverHundredRejected(){
  assertThatThrownBy(()->service.create(new PromotionCreateRequest("BIG",DiscountType.PERCENT,new BigDecimal("101"),Instant.now(),Instant.now().plusSeconds(3600)))).hasMessageContaining("Invalid promotion");
 }
}
