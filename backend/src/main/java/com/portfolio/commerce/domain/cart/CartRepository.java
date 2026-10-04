package com.portfolio.commerce.domain.cart;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Page<Cart> findByUserId(Long userId, Pageable pageable);

    Page<Cart> findByStatus(CartStatus status, Pageable pageable);

    Page<Cart> findByUserIdAndStatus(Long userId, CartStatus status, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.id = :id")
    Optional<Cart> findByIdForUpdate(@Param("id") Long id);
}
