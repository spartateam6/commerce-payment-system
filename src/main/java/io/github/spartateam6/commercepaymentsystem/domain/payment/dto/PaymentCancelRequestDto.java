package io.github.spartateam6.commercepaymentsystem.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentCancelRequestDto(
        @NotBlank
        String orderNumber
) {}
