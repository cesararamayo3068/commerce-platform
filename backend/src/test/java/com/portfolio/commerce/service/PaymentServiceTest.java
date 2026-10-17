package com.portfolio.commerce.service;

import com.portfolio.commerce.domain.cart.*;
import com.portfolio.commerce.domain.order.*;
import com.portfolio.commerce.domain.payment.*;
import com.portfolio.commerce.domain.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
 @Mock PaymentRepository payments;
 @Mock OrderRepository orders;
 @InjectMocks PaymentService service;
 private static void id(Object object,Long id){try{var field=object.getClass().getDeclaredField("id");field.setAccessible(true);field.set(object,id);}catch(Exception e){throw new RuntimeException(e);}}
 private Order order(){User user=new User("123","Test","User",false);id(user,1L);Cart cart=new Cart(user,CartStatus.CHECKED_OUT);id(cart,2L);Order order=new Order(user,cart,BigDecimal.TEN,BigDecimal.ZERO,BigDecimal.TEN,null);id(order,3L);return order;}
 @Test void cannotSimulateAnotherUsersOrder(){when(orders.findByIdForUpdate(3L)).thenReturn(Optional.of(order()));assertThatThrownBy(()->service.simulate(3L,99L,PaymentStatus.APPROVED)).isInstanceOf(AccessDeniedException.class);verifyNoInteractions(payments);}
 @Test void pendingCanRequestRejection(){Order order=order();Payment payment=new Payment(order);when(orders.findByIdForUpdate(3L)).thenReturn(Optional.of(order));when(payments.findByOrderIdForUpdate(3L)).thenReturn(Optional.of(payment));service.simulate(3L,1L,PaymentStatus.REJECTED);assertThat(payment.getOutcome()).isEqualTo(PaymentStatus.REJECTED);assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);}
 @Test void completedPaymentCannotBeChanged(){Order order=order();Payment payment=new Payment(order);payment.setOutcome(PaymentStatus.APPROVED);payment.settle();when(orders.findByIdForUpdate(3L)).thenReturn(Optional.of(order));when(payments.findByOrderIdForUpdate(3L)).thenReturn(Optional.of(payment));assertThatThrownBy(()->service.simulate(3L,1L,PaymentStatus.REJECTED)).isInstanceOf(ResponseStatusException.class);}
 @Test void workerSettlesOnce(){Order order=order();Payment payment=new Payment(order);id(payment,4L);payment.setOutcome(PaymentStatus.APPROVED);when(payments.findById(4L)).thenReturn(Optional.of(payment));when(orders.findByIdForUpdate(3L)).thenReturn(Optional.of(order));when(payments.findByOrderIdForUpdate(3L)).thenReturn(Optional.of(payment));service.process(4L);service.process(4L);assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);verify(orders,times(1)).findByIdForUpdate(3L);}
}
