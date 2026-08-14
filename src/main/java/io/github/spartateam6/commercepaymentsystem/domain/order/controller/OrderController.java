package io.github.spartateam6.commercepaymentsystem.domain.order.controller;

import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderCreateRequest;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderCreateResponse;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderPreviewRequest;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderPreviewResponse;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderService;
import io.github.spartateam6.commercepaymentsystem.global.annotation.MemberId;
import io.github.spartateam6.commercepaymentsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<OrderPreviewResponse>> preview(
            @MemberId Long memberId,
            @Valid @RequestBody OrderPreviewRequest request
    ) {
        OrderPreviewResponse response = orderService.preview(memberId, request);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>>
    createOrder(
            @MemberId Long memberId,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        OrderCreateResponse response = orderService.createOrder(memberId, request);

        URI location = URI.create("/api/orders/" + response.orderId());

        return ResponseEntity.created(location).body(ApiResponse.ok(response));
    }
}