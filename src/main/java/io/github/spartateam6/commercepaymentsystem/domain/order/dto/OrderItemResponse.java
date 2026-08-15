package io.github.spartateam6.commercepaymentsystem.domain.order.dto;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderItemResponse(
        Long orderItemId,
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineAmount,
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
