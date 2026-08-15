package io.github.spartateam6.commercepaymentsystem.domain.order.dto;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long orderId,
        String orderNumber,
        BigDecimal totalAmount,
        OrderStatus orderStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResponse> orderItems,
        PaymentResponse payment
) {
    public static OrderDetailResponse from(Order order, PaymentResponse payment) {
        List<OrderItemResponse> items = order.getOrderItems()
                .stream()
                .map(OrderItemResponse::from)
                .toList();

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items,
                payment
        );
    }

    public record PaymentResponse(
            Long paymentId,
            BigDecimal amount,
            String status,
            LocalDateTime completedAt,
            LocalDateTime createdAt
    ) {
    }
}
