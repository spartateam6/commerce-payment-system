package io.github.spartateam6.commercepaymentsystem.domain.point.dto;

import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.point.entity.PointTransaction;
import io.github.spartateam6.commercepaymentsystem.domain.point.entity.PointTransactionType;

import java.time.LocalDateTime;

public record PointTransactionResponse(

        Long transactionId,
        PointTransactionType type,
        String typeLabel,
        Integer amount,
        Long paymentId,
        LocalDateTime createdAt
) {

    public static PointTransactionResponse from(PointTransaction pointTransaction) {
        Payment payment = pointTransaction.getPayment();
        return new PointTransactionResponse(
                pointTransaction.getId(),
                pointTransaction.getTransactionType(),
                pointTransaction.getTransactionType().getLabel(),
                pointTransaction.getAmount(),
                payment == null ? null : payment.getId(),
                pointTransaction.getCreatedAt()
        );
    }
}
