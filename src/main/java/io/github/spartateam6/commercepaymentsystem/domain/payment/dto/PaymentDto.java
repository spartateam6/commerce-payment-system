package io.github.spartateam6.commercepaymentsystem.domain.payment.dto;

import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.PaymentStatus;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
public record PaymentDto(
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long id,
        Integer orderAmount,
        PaymentStatus status,
        String portonePaymentId,
        LocalDateTime completedAt
) {

  public static PaymentDto from(Payment payment) {
    return PaymentDto.builder()
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .id(payment.getId())
            .orderAmount(payment.getOrderAmount())
            .status(payment.getStatus())
            .portonePaymentId(payment.getPortonePaymentId())
            .completedAt(payment.getCompletedAt())
            .build();
  }

}