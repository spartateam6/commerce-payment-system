package io.github.spartateam6.commercepaymentsystem.domain.cart.controller;

import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.request.CartItemAddRequest;
import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.response.CartItemResponse;
import io.github.spartateam6.commercepaymentsystem.domain.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart/items")
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartItemResponse> addItem(
            @Valid @RequestBody CartItemAddRequest request
    ) {
        // TODO: 인증 기능 병합 후 교체
        Long temporaryMemberId = 1L;

        CartItemResponse response =
                cartService.addItem(
                        temporaryMemberId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}