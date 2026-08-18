package io.github.spartateam6.commercepaymentsystem.domain.cart.controller;

import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.request.CartItemAddRequest;
import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.request.CartItemQuantityUpdateRequest;
import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.response.CartItemResponse;
import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.response.CartResponse;
import io.github.spartateam6.commercepaymentsystem.domain.cart.service.CartService;
import io.github.spartateam6.commercepaymentsystem.global.annotation.MemberId;
import io.github.spartateam6.commercepaymentsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart/items")
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<ApiResponse<CartItemResponse>> addItem(
            @MemberId Long memberId,
            @Valid @RequestBody CartItemAddRequest request
    ) {
        CartItemResponse response =
                cartService.addItem(memberId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @PatchMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateItemQuantity(
            @MemberId Long memberId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemQuantityUpdateRequest request
    ) {
        CartItemResponse response = cartService.updateItemQuantity(
                memberId,
                cartItemId,
                request
        );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> deleteItem(
            @MemberId Long memberId,
            @PathVariable Long cartItemId
    ) {
        cartService.deleteItem(memberId, cartItemId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @MemberId Long memberId
    ) {
        cartService.clearCart(memberId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @MemberId Long memberId
    ){
        CartResponse response =
                cartService.getCart(memberId);
        return ResponseEntity.ok(
                ApiResponse.ok(response)
        );
    }
}
