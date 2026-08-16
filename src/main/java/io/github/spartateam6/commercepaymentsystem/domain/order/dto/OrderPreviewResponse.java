package io.github.spartateam6.commercepaymentsystem.domain.order.dto;

import java.util.List;

public record OrderPreviewResponse(
        List<PreviewItem> items,
        Integer totalAmount
) {
    public record PreviewItem(
            Long cartItemId,
            Long productId,
            String productName,
            Integer unitPrice,
            Integer quantity,
            Integer lineAmount
    ) {
    }
}