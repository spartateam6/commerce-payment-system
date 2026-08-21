package io.github.spartateam6.commercepaymentsystem.domain.refund.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RefundRequest(
        @NotNull(message = "결제 ID는 필수입니다.")
        @Positive(message = "결제 ID는 양수여야 합니다.")
        Long paymentId,

        @NotBlank(message = "취소 사유는 필수입니다.")
        @Size(max = 500, message = "취소 사유는 500자 이하여야 합니다.")
        String cancelReason
) {
}
