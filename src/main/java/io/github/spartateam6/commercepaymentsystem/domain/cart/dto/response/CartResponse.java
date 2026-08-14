package io.github.spartateam6.commercepaymentsystem.domain.cart.dto.response;

import java.util.List;

public record CartResponse(
        Long cartId,
        List<CartItemResponse> items,
        Integer totalPrice
) {
    public static CartResponse of(
            Long cartId,
            List<CartItemResponse> items
    ) {
        int totalPrice = items.stream()
                .mapToInt(CartItemResponse::totalPrice)
                .sum();

        return new CartResponse(
                cartId,
                items,
                totalPrice
        );
    }
}