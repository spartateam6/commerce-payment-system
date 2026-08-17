package io.github.spartateam6.commercepaymentsystem.domain.order.integration;

import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.response.CartResponse;
import io.github.spartateam6.commercepaymentsystem.domain.cart.service.CartService;
import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.member.repository.MemberRepository;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.product.dto.response.ProductResponse;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import io.github.spartateam6.commercepaymentsystem.domain.product.repository.ProductRepository;
import io.github.spartateam6.commercepaymentsystem.domain.product.service.ProductService;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderIntegrationService {

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ProductService productService;

    private final Map<Long, MutableStock> mockStocks = new ConcurrentHashMap<>();
    private final Map<Long, PaymentInformation> mockPayments = new ConcurrentHashMap<>();
    private final AtomicLong paymentSequence = new AtomicLong(1L);

    /**
     * MemberService에는 엔티티를 반환하는 메서드가 아직 없으므로,
     * 기존 MemberRepository 조회 기능으로 실제 영속 엔티티를 가져온다.
     */
    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * 기존 CartService#getCart를 사용한다. 이 메서드 내부에서 회원 존재와
     * 회원 소유 장바구니가 이미 검증된다.
     */
    public List<CartItemForOrder> getCartItems(
            Long memberId,
            Set<Long> requestedCartItemIds
    ) {
        CartResponse cart = cartService.getCart(memberId);

        return cart.items()
                .stream()
                .filter(item -> requestedCartItemIds.isEmpty()
                        || requestedCartItemIds.contains(item.cartItemId()))
                .map(item -> new CartItemForOrder(
                        item.cartItemId(),
                        item.productId(),
                        item.quantity()
                ))
                .toList();
    }

    /**
     * 미리보기용 상품 조회. 재고는 변경하지 않는다.
     */
    public synchronized Map<Long, ProductForOrder> getProducts(Set<Long> productIds) {
        return loadProducts(productIds);
    }

    /**
     * 상품 도메인에 아직 없는 주문용 재고 예약 Mock이다.
     * 모든 상품을 먼저 검증한 뒤 하나도 부족하지 않을 때만 전체 차감한다.
     */
    public synchronized Map<Long, ProductForOrder> validateAndDecreaseStocks(
            Map<Long, Integer> orderQuantities
    ) {
        ensureTransactionActive();

        List<Long> sortedProductIds = orderQuantities.keySet()
                .stream()
                .sorted()
                .toList();

        Map<Long, ProductForOrder> products = loadProducts(Set.copyOf(sortedProductIds));

        if (!products.keySet().equals(orderQuantities.keySet())) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_ITEM_SELECTION);
        }

        for (Long productId : sortedProductIds) {
            Integer quantity = orderQuantities.get(productId);
            ProductForOrder product = products.get(productId);

            if (quantity == null || quantity <= 0) {
                throw new BusinessException(ErrorCode.INVALID_ORDER_ITEM_SELECTION);
            }

            if (product.stock() < quantity) {
                throw new BusinessException(
                        ErrorCode.ORDER_STOCK_INSUFFICIENT,
                        product.productName() + "의 재고가 부족합니다."
                );
            }
        }

        for (Long productId : sortedProductIds) {
            mockStocks.get(productId).decrease(orderQuantities.get(productId));
        }

        registerRollbackAction(() -> {
            synchronized (OrderIntegrationService.this) {
                orderQuantities.forEach((productId, quantity) -> {
                    MutableStock stock = mockStocks.get(productId);
                    if (stock != null) {
                        stock.increase(quantity);
                    }
                });
            }
        });

        /*
         * 차감 전 스냅샷을 반환한다. 주문 금액과 재고 검증은 주문 시점 값을
         * 사용하고, 이후 조회에서는 mockStocks의 차감된 재고가 사용된다.
         */
        return products;
    }

    /**
     * 결제 도메인에 아직 없는 결제 대기 생성 Mock이다.
     */
    public void createWaitingPayment(Order order, Integer amount) {
        ensureTransactionActive();

        if (mockPayments.containsKey(order.getId())) {
            throw new IllegalStateException("이미 결제 대기 정보가 존재하는 주문입니다.");
        }

        PaymentInformation payment = new PaymentInformation(
                paymentSequence.getAndIncrement(),
                order.getId(),
                amount,
                "PENDING",
                null,
                LocalDateTime.now()
        );

        mockPayments.put(order.getId(), payment);
        registerRollbackAction(() -> mockPayments.remove(order.getId(), payment));
    }

    /**
     * 결제 도메인에 아직 없는 주문별 결제 조회 Mock이다.
     */
    public Optional<PaymentInformation> getPayment(Long orderId) {
        return Optional.ofNullable(mockPayments.get(orderId));
    }

    private Map<Long, ProductForOrder> loadProducts(Set<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Product> entityMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Map<Long, ProductForOrder> result = new LinkedHashMap<>();

        productIds.stream()
                .sorted(Comparator.naturalOrder())
                .forEach(productId -> {
                    Product product = entityMap.get(productId);

                    if (product == null) {
                        throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
                    }

                    MutableStock stock = mockStocks.computeIfAbsent(
                            productId,
                            ignored -> new MutableStock(product.getStock())
                    );

                    result.put(
                            productId,
                            new ProductForOrder(
                                    product,
                                    productId,
                                    product.getName(),
                                    product.getPrice(),
                                    stock.value()
                            )
                    );
                });

        return Collections.unmodifiableMap(result);
    }

    private void ensureTransactionActive() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("활성화된 주문 트랜잭션이 없습니다.");
        }
    }

    private void registerRollbackAction(Runnable rollbackAction) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            rollbackAction.run();
                        }
                    }
                }
        );
    }

    public record CartItemForOrder(
            Long cartItemId,
            Long productId,
            Integer quantity
    ) {
    }

    public record ProductForOrder(
            Product product,
            Long productId,
            String productName,
            Integer unitPrice,
            Integer stock
    ) {
    }

    public record PaymentInformation(
            Long paymentId,
            Long orderId,
            Integer amount,
            String status,
            LocalDateTime completedAt,
            LocalDateTime createdAt
    ) {
    }

    private static final class MutableStock {

        private int value;

        private MutableStock(int value) {
            this.value = value;
        }

        private int value() {
            return value;
        }

        private void decrease(int quantity) {
            value -= quantity;
        }

        private void increase(int quantity) {
            value += quantity;
        }
    }
}
