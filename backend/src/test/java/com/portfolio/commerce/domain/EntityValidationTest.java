package com.portfolio.commerce.domain;

import com.portfolio.commerce.domain.cart.CartItem;
import com.portfolio.commerce.domain.product.Product;
import com.portfolio.commerce.domain.user.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bean Validation tests for the domain constraints that are enforced at the
 * application layer (in addition to the database constraints).
 */
class EntityValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void userDniIsRequired() {
        User user = new User("", "Cesar", "Perez", false);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void productPriceMustNotBeNull() {
        Product product = new Product("Teclado", "Teclado mecanico", null, true);

        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void productPriceMustNotBeNegative() {
        Product product = new Product("Teclado", "Teclado mecanico", new BigDecimal("-0.01"), true);

        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void cartItemQuantityMustBeAtLeastOne() {
        CartItem cartItem = new CartItem(null, null, 0);

        Set<ConstraintViolation<CartItem>> violations = validator.validate(cartItem);

        assertThat(violations).isNotEmpty();
    }
}
