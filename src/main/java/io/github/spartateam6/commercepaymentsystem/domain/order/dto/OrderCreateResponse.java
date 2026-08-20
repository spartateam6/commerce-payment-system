package io.github.spartateam6.commercepaymentsystem.domain.order.dto;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;

public record OrderCreateResponse(
        Long orderId,
        String orderNumber,
        Integer totalAmount, // 상품 전체 가격
        Integer pointUsedAmount, // 사용할 포인트 총액
        Integer pgPaymentAmount // totalAmount - pgPaymentAmount = 실제 결제할 금액

) {
    public static OrderCreateResponse from(Order order) {
        return new OrderCreateResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getPointUsedAmount(),
                order.calculatePgPaymentAmount()
        );
    }
}