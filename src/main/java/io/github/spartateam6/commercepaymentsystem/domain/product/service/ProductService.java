package io.github.spartateam6.commercepaymentsystem.domain.product.service;

import io.github.spartateam6.commercepaymentsystem.domain.product.dto.request.ProductSearchCondition;
import io.github.spartateam6.commercepaymentsystem.domain.product.dto.response.PageResponse;
import io.github.spartateam6.commercepaymentsystem.domain.product.dto.response.ProductResponse;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import io.github.spartateam6.commercepaymentsystem.domain.product.repository.ProductRepository;
import io.github.spartateam6.commercepaymentsystem.domain.product.repository.ProductSpecification;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;

    public PageResponse<ProductResponse> getProducts(ProductSearchCondition condition) {
        validatePaging(condition.page(), condition.size());

        Specification<Product> spec = Specification
                .where(ProductSpecification.categoryEquals(condition.category()))
                .and(ProductSpecification.priceGoe(condition.minPrice()))
                .and(ProductSpecification.priceLoe(condition.maxPrice()));

        Pageable pageable = PageRequest.of(
                condition.page() -1,
                condition.size(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        List<ProductResponse> content = productPage.getContent().stream()
                .map(ProductResponse::from)
                .toList();

        return PageResponse.of(content, productPage);
    }

    public ProductResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductResponse.from(product);
    }

    private void validatePaging(int page, int size) {
        if (page < 1) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "page는 1 이상이어야 합니다.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "size는 1 이상 " + MAX_PAGE_SIZE + " 이하이어야 합니다.");
        }
    }

    @Transactional
    public void deductStock(Long productId, int quantity) {
        validateQuantity(quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        product.deductStock(quantity);
    }

    @Transactional
    public void increaseStock(Long productId, int quantity) {
        validateQuantity(quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        product.increaseStock(quantity);
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "수량은 1 이상이어야 합니다.");
        }
    }
}
