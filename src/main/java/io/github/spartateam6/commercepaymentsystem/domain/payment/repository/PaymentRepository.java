package io.github.spartateam6.commercepaymentsystem.domain.payment.repository;

import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("""
    SELECT p FROM Payment p
    JOIN FETCH p.order o
    WHERE o.orderNumber = :orderNumber
    """)
    Optional<Payment> findByOrderNumberWithOrder(
            @Param("orderNumber") String orderNumber
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT p FROM Payment p
    JOIN FETCH p.order o
    WHERE o.orderNumber = :orderNumber
    """)
    Optional<Payment> findByOrderNumberWithOrderLock(
            @Param("orderNumber") String orderNumber
    );

    Optional<Payment> findByOrder_Id(Long orderId);

    boolean existsByOrder_Id(Long orderId);
}