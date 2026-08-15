package io.github.spartateam6.commercepaymentsystem.domain.cart.dto.request;

import jakarta.validation.constraints.NotNull;

public record CartItemQuantityUpdateRequest(

        @NotNull(message = "수량은 필수입니다.")
        Integer quantity

) {
}
