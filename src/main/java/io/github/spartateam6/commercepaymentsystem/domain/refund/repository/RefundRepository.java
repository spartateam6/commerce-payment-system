package io.github.spartateam6.commercepaymentsystem.domain.refund.repository;

import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
}