package com.portfolio.commerce.domain.promotion;
import com.portfolio.commerce.domain.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name="promotions")
public class Promotion extends AuditableEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=40,unique=true) private String code;
 @Enumerated(EnumType.STRING) @Column(name="discount_type",nullable=false,length=12) private DiscountType discountType;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
 @Column(name="starts_at",nullable=false) private Instant startsAt;
 @Column(name="ends_at",nullable=false) private Instant endsAt;
 @Column(nullable=false) private boolean active=true;
 protected Promotion(){}
 public Promotion(String code,DiscountType type,BigDecimal amount,Instant startsAt,Instant endsAt){this.code=code;this.discountType=type;this.amount=amount;this.startsAt=startsAt;this.endsAt=endsAt;}
 public Long getId(){return id;} public String getCode(){return code;} public DiscountType getDiscountType(){return discountType;} public BigDecimal getAmount(){return amount;} public Instant getStartsAt(){return startsAt;} public Instant getEndsAt(){return endsAt;} public boolean isActive(){return active;} public void setActive(boolean active){this.active=active;}
 public boolean isValid(Instant now){return active&&!now.isBefore(startsAt)&&now.isBefore(endsAt);}
}
