package io.github.spartateam6.commercepaymentsystem.domain.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequestDto(
        @NotBlank
        String orderNumber
) {
}
