package io.github.spartateam6.commercepaymentsystem.domain.order.facade;

import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.response.CartItemForOrderResponse;
import io.github.spartateam6.commercepaymentsystem.domain.cart.dto.response.CartResponse;
import io.github.spartateam6.commercepaymentsystem.domain.cart.service.CartService;
import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.member.service.MemberService;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderCreateRequest;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderCreateResponse;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderDetailResponse;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderPreviewRequest;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderPreviewResponse;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderStatus;
import io.github.spartateam6.commercepaymentsystem.domain.order.integration.OrderIntegrationService;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.PaymentStatus;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderItemService;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderService;
import io.github.spartateam6.commercepaymentsystem.domain.payment.service.PaymentService;
import io.github.spartateam6.commercepaymentsystem.domain.product.dto.response.ProductForOrderResponse;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import io.github.spartateam6.commercepaymentsystem.domain.product.service.ProductService;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class OrderFacade {

    private static final DateTimeFormatter ORDER_NUMBER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderService orderService;
    private final OrderIntegrationService integrationService;
    private final PaymentService paymentService;
    private final OrderItemService orderItemService;
    private final MemberService memberService;
    private final CartService cartService;
    private final ProductService productService;

    @Transactional
    public void cancelOrder(Long memberId, Long orderId) {
        Order order = orderService.getOrder(memberId, orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.ALREADY_ORDER_CANCELED);
        }

        PaymentStatus targetPaymentStatus = switch (order.getStatus()) {
            case PAYMENT_PENDING -> PaymentStatus.FAILED;
            case CONFIRMED       -> PaymentStatus.REFUND;
            default -> throw new BusinessException(ErrorCode.ALREADY_ORDER_CANCELED);
        };

        order.updateStatus(OrderStatus.CANCELLED);
        paymentService.cancelByOrder(order.getOrderNumber(), targetPaymentStatus);
        orderItemService.restoreOrderProductStock(orderId);
    }


    @Transactional(readOnly = true)
    public OrderPreviewResponse preview(Long memberId, OrderPreviewRequest request) {
        memberService.getMember(memberId);

        List<CartItemForOrderResponse> cartItems =
                getOwnedCartItems(memberId, request.cartItemIds());

        Set<Long> productIds = cartItems.stream()
                .map(CartItemForOrderResponse::productId)
                .collect(Collectors.toSet());

        PreparedOrder preparedOrder = prepareOrder(
                cartItems,
                productService.getProductsForOrder(productIds)
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


    @Transactional
    public OrderCreateResponse createOrder(Long memberId, OrderCreateRequest request) {
        Member member = memberService.getMember(memberId);

        List<CartItemForOrderResponse> cartItems =
                getOwnedCartItems(memberId, request.cartItemIds());

        Map<Long, Integer> quantities = cartItems.stream()
                .collect(Collectors.toMap(
                        CartItemForOrderResponse::productId,
                        CartItemForOrderResponse::quantity,
                        (first, second) -> {
                            throw new BusinessException(
                                    ErrorCode.DUPLICATE_ORDER_ITEM_SELECTION
                            );
                        },
                        LinkedHashMap::new
                ));

        Map<Long, ProductForOrderResponse> reservedProducts = productService.validateAndDecreaseStocks(quantities);

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
        Order order = orderService.getOrder(memberId, orderId);

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

    private List<CartItemForOrderResponse> getOwnedCartItems(
            Long memberId,
            List<Long> requestedIds
    ) {
        Set<Long> requestedCartItemIds =
                new LinkedHashSet<>(requestedIds);

        if (requestedCartItemIds.size() != requestedIds.size()) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_ORDER_ITEM_SELECTION
            );
        }

        CartResponse cart = cartService.getCart(memberId);

        // CartResponse 안의 items를 꺼내서 주문에서 필요한 CartItemForOrder 목록으로 변환
        List<CartItemForOrderResponse> selectedCartItems =
                cart.items()
                        .stream()
                        .filter(item ->
                                requestedCartItemIds.isEmpty()
                                        || requestedCartItemIds.contains(
                                        item.cartItemId()
                                )
                        )
                        .map(item -> new CartItemForOrderResponse(
                                item.cartItemId(),
                                item.productId(),
                                item.quantity()
                        ))
                        .toList();

        if (selectedCartItems.isEmpty()) {
            if (requestedCartItemIds.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.ORDER_ITEMS_EMPTY
                );
            }

            throw new BusinessException(
                    ErrorCode.INVALID_ORDER_ITEM_SELECTION
            );
        }

        if (!requestedCartItemIds.isEmpty()) {
            Set<Long> foundIds = selectedCartItems.stream()
                    .map(CartItemForOrderResponse::cartItemId)
                    .collect(Collectors.toSet());

            if (!foundIds.equals(requestedCartItemIds)) {
                throw new BusinessException(
                        ErrorCode.INVALID_ORDER_ITEM_SELECTION
                );
            }
        }

        return selectedCartItems;
    }

    private PreparedOrder prepareOrder(
            List<CartItemForOrderResponse> cartItems,
            Map<Long, ProductForOrderResponse> productMap
    ) {
        Set<Long> requestedProductIds = cartItems.stream()
                .map(CartItemForOrderResponse::productId)
                .collect(Collectors.toSet());

        if (!productMap.keySet().equals(requestedProductIds)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_ITEM_SELECTION);
        }

        List<PreparedItem> items = new ArrayList<>();
        int totalAmount = 0;

        for (CartItemForOrderResponse cartItem : cartItems) {
            ProductForOrderResponse product =
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
