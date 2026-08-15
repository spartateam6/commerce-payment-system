package io.github.spartateam6.commercepaymentsystem.domain.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderPreviewResponse(
        List<PreviewItem> items,
        BigDecimal totalAmount
) {
    public record PreviewItem(
            Long cartItemId,
            Long productId,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineAmount
    ) {
    }
}
