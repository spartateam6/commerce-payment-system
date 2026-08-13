package io.github.spartateam6.commercepaymentsystem.domain.product.repository;

import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> categoryEquals(String category) {
        return (root, query, cb)
                -> category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Product> priceGoe(Integer minPrice) {
        return (root, query, cb)
                -> minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> priceLoe(Integer maxPrice) {
        return (root, query, cb)
                ->  maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }
}
