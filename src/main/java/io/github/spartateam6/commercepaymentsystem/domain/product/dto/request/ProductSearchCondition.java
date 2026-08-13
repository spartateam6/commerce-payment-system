package io.github.spartateam6.commercepaymentsystem.domain.product.dto.request;

public record ProductSearchCondition(
        String category,
        Integer minPrice,
        Integer maxPrice,
        int page,
        int size
){}
