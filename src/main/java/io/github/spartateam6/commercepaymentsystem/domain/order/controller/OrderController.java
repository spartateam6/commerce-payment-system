package io.github.spartateam6.commercepaymentsystem.domain.order.controller;

import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderCreateRequest;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderCreateResponse;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderDetailResponse;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderPreviewRequest;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderPreviewResponse;
import io.github.spartateam6.commercepaymentsystem.domain.order.facade.OrderFacade;
import io.github.spartateam6.commercepaymentsystem.global.annotation.MemberId;
import io.github.spartateam6.commercepaymentsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderFacade orderFacade;

    public OrderController(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<OrderPreviewResponse>> preview(
            @MemberId Long memberId,
            @Valid @RequestBody OrderPreviewRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(orderFacade.preview(memberId, request))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @MemberId Long memberId,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        OrderCreateResponse response = orderFacade.createOrder(memberId, request);
        URI location = URI.create("/api/orders/" + response.orderId());

        return ResponseEntity.created(location)
                .body(ApiResponse.ok(response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @MemberId Long memberId,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(orderFacade.getOrderDetail(memberId, orderId))
        );
    }
}
