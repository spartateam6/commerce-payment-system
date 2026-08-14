package io.github.spartateam6.commercepaymentsystem.domain.order.mock;

import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OrderMockData {

    /*
     * 회원 한 명당 장바구니 한 개
     *
     * Map Key가 memberId이므로 같은 회원 ID에
     * 장바구니 두 개를 저장할 수 없다.
     */
    private static final Map<Long, CartData> CARTS_BY_MEMBER_ID =
            Map.of(
                    1L, new CartData(1001L, 1L),
                    2L, new CartData(1002L, 2L)
            );

    /*
     * CartItem은 회원 ID가 아니라 장바구니 ID를 참조한다.
     */
    private static final List<CartItemData> CART_ITEMS =
            List.of(
                    // 회원 1의 장바구니 1001
                    new CartItemData(
                            1L,
                            1001L,
                            101L,
                            1
                    ),
                    new CartItemData(
                            2L,
                            1001L,
                            102L,
                            2
                    ),
                    new CartItemData(
                            3L,
                            1001L,
                            103L,
                            3
                    ),

                    // 회원 2의 장바구니 1002
                    new CartItemData(
                            4L,
                            1002L,
                            101L,
                            1
                    )
            );

    /*
     * 재고가 차감되어야 하므로 상품 Mock은 mutable 객체로 관리한다.
     */
    private final Map<Long, MockProduct> products =
            new HashMap<>();

    /*
     * 주문별 결제 사전 기록
     */
    private final Map<Long, PaymentData> payments =
            new ConcurrentHashMap<>();

    private final AtomicLong paymentSequence =
            new AtomicLong(1L);

    public OrderMockData() {
        products.put(
                101L,
                new MockProduct(
                        101L,
                        "노트북",
                        new BigDecimal("1500000.00"),
                        10
                )
        );

        products.put(
                102L,
                new MockProduct(
                        102L,
                        "키보드",
                        new BigDecimal("120000.00"),
                        20
                )
        );

        products.put(
                103L,
                new MockProduct(
                        103L,
                        "모니터",
                        new BigDecimal("450000.00"),
                        5
                )
        );
    }

    /*
     * TODO:
     * 장바구니 담당자의 실제 조회 함수가 완성되면
     * 이 메서드 내부만 실제 함수 호출로 교체한다.
     */
    public List<CartItemData> getCartItems(
            Long memberId,
            Set<Long> requestedCartItemIds
    ) {
        CartData cart = CARTS_BY_MEMBER_ID.get(memberId);

        if (cart == null) {
            return List.of();
        }

        return CART_ITEMS.stream()
                /*
                 * 로그인 회원 장바구니의 상품만 조회
                 */
                .filter(cartItem ->
                        cartItem.cartId().equals(cart.cartId())
                )
                /*
                 * 빈 목록이면 장바구니 전체,
                 * 목록이 있으면 선택한 상품만 조회
                 */
                .filter(cartItem ->
                        requestedCartItemIds.isEmpty()
                                || requestedCartItemIds.contains(
                                cartItem.cartItemId()
                        )
                )
                .toList();
    }

    /*
     * TODO:
     * 상품 담당자의 실제 상품 조회 함수가 완성되면
     * 이 메서드 내부만 실제 함수 호출로 교체한다.
     *
     * 미리보기에서 사용한다.
     */
    public synchronized Map<Long, ProductData> getProducts(
            Set<Long> productIds
    ) {
        return productIds.stream()
                .filter(products::containsKey)
                .map(products::get)
                .map(MockProduct::toProductData)
                .collect(Collectors.toUnmodifiableMap(
                        ProductData::productId,
                        Function.identity()
                ));
    }

    /*
     * TODO:
     * 상품 담당자의 실제 재고 차감 함수가 완성되면
     * 이 메서드 내부만 실제 함수 호출로 교체한다.
     *
     * 이 메서드는 다음을 한 번에 수행해야 한다.
     *
     * 1. 모든 상품 조회
     * 2. 모든 재고 검증
     * 3. 검증이 전부 성공한 경우 전체 재고 차감
     * 4. 주문 시점 상품명·가격 반환
     */
    public synchronized Map<Long, ProductData> validateAndDecreaseStocks(
            Map<Long, Integer> orderQuantities
    ) {
        ensureTransactionActive();

        List<Long> sortedProductIds =
                orderQuantities.keySet()
                        .stream()
                        .sorted()
                        .toList();

        /*
         * 1단계: 모든 상품 존재 여부와 재고 검증
         *
         * 이 반복문에서는 아직 재고를 차감하지 않는다.
         */
        for (Long productId : sortedProductIds) {
            MockProduct product = products.get(productId);

            if (product == null) {
                throw new BusinessException(
                        ErrorCode.ORDER_PRODUCT_UNAVAILABLE
                );
            }

            Integer quantity = orderQuantities.get(productId);

            if (quantity == null || quantity <= 0) {
                throw new IllegalStateException(
                        "주문 수량이 올바르지 않습니다."
                );
            }

            if (product.getStock() < quantity) {
                throw new BusinessException(
                        ErrorCode.ORDER_STOCK_INSUFFICIENT,
                        product.getName()
                                + "의 재고가 부족합니다."
                );
            }
        }

        /*
         * 재고 차감 전 상품명·가격·기존 재고 스냅샷
         */
        Map<Long, ProductData> reservedSnapshots =
                sortedProductIds.stream()
                        .map(products::get)
                        .map(MockProduct::toProductData)
                        .collect(Collectors.toMap(
                                ProductData::productId,
                                Function.identity(),
                                (first, second) -> first,
                                LinkedHashMap::new
                        ));

        /*
         * 2단계: 모든 검증 성공 후 전체 재고 차감
         */
        for (Long productId : sortedProductIds) {
            MockProduct product = products.get(productId);
            Integer quantity = orderQuantities.get(productId);

            product.decreaseStock(quantity);
        }

        /*
         * 주문 생성 중 예외가 발생하면
         * 차감한 수량만큼 다시 증가시킨다.
         */
        registerRollbackAction(() -> {
            synchronized (OrderMockData.this) {
                orderQuantities.forEach((productId, quantity) -> {
                    MockProduct product = products.get(productId);

                    if (product != null) {
                        product.increaseStock(quantity);
                    }
                });
            }
        });

        return Collections.unmodifiableMap(
                reservedSnapshots
        );
    }

    /*
     * TODO:
     * 결제 담당자의 결제 사전 기록 함수가 완성되면
     * 이 메서드 내부만 실제 함수 호출로 교체한다.
     *
     * 주문 생성 시점에는 실제 결제 승인을 하지 않는다.
     * WAITING 상태의 결제 기록만 만든다.
     */
    public void createWaitingPayment(
            Long orderId,
            BigDecimal amount
    ) {
        ensureTransactionActive();

        if (payments.containsKey(orderId)) {
            throw new IllegalStateException(
                    "이미 결제 사전 기록이 존재하는 주문입니다."
            );
        }

        PaymentData payment = new PaymentData(
                paymentSequence.getAndIncrement(),
                orderId,
                amount,
                PaymentStatusData.WAITING,
                null,
                LocalDateTime.now()
        );

        payments.put(orderId, payment);

        /*
         * 주문 트랜잭션이 롤백되면
         * Mock 결제 기록도 함께 제거한다.
         */
        registerRollbackAction(() ->
                payments.remove(orderId, payment)
        );
    }

    private void ensureTransactionActive() {
        if (!TransactionSynchronizationManager
                .isSynchronizationActive()) {

            throw new IllegalStateException(
                    "활성화된 주문 트랜잭션이 없습니다."
            );
        }
    }

    private void registerRollbackAction(
            Runnable rollbackAction
    ) {
        TransactionSynchronizationManager
                .registerSynchronization(
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

    public record CartData(
            Long cartId,
            Long memberId
    ) {
    }

    public record CartItemData(
            Long cartItemId,
            Long cartId,
            Long productId,
            Integer quantity
    ) {
    }

    /*
     * 서비스로 반환되는 변경 불가능한 상품 스냅샷
     */
    public record ProductData(
            Long productId,
            String productName,
            BigDecimal price,
            Integer stock
    ) {
    }

    public enum PaymentStatusData {
        WAITING,
        COMPLETED,
        FAILED,
        CANCELED
    }

    public record PaymentData(
            Long paymentId,
            Long orderId,
            BigDecimal amount,
            PaymentStatusData status,
            LocalDateTime completedAt,
            LocalDateTime createdAt
    ) {
    }

    /*
     * Mock 내부에서만 사용하는 변경 가능한 상품 객체
     */
    private static class MockProduct {

        private final Long id;
        private final String name;
        private final BigDecimal price;
        private int stock;

        private MockProduct(
                Long id,
                String name,
                BigDecimal price,
                int stock
        ) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
        }

        public String getName() {
            return name;
        }

        public int getStock() {
            return stock;
        }

        public void decreaseStock(int quantity) {
            stock -= quantity;
        }

        public void increaseStock(int quantity) {
            stock += quantity;
        }

        public ProductData toProductData() {
            return new ProductData(
                    id,
                    name,
                    price,
                    stock
            );
        }
    }
}