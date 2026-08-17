package io.github.spartateam6.commercepaymentsystem.domain.order.dto;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderItem;

import java.time.LocalDateTime;

public record OrderItemResponse(
        Long orderItemId,
        Long productId,
        String productName,
        Integer unitPrice,
        Integer quantity,
        Integer lineAmount,
        LocalDateTime createdAt
) {
    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getProductNameSnapshot(),
                orderItem.getUnitPriceSnapshot(),
                orderItem.getQuantity(),
                orderItem.calculateLineAmount(),
                orderItem.getCreatedAt()
        );
    }
}