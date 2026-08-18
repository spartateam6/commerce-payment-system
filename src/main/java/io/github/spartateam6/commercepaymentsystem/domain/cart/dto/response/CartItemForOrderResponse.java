package io.github.spartateam6.commercepaymentsystem.domain.cart.dto.response;

import io.github.spartateam6.commercepaymentsystem.domain.cart.entity.CartItem;

public record CartItemForOrderResponse(
        Long cartItemId,
        Long productId,
        Integer quantity
) {

    public static CartItemForOrderResponse from(CartItem cartItem) {
        return new CartItemForOrderResponse(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getQuantity()
        );
    }
}
