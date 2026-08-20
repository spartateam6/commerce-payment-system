package io.github.spartateam6.commercepaymentsystem.domain.point.dto;

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
        return new PointTransactionResponse(
                pointTransaction.getId(),
                pointTransaction.getTransactionType(),
                pointTransaction.getTransactionType().getLabel(),
                pointTransaction.getAmount(),
                pointTransaction.getPayment().getId(),
                pointTransaction.getCreatedAt()
        );
    }
}
