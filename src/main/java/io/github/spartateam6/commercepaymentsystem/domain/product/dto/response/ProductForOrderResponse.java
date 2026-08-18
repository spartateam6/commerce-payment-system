package io.github.spartateam6.commercepaymentsystem.domain.product.dto.response;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;


public record ProductForOrderResponse(
        Product product,
        Long productId,
        String productName,
        Integer unitPrice,
        Integer stock
) {
    public static ProductForOrderResponse from(Product product) {
        return new ProductForOrderResponse(
                product,
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock()
        );
    }
}
