package io.github.spartateam6.commercepaymentsystem.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record OrderPreviewRequest(
        List<@NotNull(message = "장바구니 상품 ID는 필수입니다.")
                @Positive(message = "장바구니 상품 ID는 양수여야 합니다.") Long> cartItemIds
) {
    public OrderPreviewRequest {
        cartItemIds = cartItemIds == null
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(cartItemIds));
    }
}
