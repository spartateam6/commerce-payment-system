package io.github.spartateam6.commercepaymentsystem.domain.order.dto;

import java.math.BigDecimal;

public record PreparedItem (
        Long cartItemId,
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineAmount
){
}
