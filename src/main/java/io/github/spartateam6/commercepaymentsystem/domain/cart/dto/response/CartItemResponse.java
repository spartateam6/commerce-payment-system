package io.github.spartateam6.commercepaymentsystem.domain.cart.dto.response;

import io.github.spartateam6.commercepaymentsystem.domain.cart.entity.CartItem;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;

public record CartItemResponse(
        Long cartItemId,
        Long productId,
        String productName,
        Integer price,
        Integer quantity,
        Integer totalPrice
) {

    public static CartItemResponse from(CartItem cartItem) {
        Product product = cartItem.getProduct();

        return new CartItemResponse(
                cartItem.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                cartItem.getQuantity(),
                product.getPrice() * cartItem.getQuantity()
        );
    }
}