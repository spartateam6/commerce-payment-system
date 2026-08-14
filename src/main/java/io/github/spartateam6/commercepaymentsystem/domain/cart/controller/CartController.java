package io.github.spartateam6.commercepaymentsystem.domain.cart.controller;

import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.request.CartItemAddRequest;
import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.response.CartItemResponse;
import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.response.CartResponse;
import io.github.spartateam6.commercepaymentsystem.domain.cart.service.CartService;
import io.github.spartateam6.commercepaymentsystem.global.annotation.MemberId;
import io.github.spartateam6.commercepaymentsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
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