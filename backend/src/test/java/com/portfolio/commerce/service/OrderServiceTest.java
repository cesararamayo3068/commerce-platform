package com.portfolio.commerce.service;

import com.portfolio.commerce.domain.cart.*;
import com.portfolio.commerce.domain.order.*;
import com.portfolio.commerce.domain.product.*;
import com.portfolio.commerce.domain.user.User;
import com.portfolio.commerce.web.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock ProductRepository productRepository;
    @Mock PromotionService promotionService;
    @InjectMocks OrderService orderService;

    @Test
    void checkoutCreatesOrderDecrementsStockAndClosesCart() {
        User user = new User("123", "Cesar", "Test", false); setId(user, 1L);
        Product product = new Product("Keyboard", "Mechanical", new BigDecimal("100.00"), true); setId(product, 5L); product.setStock(10);
        Cart cart = new Cart(user, CartStatus.ACTIVE); setId(cart, 7L);
        CartItem item = new CartItem(cart, product, 2); setId(item, 9L);

        when(cartRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByIdAsc(7L)).thenReturn(List.of(item));
        when(productRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> { Order o = inv.getArgument(0); setId(o, 11L); return o; });
        when(orderItemRepository.findByOrderIdOrderByIdAsc(11L)).thenAnswer(inv -> {
            Order order = new Order(user, cart, new BigDecimal("200.00"), BigDecimal.ZERO, new BigDecimal("200.00"), null); setId(order, 11L);
            OrderItem oi = new OrderItem(order, product, product.getName(), product.getPrice(), 2); setId(oi, 12L); return List.of(oi);
        });
        when(cartRepository.saveAndFlush(cart)).thenReturn(cart);

        OrderResponse response = orderService.checkout(7L, 1L);

        assertThat(response.total()).isEqualByComparingTo("200.00");
        assertThat(response.items()).hasSize(1);
        assertThat(product.getStock()).isEqualTo(8);
        assertThat(cart.getStatus()).isEqualTo(CartStatus.CHECKED_OUT);
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    void checkoutRejectsInsufficientStockWithoutCreatingOrder() {
        User user = new User("123", "Cesar", "Test", false); setId(user, 1L);
        Product product = new Product("Keyboard", "Mechanical", new BigDecimal("100.00"), true); setId(product, 5L); product.setStock(1);
        Cart cart = new Cart(user, CartStatus.ACTIVE); setId(cart, 7L);
        CartItem item = new CartItem(cart, product, 2);
        when(cartRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByIdAsc(7L)).thenReturn(List.of(item));
        when(productRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.checkout(7L, 1L)).isInstanceOf(InsufficientStockException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void checkoutRejectsEmptyCart() {
        User user = new User("123", "Cesar", "Test", false); setId(user, 1L);
        Cart cart = new Cart(user, CartStatus.ACTIVE); setId(cart, 7L);
        when(cartRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByIdAsc(7L)).thenReturn(List.of());
        assertThatThrownBy(() -> orderService.checkout(7L, 1L)).isInstanceOf(EmptyCartException.class);
    }

    private static void setId(Object entity, Long id) {
        try { var field = entity.getClass().getDeclaredField("id"); field.setAccessible(true); field.set(entity, id); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
}
