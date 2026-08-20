package io.github.spartateam6.commercepaymentsystem.domain.point.repository;

import io.github.spartateam6.commercepaymentsystem.domain.point.entity.PointTransaction;
import io.github.spartateam6.commercepaymentsystem.domain.point.entity.PointTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    Page<PointTransaction> findByMember_Id(Long memberId, Pageable pageable);

    Optional<PointTransaction> findByMember_IdAndPayment_IdAndTransactionType(
            Long memberId,
            Long paymentId,
            PointTransactionType transactionType
    );

    boolean existsByMember_IdAndPayment_IdAndTransactionTypeIn(
            Long memberId,
            Long paymentId,
            Collection<PointTransactionType> transactionTypes
    );
}