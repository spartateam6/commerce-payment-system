package io.github.spartateam6.commercepaymentsystem.domain.product.service;

import io.github.spartateam6.commercepaymentsystem.domain.product.dto.request.ProductSearchCondition;
import io.github.spartateam6.commercepaymentsystem.domain.product.dto.response.PageResponse;
import io.github.spartateam6.commercepaymentsystem.domain.product.dto.response.ProductForOrderResponse;
import io.github.spartateam6.commercepaymentsystem.domain.product.dto.response.ProductResponse;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.SaleStatus;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                .and(ProductSpecification.priceLoe(condition.maxPrice()))
                .and(ProductSpecification.saleStatusEquals(resolveSaleStatus(condition.saleStatus()))
                .and(ProductSpecification.soldOutEquals(condition.soldOut())));

        Pageable pageable = PageRequest.of(
                condition.page() -1,
                condition.size(),
                resolveSort(condition.sort())
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

    private SaleStatus resolveSaleStatus(String saleStatus) {
        if (saleStatus == null) {
            return null;
        }
        try {
            return SaleStatus.valueOf(saleStatus);
        } catch (IllegalArgumentException e) {
            throw  new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 판매 상태입니다: " + saleStatus);
        }
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.equals("latest")) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        if (sort.equals("priceAsc")) {
            return Sort.by(Sort.Direction.ASC, "price");
        }
        if (sort.equals("priceDesc")) {
            return Sort.by(Sort.Direction.DESC, "price");
        }
        throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 정렬 조건입니다: " +sort);
    }


    @Transactional(propagation = Propagation.MANDATORY)
    public Map<Long, ProductForOrderResponse> validateAndDecreaseStocks(
            Map<Long, Integer> orderQuantities
    ) {
        if (orderQuantities == null || orderQuantities.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "주문할 상품이 없습니다.");
        }

        Set<Long> productIds = new LinkedHashSet<>(orderQuantities.keySet());

        // 실제 Product 행을 비관적 락으로 조회
        List<Product> products = productRepository.findAllByIdInForUpdate(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        Function.identity()
                ));

        // 요청한 상품 중 존재하지 않는 상품이 있는지 확인
        if (!productMap.keySet().equals(productIds)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        List<Long> sortedProductIds = productIds.stream().sorted().toList();

        // 모든 상품의 수량과 재고를 먼저 검증, 아직 재고는 차감하지 않는다.

        for (Long productId : sortedProductIds) {
            Integer quantity = orderQuantities.get(productId);
            Product product = productMap.get(productId);

            if (quantity == null || quantity <= 0) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "주문 수량은 1개 이상이어야 합니다.");
            }

            if (product.getStock() < quantity) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, product.getName() + "의 재고가 부족합니다.");
            }
        }

        // 재고 차감 전 상품명·가격·재고 스냅샷을 만든다.
        Map<Long, ProductForOrderResponse> snapshots = new LinkedHashMap<>();

        for (Long productId : sortedProductIds) {
            Product product = productMap.get(productId);
            snapshots.put(productId, ProductForOrderResponse.from(product));
        }

        // 모든 검증이 성공한 다음 실제 재고를 차감
        for (Long productId : sortedProductIds) {
            Product product = productMap.get(productId);
            Integer quantity = orderQuantities.get(productId);

            product.decreaseStock(quantity);
        }

        return Collections.unmodifiableMap(snapshots);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    public Map<Long, ProductForOrderResponse> getProductsForOrder(Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Product> productMap =
                productRepository.findAllById(productIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Product::getId,
                                Function.identity()
                        ));

        // 요청한 상품 중 하나라도 존재하지 않으면 주문 미리보기 전체를 실패시킨다.
        if (!productMap.keySet().equals(productIds)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Map<Long, ProductForOrderResponse> result = new LinkedHashMap<>();

        productIds.stream()
                .sorted()
                .forEach(productId ->
                        result.put(
                                productId,
                                ProductForOrderResponse.from(
                                        productMap.get(productId)
                                )
                        )
                );

        return Collections.unmodifiableMap(result);
    }
      
    @Transactional
    public void deductStock(Long productId, int quantity) {
        validateQuantity(quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        int updateedRows = productRepository.decreaseStock(productId, quantity);
        if (updateedRows == 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }
    }

    @Transactional
    public void increaseStock(Long productId, int quantity) {
        validateQuantity(quantity);

        productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        productRepository.increaseStock(productId, quantity);
    }

    @Transactional(readOnly = true)
    public Product getProductEntity(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "수량은 1 이상이어야 합니다.");
        }
    }
}
