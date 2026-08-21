package io.github.spartateam6.commercepaymentsystem.domain.product.dto.request;

import lombok.Builder;

@Builder
public record ProductSearchCondition(
        String category,
        Integer minPrice,
        Integer maxPrice,
        String saleStatus,
        Boolean soldOut,
        String sort,
        int page,
        int size
){}
