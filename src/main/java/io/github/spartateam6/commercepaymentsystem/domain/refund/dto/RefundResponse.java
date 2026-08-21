package io.github.spartateam6.commercepaymentsystem.domain.refund.dto;

import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.Refund;
import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.RefundStatus;

public record RefundResponse(
        Long refundId,
        RefundStatus status,
        Integer pgRefundAmount,
        Integer pointRefundAmount
) {
    public static RefundResponse from(Refund refund) {
        return new RefundResponse(
                refund.getId(),
                refund.getStatus(),
                refund.getPgRefundAmount(),
                refund.getPointRefundAmount()
        );
    }
}
