package io.github.spartateam6.commercepaymentsystem.domain.payment.dto;

import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.PaymentStatus;
import lombok.Builder;

@Builder
public record PaymentConfirmResponseDto(
        Long paymentId,
        String portonePaymentId,
        Integer amount,
        PaymentStatus paymentStatus
) {
    public static PaymentConfirmResponseDto success(PaymentDto dto) {
        return PaymentConfirmResponseDto.builder()
                .paymentId(dto.id())
                .portonePaymentId(dto.portonePaymentId())
                .amount(dto.orderAmount())
                .paymentStatus(PaymentStatus.PAID)
                .build();
    }

    public static PaymentConfirmResponseDto fail() {
        return PaymentConfirmResponseDto.builder()
                .paymentId(null)
                .portonePaymentId(null)
                .amount(null)
                .paymentStatus(PaymentStatus.FAILED)
                .build();
    }
}
