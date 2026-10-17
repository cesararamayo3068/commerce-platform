package com.portfolio.commerce.domain.payment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface PaymentRepository extends JpaRepository<Payment,Long>{
 Optional<Payment> findByOrderId(Long orderId);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Payment p where p.order.id=:orderId") Optional<Payment> findByOrderIdForUpdate(@Param("orderId") Long orderId);
 @Query("select p.id from Payment p where p.status='PENDING' and p.outcome is not null order by p.id") List<Long> findPendingIds(org.springframework.data.domain.Pageable pageable);
}
