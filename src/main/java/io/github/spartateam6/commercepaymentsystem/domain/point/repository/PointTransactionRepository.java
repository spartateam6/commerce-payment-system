package io.github.spartateam6.commercepaymentsystem.domain.point.repository;

import io.github.spartateam6.commercepaymentsystem.domain.point.entity.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
}