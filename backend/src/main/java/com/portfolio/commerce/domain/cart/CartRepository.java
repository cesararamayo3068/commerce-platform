package com.portfolio.commerce.domain.cart;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Page<Cart> findByUserId(Long userId, Pageable pageable);

    Page<Cart> findByStatus(CartStatus status, Pageable pageable);

    Page<Cart> findByUserIdAndStatus(Long userId, CartStatus status, Pageable pageable);
}
