package com.portfolio.commerce.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByActive(boolean active, Pageable pageable);

    @Query("""
        select p from Product p
        where (:active is null or p.active = :active)
          and (lower(p.name) like lower(concat('%', :search, '%'))
            or lower(coalesce(p.brand, '')) like lower(concat('%', :search, '%'))
            or lower(coalesce(p.category, '')) like lower(concat('%', :search, '%')))
        """)
    Page<Product> search(@Param("active") Boolean active, @Param("search") String search, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
