package com.portfolio.commerce.service;
import com.portfolio.commerce.domain.promotion.*;
import com.portfolio.commerce.web.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.*;
import java.time.Instant;
import java.util.*;
@Service
public class PromotionService {
 private final PromotionRepository repository;
 public PromotionService(PromotionRepository repository){this.repository=repository;}
 @Transactional public PromotionResponse create(PromotionCreateRequest request){
  String code=request.code().trim().toUpperCase(Locale.ROOT);
  if(!request.endsAt().isAfter(request.startsAt())||(request.discountType()==DiscountType.PERCENT&&request.amount().compareTo(new BigDecimal("100"))>0))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid promotion amount or dates");
  if(repository.findByCode(code).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Coupon already exists");
  return PromotionResponse.from(repository.save(new Promotion(code,request.discountType(),request.amount(),request.startsAt(),request.endsAt())));
 }
 @Transactional(readOnly=true) public List<PromotionResponse> list(){return repository.findAll().stream().map(PromotionResponse::from).toList();}
 @Transactional public PromotionResponse deactivate(Long id){Promotion p=repository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Promotion not found"));p.setActive(false);return PromotionResponse.from(p);}
 @Transactional(readOnly=true) public Promotion validCoupon(String raw){
  if(raw==null||raw.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Coupon required");
  Promotion p=repository.findByCode(raw.trim().toUpperCase(Locale.ROOT)).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid coupon"));
  if(!p.isValid(Instant.now()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Coupon expired or inactive");
  return p;
 }
 public BigDecimal discount(Promotion p,BigDecimal subtotal){
  if(p==null||!p.isValid(Instant.now()))return BigDecimal.ZERO;
  BigDecimal d=p.getDiscountType()==DiscountType.PERCENT?subtotal.multiply(p.getAmount()).divide(new BigDecimal("100"),2,RoundingMode.HALF_UP):p.getAmount();
  return d.min(subtotal).max(BigDecimal.ZERO);
 }
}
