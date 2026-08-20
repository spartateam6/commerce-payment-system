package io.github.spartateam6.commercepaymentsystem.domain.product.dto.response;

import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.SaleStatus;

import java.time.LocalDateTime;

public record ProductResponse (
        Long id,
        String name,
        Integer price,
        Integer stock,
        String category,
        SaleStatus saleStatus,
        boolean soldOut,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
    public static ProductResponse from(Product product){
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
               product.getSaleStatus(),
                product.getStock() == 0,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
