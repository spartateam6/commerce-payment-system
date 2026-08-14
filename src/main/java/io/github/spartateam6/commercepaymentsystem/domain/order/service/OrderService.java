package io.github.spartateam6.commercepaymentsystem.domain.order.service;

import io.github.spartateam6.commercepaymentsystem.domain.order.dto.*;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.mock.OrderMockData;
import io.github.spartateam6.commercepaymentsystem.domain.order.repository.OrderRepository;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderService {

    private static final DateTimeFormatter ORDER_NUMBER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderRepository orderRepository;
    private final OrderMockData mockData;

    /*
     * 주문 미리보기
     *
     * 조회·검증·금액 계산만 수행한다.
     * 재고와 DB 데이터는 변경하지 않는다.
     */
    @Transactional(readOnly = true)
    public OrderPreviewResponse preview(Long memberId, OrderPreviewRequest request) {
        // cartItemIds가 빈 목록이면 회원 1번의 장바구니 전체를 가져온다.
        List<OrderMockData.CartItemData> cartItems = getOwnedCartItems(
                        memberId,
                        request.cartItemIds()
                );

        // 상품 ID만 추출
        Set<Long> productIds = cartItems.stream()
                .map(OrderMockData.CartItemData::productId)
                .collect(Collectors.toSet());

        // 상품 정보 일괄 조회
        Map<Long, OrderMockData.ProductData> productMap = mockData.getProducts(productIds);

        // 주문 데이터 준비
        PreparedOrder preparedOrder = prepareOrder(cartItems, productMap);

        // 미리보기 응답 DTO로 변환
        List<OrderPreviewResponse.PreviewItem> previewItems =
                preparedOrder.items()
                        .stream()
                        .map(item ->
                                new OrderPreviewResponse.PreviewItem(
                                        item.cartItemId(),
                                        item.productId(),
                                        item.productName(),
                                        item.unitPrice(),
                                        item.quantity(),
                                        item.lineAmount()
                                )
                        )
                        .toList();

        return new OrderPreviewResponse(previewItems, preparedOrder.totalAmount());
    }


    @Transactional
    public OrderCreateResponse createOrder(Long memberId, OrderCreateRequest request) {

        // 장바구니 상품 조회 + 본인 소유 확인
        List<OrderMockData.CartItemData> cartItems = getOwnedCartItems(memberId, request.cartItemIds());

        // 상품별 주문 수량 Map 생성
        Map<Long, Integer> orderQuantities = cartItems.stream()
                        .collect(Collectors.toMap(
                                OrderMockData.CartItemData::productId,
                                OrderMockData.CartItemData::quantity,
                                (first, second) -> {
                                    throw new IllegalStateException(
                                            "장바구니에 동일 상품이 "
                                                    + "중복 저장되어 있습니다."
                                    );
                                },
                                LinkedHashMap::new //장바구니 상품의 처리 순서를 유지하기 위해 사용
                        ));

        // 모든 상품 검증 후 재고 차감
        Map<Long, OrderMockData.ProductData> reservedProducts = mockData.validateAndDecreaseStocks(orderQuantities);

        // 상품 스냅샷으로 주문 총액 계산
        PreparedOrder preparedOrder = prepareOrder(cartItems, reservedProducts);

        // 주문 생성, 상태 = PAYMENT_PENDING, 주문번호 = 서버 자동 채번
        Order order = Order.create(memberId, generateOrderNumber());

        // 주문 상품 생성 + 주문 시점 상품명·가격 스냅샷 저장
        for (PreparedItem item : preparedOrder.items()) {
            order.addOrderItem(
                    item.productId(),
                    item.productName(),
                    item.unitPrice(),
                    item.quantity()
            );
        }

        // Order의 CascadeType.ALL로 OrderItem도 함께 저장
        Order savedOrder = orderRepository.save(order);

        /*
         * 결제 사전 기록
         * 실제 결제 승인 아님
         * 상태 = WAITING
         * 결제 금액 = 주문 총액
         */
        mockData.createWaitingPayment(
                savedOrder.getId(),
                savedOrder.getTotalAmount()
        );

        return OrderCreateResponse.from(savedOrder);
    }


    // 장바구니 상품 조회와 소유권 검증
    private List<OrderMockData.CartItemData> getOwnedCartItems(Long memberId, List<Long> requestedIds) {

        // Set을 통한 중복 제거
        Set<Long> requestedCartItemIds = new LinkedHashSet<>(requestedIds);

        if (requestedCartItemIds.size() != requestedIds.size()) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_ORDER_ITEM_SELECTION
            );
        }

        // 싱제 장바구니 아이템 조회
        List<OrderMockData.CartItemData> cartItems = mockData.getCartItems(memberId, requestedCartItemIds);

        if (cartItems.isEmpty()) {
            if (requestedCartItemIds.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.ORDER_ITEMS_EMPTY
                );
            }

            throw new BusinessException(
                    ErrorCode.INVALID_ORDER_ITEM_SELECTION
            );
        }

        // 일부만 조회된 경우를 확인하여 “정상 상품만 조용히 일부 주문되는 문제”를 막는다.
        if (!requestedCartItemIds.isEmpty()) {
            Set<Long> foundCartItemIds = cartItems.stream()
                    .map(OrderMockData.CartItemData::cartItemId)
                    .collect(Collectors.toSet());

            if (!foundCartItemIds.equals(requestedCartItemIds)) {
                throw new BusinessException(
                        ErrorCode.INVALID_ORDER_ITEM_SELECTION
                );
            }
        }

        return cartItems;
    }


    // 장바구니 정보와 상품 정보를 합쳐 주문 계산 결과 생성
    private PreparedOrder prepareOrder(
            List<OrderMockData.CartItemData> cartItems,
            Map<Long, OrderMockData.ProductData> productMap
    ) {
        // 주문에 필요한 상품 ID 추출
        Set<Long> requestedProductIds = cartItems.stream()
                        .map(OrderMockData.CartItemData::productId)
                        .collect(Collectors.toSet());

        // 상품 누락 확인
        if (!productMap.keySet().equals(requestedProductIds)) {
            throw new BusinessException(
                    ErrorCode.ORDER_PRODUCT_UNAVAILABLE
            );
        }

        // 상품별 주문 계산 결과
        List<PreparedItem> preparedItems = new ArrayList<>();

        // 전체 주문 금액
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 상품별 재고와 금액 계산
        for (OrderMockData.CartItemData cartItem : cartItems) {
            OrderMockData.ProductData product = productMap.get(cartItem.productId());

            // 재고 확인
            if (product.stock() < cartItem.quantity()) {
                throw new BusinessException(
                        ErrorCode.ORDER_STOCK_INSUFFICIENT,
                        product.productName()
                                + "의 재고가 부족합니다."
                );
            }

            // 상품별 금액 계산
            BigDecimal lineAmount = product.price().multiply(BigDecimal.valueOf(cartItem.quantity()));

            // 내부 계산 객체 생성
            PreparedItem preparedItem =
                    new PreparedItem(
                            cartItem.cartItemId(),
                            product.productId(),
                            product.productName(),
                            product.price(),
                            cartItem.quantity(),
                            lineAmount
                    );

            // 목록과 총액에 반영
            preparedItems.add(preparedItem);
            totalAmount = totalAmount.add(lineAmount);
        }

        // List.copyOf()를 사용해 외부에서 상품 목록을 변경하지 못하게 한다.
        return new PreparedOrder(List.copyOf(preparedItems), totalAmount);
    }


    private String generateOrderNumber() {
        String dateTime = LocalDateTime.now().format(ORDER_NUMBER_DATE_FORMAT);

        String randomValue = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();

        return "ORD-" + dateTime + "-" + randomValue;
    }
}