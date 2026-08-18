package io.github.spartateam6.commercepaymentsystem.domain.order.controller;

import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderCreateRequest;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderCreateResponse;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderDetailResponse;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderPreviewRequest;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderPreviewResponse;
import io.github.spartateam6.commercepaymentsystem.domain.order.facade.OrderFacade;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderService;
import io.github.spartateam6.commercepaymentsystem.global.annotation.MemberId;
import io.github.spartateam6.commercepaymentsystem.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderFacade orderFacade;
    private final OrderService orderService;

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

    @GetMapping
    public ApiResponse<List<OrderDetailResponse>> getOrderList(
            @MemberId Long memberId,
            @Parameter(hidden = true) @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ApiResponse.ok(orderService.getOrderList(memberId, pageable));
    }


    @PostMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(
            @MemberId Long memberId,
            @PathVariable Long orderId
    ) {
        orderFacade.cancelOrder(memberId, orderId);
        return ApiResponse.ok();
    }

}
