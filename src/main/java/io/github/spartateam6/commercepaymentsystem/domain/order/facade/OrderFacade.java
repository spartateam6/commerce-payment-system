package io.github.spartateam6.commercepaymentsystem.domain.order.facade;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderCreateRequest;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderCreateResponse;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderDetailResponse;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderPreviewRequest;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderPreviewResponse;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.integration.OrderIntegrationService;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderService;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderFacade {

    private static final DateTimeFormatter ORDER_NUMBER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderService orderService;
    private final OrderIntegrationService integrationService;

    public OrderFacade(
            OrderService orderService,
            OrderIntegrationService integrationService
    ) {
        this.orderService = orderService;
        this.integrationService = integrationService;
    }

    @Transactional(readOnly = true)
    public OrderPreviewResponse preview(Long memberId, OrderPreviewRequest request) {
        integrationService.getMember(memberId);

        List<OrderIntegrationService.CartItemForOrder> cartItems =
                getOwnedCartItems(memberId, request.cartItemIds());

        Set<Long> productIds = cartItems.stream()
                .map(OrderIntegrationService.CartItemForOrder::productId)
                .collect(Collectors.toSet());

        PreparedOrder preparedOrder = prepareOrder(
                cartItems,
                integrationService.getProducts(productIds)
        );

        List<OrderPreviewResponse.PreviewItem> items = preparedOrder.items()
                .stream()
                .map(item -> new OrderPreviewResponse.PreviewItem(
                        item.cartItemId(),
                        item.product().getId(),
                        item.productName(),
                        item.unitPrice(),
                        item.quantity(),
                        item.lineAmount()
                ))
                .toList();

        return new OrderPreviewResponse(items, preparedOrder.totalAmount());
    }

    /**
     * 주문 생성의 유일한 public 트랜잭션 진입점이다.
     */
    @Transactional
    public OrderCreateResponse createOrder(Long memberId, OrderCreateRequest request) {
        Member member = integrationService.getMember(memberId);

        List<OrderIntegrationService.CartItemForOrder> cartItems =
                getOwnedCartItems(memberId, request.cartItemIds());

        Map<Long, Integer> quantities = cartItems.stream()
                .collect(Collectors.toMap(
                        OrderIntegrationService.CartItemForOrder::productId,
                        OrderIntegrationService.CartItemForOrder::quantity,
                        (first, second) -> {
                            throw new BusinessException(
                                    ErrorCode.DUPLICATE_ORDER_ITEM_SELECTION
                            );
                        },
                        LinkedHashMap::new
                ));

        Map<Long, OrderIntegrationService.ProductForOrder> reservedProducts =
                integrationService.validateAndDecreaseStocks(quantities);

        /* 롤백 검증 시 아래 한 줄의 주석을 잠시 해제한다. */
        // throw new RuntimeException("재고 차감 후 강제 롤백");

        PreparedOrder preparedOrder = prepareOrder(cartItems, reservedProducts);

        List<OrderService.CreateOrderItem> createItems = preparedOrder.items()
                .stream()
                .map(item -> new OrderService.CreateOrderItem(
                        item.product(),
                        item.productName(),
                        item.unitPrice(),
                        item.quantity()
                ))
                .toList();

        Order savedOrder = orderService.createOrder(
                member,
                generateOrderNumber(),
                createItems
        );

        integrationService.createWaitingPayment(
                savedOrder,
                savedOrder.getTotalAmount()
        );

        /* 결제 완료 전이므로 장바구니는 비우지 않는다. */
        return OrderCreateResponse.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long memberId, Long orderId) {
        Member member = integrationService.getMember(memberId);
        Order order = orderService.getOrder(orderId, member);

        OrderIntegrationService.PaymentInformation payment =
                integrationService.getPayment(orderId)
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.ORDER_PAYMENT_INFORMATION_UNAVAILABLE
                        ));

        OrderDetailResponse.PaymentResponse paymentResponse =
                new OrderDetailResponse.PaymentResponse(
                        payment.paymentId(),
                        payment.amount(),
                        payment.status(),
                        payment.completedAt(),
                        payment.createdAt()
                );

        return OrderDetailResponse.from(order, paymentResponse);
    }

    private List<OrderIntegrationService.CartItemForOrder> getOwnedCartItems(
            Long memberId,
            List<Long> requestedIds
    ) {
        Set<Long> requestedCartItemIds = new LinkedHashSet<>(requestedIds);

        if (requestedCartItemIds.size() != requestedIds.size()) {
            throw new BusinessException(ErrorCode.DUPLICATE_ORDER_ITEM_SELECTION);
        }

        List<OrderIntegrationService.CartItemForOrder> cartItems =
                integrationService.getCartItems(memberId, requestedCartItemIds);

        if (cartItems.isEmpty()) {
            if (requestedCartItemIds.isEmpty()) {
                throw new BusinessException(ErrorCode.ORDER_ITEMS_EMPTY);
            }
            throw new BusinessException(ErrorCode.INVALID_ORDER_ITEM_SELECTION);
        }

        if (!requestedCartItemIds.isEmpty()) {
            Set<Long> foundIds = cartItems.stream()
                    .map(OrderIntegrationService.CartItemForOrder::cartItemId)
                    .collect(Collectors.toSet());

            if (!foundIds.equals(requestedCartItemIds)) {
                throw new BusinessException(ErrorCode.INVALID_ORDER_ITEM_SELECTION);
            }
        }

        return cartItems;
    }

    private PreparedOrder prepareOrder(
            List<OrderIntegrationService.CartItemForOrder> cartItems,
            Map<Long, OrderIntegrationService.ProductForOrder> productMap
    ) {
        Set<Long> requestedProductIds = cartItems.stream()
                .map(OrderIntegrationService.CartItemForOrder::productId)
                .collect(Collectors.toSet());

        if (!productMap.keySet().equals(requestedProductIds)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_ITEM_SELECTION);
        }

        List<PreparedItem> items = new ArrayList<>();
        int totalAmount = 0;

        for (OrderIntegrationService.CartItemForOrder cartItem : cartItems) {
            OrderIntegrationService.ProductForOrder product =
                    productMap.get(cartItem.productId());

            if (product.stock() < cartItem.quantity()) {
                throw new BusinessException(
                        ErrorCode.ORDER_STOCK_INSUFFICIENT,
                        product.productName() + "의 재고가 부족합니다."
                );
            }

            int lineAmount = product.unitPrice() * cartItem.quantity();

            items.add(new PreparedItem(
                    cartItem.cartItemId(),
                    product.product(),
                    product.productName(),
                    product.unitPrice(),
                    cartItem.quantity(),
                    lineAmount
            ));

            totalAmount += lineAmount;
        }

        return new PreparedOrder(List.copyOf(items), totalAmount);
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

    private record PreparedItem(
            Long cartItemId,
            Product product,
            String productName,
            Integer unitPrice,
            Integer quantity,
            Integer lineAmount
    ) {
    }

    private record PreparedOrder(
            List<PreparedItem> items,
            Integer totalAmount
    ) {
    }
}
