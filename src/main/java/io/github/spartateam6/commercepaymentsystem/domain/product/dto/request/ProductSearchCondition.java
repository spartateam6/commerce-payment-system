package io.github.spartateam6.commercepaymentsystem.domain.product.dto.request;

import io.github.spartateam6.commercepaymentsystem.domain.product.entity.SaleStatus;

public record ProductSearchCondition(
        String category,
        Integer minPrice,
        Integer maxPrice,
        SaleStatus saleStatus,
        Boolean soldOut,
        String sort,
        int page,
        int size
){}
