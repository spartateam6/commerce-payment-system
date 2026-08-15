package io.github.spartateam6.commercepaymentsystem.domain.order.dto;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;

import java.math.BigDecimal;

public record OrderCreateResponse(
        Long orderId,
        String orderNumber,
        BigDecimal totalAmount
) {
    public static OrderCreateResponse from(Order order) {
        return new OrderCreateResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount()
        );
    }
}
