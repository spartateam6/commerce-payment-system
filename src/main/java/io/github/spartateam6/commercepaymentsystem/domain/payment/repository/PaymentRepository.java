package io.github.spartateam6.commercepaymentsystem.domain.payment.repository;

import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}