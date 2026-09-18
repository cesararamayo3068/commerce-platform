package com.portfolio.commerce.service;

import com.portfolio.commerce.domain.cart.Cart;
import com.portfolio.commerce.domain.cart.CartItem;
import com.portfolio.commerce.domain.cart.CartItemRepository;
import com.portfolio.commerce.domain.cart.CartRepository;
import com.portfolio.commerce.domain.cart.CartStatus;
import com.portfolio.commerce.domain.product.Product;
import com.portfolio.commerce.domain.product.ProductRepository;
import com.portfolio.commerce.domain.user.User;
import com.portfolio.commerce.domain.user.UserRepository;
import com.portfolio.commerce.web.dto.CartCreateRequest;
import com.portfolio.commerce.web.dto.CartItemQuantityUpdateRequest;
import com.portfolio.commerce.web.dto.CartItemRequest;
import com.portfolio.commerce.web.dto.CartResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private final User user = user(1L);
    private final Product product = product(5L, "Teclado", new BigDecimal("100.00"), true);

    private User user(Long id) {
        User u = new User("12345678", "Cesar", "Perez", false);
        setId(u, id);
        return u;
    }

    private Product product(Long id, String name, BigDecimal price, boolean active) {
        Product p = new Product(name, "Description of " + name, price, active);
        setId(p, id);
        return p;
    }

    private Cart cart(Long id, User owner, CartStatus status) {
        Cart c = new Cart(owner, status);
        setId(c, id);
        return c;
    }

    private CartItem item(Long id, Cart cart, Product product, Integer quantity) {
        CartItem ci = new CartItem(cart, product, quantity);
        setId(ci, id);
        return ci;
    }

    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void createPersistsActiveCartForExistingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Cart saved = cart(10L, user, CartStatus.ACTIVE);
        when(cartRepository.save(any(Cart.class))).thenReturn(saved);
        when(cartItemRepository.findByCartIdOrderByIdAsc(10L)).thenReturn(List.of());

        CartResponse response = cartService.create(new CartCreateRequest(1L));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(CartStatus.ACTIVE);
        assertThat(response.items()).isEmpty();
        assertThat(response.total()).isEqualByComparingTo("0");
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void createThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.create(new CartCreateRequest(99L)))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getByIdThrowsWhenCartDoesNotExist() {
        when(cartRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getById(99L))
                .isInstanceOf(CartNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void addItemCreatesNewItemWhenProductNotInCart() {
        Cart cart = cart(10L, user, CartStatus.ACTIVE);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(10L, 5L)).thenReturn(Optional.empty());
        when(cartItemRepository.findByCartIdOrderByIdAsc(10L))
                .thenReturn(List.of(item(1L, cart, product, 2)));

        CartResponse response = cartService.addItem(10L, new CartItemRequest(5L, 2));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).productId()).isEqualTo(5L);
        assertThat(response.items().get(0).quantity()).isEqualTo(2);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItemIncrementsQuantityWhenProductAlreadyInCart() {
        Cart cart = cart(10L, user, CartStatus.ACTIVE);
        CartItem existing = item(1L, cart, product, 2);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(10L, 5L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.findByCartIdOrderByIdAsc(10L)).thenReturn(List.of(existing));

        CartResponse response = cartService.addItem(10L, new CartItemRequest(5L, 3));

        assertThat(existing.getQuantity()).isEqualTo(5);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(5);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addItemRejectsInactiveProduct() {
        Product inactive = product(5L, "Teclado", new BigDecimal("100.00"), false);
        Cart cart = cart(10L, user, CartStatus.ACTIVE);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(5L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> cartService.addItem(10L, new CartItemRequest(5L, 1)))
                .isInstanceOf(ProductNotActiveException.class);
    }

    @Test
    void addItemRejectsCancelledCart() {
        Cart cancelled = cart(10L, user, CartStatus.CANCELLED);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cancelled));

        assertThatThrownBy(() -> cartService.addItem(10L, new CartItemRequest(5L, 1)))
                .isInstanceOf(CartNotActiveException.class);
    }

    @Test
    void updateQuantitySetsExactValue() {
        Cart cart = cart(10L, user, CartStatus.ACTIVE);
        CartItem existing = item(1L, cart, product, 2);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(10L, 5L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.findByCartIdOrderByIdAsc(10L)).thenReturn(List.of(existing));

        CartResponse response = cartService.updateQuantity(10L, 5L, new CartItemQuantityUpdateRequest(4));

        assertThat(existing.getQuantity()).isEqualTo(4);
        assertThat(response.items().get(0).quantity()).isEqualTo(4);
    }

    @Test
    void quantityMustBeGreaterThanZero() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        Set<ConstraintViolation<CartItemRequest>> itemViolations =
                validator.validate(new CartItemRequest(5L, 0));
        Set<ConstraintViolation<CartItemQuantityUpdateRequest>> updateViolations =
                validator.validate(new CartItemQuantityUpdateRequest(0));

        assertThat(itemViolations).isNotEmpty();
        assertThat(updateViolations).isNotEmpty();
    }

    @Test
    void removeItemDeletesItem() {
        Cart cart = cart(10L, user, CartStatus.ACTIVE);
        CartItem existing = item(1L, cart, product, 5);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(10L, 5L)).thenReturn(Optional.of(existing));

        cartService.removeItem(10L, 5L);

        verify(cartItemRepository).delete(existing);
    }

    @Test
    void removeItemThrowsWhenItemDoesNotExist() {
        Cart cart = cart(10L, user, CartStatus.ACTIVE);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(10L, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItem(10L, 5L))
                .isInstanceOf(CartItemNotFoundException.class);
    }

    @Test
    void cancelSetsStatusToCancelled() {
        Cart cart = cart(10L, user, CartStatus.ACTIVE);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(cartRepository.saveAndFlush(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByCartIdOrderByIdAsc(10L)).thenReturn(List.of());

        CartResponse response = cartService.cancel(10L);

        assertThat(cart.getStatus()).isEqualTo(CartStatus.CANCELLED);
        assertThat(response.status()).isEqualTo(CartStatus.CANCELLED);
    }

    @Test
    void cancelRejectsCartAlreadyCancelled() {
        Cart cancelled = cart(10L, user, CartStatus.CANCELLED);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cancelled));

        assertThatThrownBy(() -> cartService.cancel(10L))
                .isInstanceOf(CartNotActiveException.class);
    }

    @Test
    void itemSubtotalIsUnitPriceTimesQuantity() {
        Cart cart = cart(10L, user, CartStatus.ACTIVE);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByIdAsc(10L))
                .thenReturn(List.of(item(1L, cart, product, 3)));

        CartResponse response = cartService.getById(10L);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).unitPrice()).isEqualByComparingTo("100.00");
        assertThat(response.items().get(0).subtotal()).isEqualByComparingTo("300.00");
    }

    @Test
    void cartTotalIsSumOfItemSubtotals() {
        Cart cart = cart(10L, user, CartStatus.ACTIVE);
        Product mouse = product(6L, "Mouse", new BigDecimal("50.00"), true);
        when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByIdAsc(10L))
                .thenReturn(List.of(item(1L, cart, product, 2), item(2L, cart, mouse, 1)));

        CartResponse response = cartService.getById(10L);

        assertThat(response.items()).hasSize(2);
        assertThat(response.total()).isEqualByComparingTo("250.00");
    }
}
