package io.github.spartateam6.commercepaymentsystem.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemAddRequest(

        @NotNull(message = "상품 ID는 필수입니다.")
        Long productId,

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        Integer quantity

) {
}