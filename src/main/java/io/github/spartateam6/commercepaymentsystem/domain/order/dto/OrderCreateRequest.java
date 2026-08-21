package io.github.spartateam6.commercepaymentsystem.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record OrderCreateRequest(
        List<
                @NotNull(message = "장바구니 상품 ID는 필수입니다.")
                @Positive(message = "장바구니 상품 ID는 양수여야 합니다.") Long> cartItemIds,

        @PositiveOrZero(message = "사용 포인트는 0 이상이어야 합니다.") Integer pointToUse
) {
    public OrderCreateRequest {
        cartItemIds = cartItemIds == null
                ? List.of()
                : Collections.unmodifiableList(
                new ArrayList<>(cartItemIds)
        );

        pointToUse = pointToUse == null ? 0 : pointToUse;
    }
}

