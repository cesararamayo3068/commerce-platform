package com.portfolio.commerce.web;

import com.portfolio.commerce.service.OrderService;
import com.portfolio.commerce.web.dto.OrderResponse;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping("/checkout/{cartId}")
    public ResponseEntity<OrderResponse> checkout(@PathVariable Long cartId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.checkout(cartId, userId(jwt)));
    }

    @GetMapping
    public List<OrderResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return orderService.listForUser(userId(jwt));
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return orderService.getById(id, userId(jwt));
    }

    private Long userId(Jwt jwt) { return ((Number) jwt.getClaim("userId")).longValue(); }
}
