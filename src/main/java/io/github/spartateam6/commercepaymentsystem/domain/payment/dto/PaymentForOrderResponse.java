package io.github.spartateam6.commercepaymentsystem.domain.payment.dto;

import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;

import java.time.LocalDateTime;

public record PaymentForOrderResponse(
        Long paymentId,
        Long orderId,
        Integer amount,
        String status,
        LocalDateTime completedAt,
        LocalDateTime createdAt
) {
    public static PaymentForOrderResponse from(Payment payment) {
        return new PaymentForOrderResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getOrderAmount(),
                payment.getStatus().name(),
                payment.getCompletedAt(),
                payment.getCreatedAt()
        );
    }
}
