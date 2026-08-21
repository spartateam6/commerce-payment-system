package io.github.spartateam6.commercepaymentsystem.domain.order.dto;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;

public record OrderCreateResponse(
        Long orderId,
        String orderNumber,
        Integer totalAmount,
        Integer pointUsedAmount,
        String portonePaymentId,
        Integer pgAmount
) {
    public static OrderCreateResponse from(Order order, Payment payment) {
        return new OrderCreateResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getPointUsedAmount(),
                payment.getPortonePaymentId(),
                payment.getPgAmount()
        );
    }
}