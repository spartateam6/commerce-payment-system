package io.github.spartateam6.commercepaymentsystem.domain.cart.dto.response;

import io.github.spartateam6.commercepaymentsystem.domain.cart.entity.CartItem;

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
    public static CartResponse empty() {
        return new CartResponse(null,List.of(),0);
    }
    public static CartResponse empty(Long cartId) {
        return new CartResponse(cartId,List.of(),0);
    }
}