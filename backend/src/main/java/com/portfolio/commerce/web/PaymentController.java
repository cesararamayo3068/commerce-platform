package com.portfolio.commerce.web;
import com.portfolio.commerce.domain.payment.PaymentStatus;
import com.portfolio.commerce.service.PaymentService;
import com.portfolio.commerce.web.dto.PaymentResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/payments")
public class PaymentController {
 private final PaymentService service;
 public PaymentController(PaymentService service){this.service=service;}
 @GetMapping("/orders/{orderId}")
 public PaymentResponse get(@PathVariable Long orderId,@AuthenticationPrincipal Jwt jwt){return service.get(orderId,userId(jwt),jwt.getClaimAsStringList("roles").contains("ADMIN"));}
 @PostMapping("/orders/{orderId}/simulate")
 public PaymentResponse simulate(@PathVariable Long orderId,@RequestBody Simulation request,@AuthenticationPrincipal Jwt jwt){return service.simulate(orderId,userId(jwt),request.outcome());}
 public record Simulation(PaymentStatus outcome){}
 private Long userId(Jwt jwt){return ((Number)jwt.getClaim("userId")).longValue();}
}
