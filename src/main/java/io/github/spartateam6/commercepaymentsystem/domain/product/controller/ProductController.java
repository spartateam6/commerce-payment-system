package io.github.spartateam6.commercepaymentsystem.domain.product.controller;

import io.github.spartateam6.commercepaymentsystem.domain.product.dto.request.ProductSearchCondition;
import io.github.spartateam6.commercepaymentsystem.domain.product.dto.response.PageResponse;
import io.github.spartateam6.commercepaymentsystem.domain.product.dto.response.ProductResponse;
import io.github.spartateam6.commercepaymentsystem.domain.product.service.ProductService;
import io.github.spartateam6.commercepaymentsystem.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/api/products")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ProductSearchCondition condition = new ProductSearchCondition(category, minPrice, maxPrice, page, size);
        return ResponseEntity.ok(ApiResponse.ok(productService.getProducts(condition)));
    }

    @GetMapping("/api/products/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @PathVariable Long productId
    ){
        return ResponseEntity.ok(ApiResponse.ok(productService.getProduct(productId)));
    }
}