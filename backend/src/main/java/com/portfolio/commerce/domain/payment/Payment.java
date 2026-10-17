package com.portfolio.commerce.domain.payment;
import com.portfolio.commerce.domain.AuditableEntity;
import com.portfolio.commerce.domain.order.Order;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="payments")
public class Payment extends AuditableEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id",nullable=false,unique=true) private Order order;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private PaymentStatus status=PaymentStatus.PENDING;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private PaymentStatus outcome;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
 protected Payment(){}
 public Payment(Order order){this.order=order;this.amount=order.getTotal();}
 public Long getId(){return id;} public Order getOrder(){return order;}
 public PaymentStatus getStatus(){return status;} public PaymentStatus getOutcome(){return outcome;}
 public BigDecimal getAmount(){return amount;}
 public void setOutcome(PaymentStatus outcome){this.outcome=outcome;}
 public void settle(){this.status=outcome;}
}
