package com.portfolio.commerce.service;
import com.portfolio.commerce.domain.payment.*;
import com.portfolio.commerce.domain.order.*;
import com.portfolio.commerce.web.dto.PaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
@Service
public class PaymentService {
 private final PaymentRepository payments; private final OrderRepository orders;
 public PaymentService(PaymentRepository payments,OrderRepository orders){this.payments=payments;this.orders=orders;}
 @Transactional(readOnly=true)
 public PaymentResponse get(Long orderId,Long userId,boolean admin){
  Order order=orders.findById(orderId).orElseThrow(()->new OrderNotFoundException(orderId));
  if(!admin&&!order.getUser().getId().equals(userId)) throw new org.springframework.security.access.AccessDeniedException("Not your order");
  return payments.findByOrderId(orderId).map(PaymentResponse::from).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"No payment for this order"));
 }
 @Transactional
 public PaymentResponse simulate(Long orderId,Long userId,PaymentStatus outcome){
  if(outcome==null||outcome==PaymentStatus.PENDING) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose APPROVED or REJECTED");
  Order order=orders.findByIdForUpdate(orderId).orElseThrow(()->new OrderNotFoundException(orderId));
  if(!order.getUser().getId().equals(userId)) throw new org.springframework.security.access.AccessDeniedException("Not your order");
  Payment payment=payments.findByOrderIdForUpdate(orderId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"No payment for this order"));
  if(order.getStatus()==OrderStatus.CANCELLED) throw new ResponseStatusException(HttpStatus.CONFLICT,"Order cancelled");
  if(payment.getStatus()!=PaymentStatus.PENDING){
   if(payment.getStatus()==outcome)return PaymentResponse.from(payment);
   throw new ResponseStatusException(HttpStatus.CONFLICT,"Payment already finalized");
  }
  payment.setOutcome(outcome);
  return PaymentResponse.from(payment);
 }
 @Transactional
 public void process(Long id){
  Payment payment=payments.findById(id).orElse(null);
  if(payment==null||payment.getStatus()!=PaymentStatus.PENDING)return;
  // Lock order first: same lock order as checkout/admin transitions/simulation.
  Order order=orders.findByIdForUpdate(payment.getOrder().getId()).orElse(null);
  if(order==null)return;
  payment=payments.findByOrderIdForUpdate(order.getId()).orElseThrow();
  if(payment.getStatus()!=PaymentStatus.PENDING||order.getStatus()==OrderStatus.CANCELLED)return;
  payment.settle();
 }
}
