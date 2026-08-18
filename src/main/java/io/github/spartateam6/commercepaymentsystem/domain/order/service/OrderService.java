package io.github.spartateam6.commercepaymentsystem.domain.order.service;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.order.dto.OrderDetailResponse;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.repository.OrderRepository;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    /**
     * Facade의 주문 생성 트랜잭션에 반드시 참여한다.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Order createOrder(
            Member member,
            String orderNumber,
            List<CreateOrderItem> orderItems
    ) {
        Order order = Order.create(member, orderNumber);

        orderItems.forEach(item -> order.addOrderItem(
                item.product(),
                item.productName(),
                item.unitPrice(),
                item.quantity()
        ));

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long memberId, Long orderId) {
        return orderRepository.findByIdAndMember_Id(orderId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Order getOrderByOrderNumber(String orderNumber, Long memberId) {
        return orderRepository.findByOrderNumberAndMember_Id(orderNumber, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<OrderDetailResponse> getOrderList(Long memberId, Pageable pageable) {
        List<Order> orderList = orderRepository.findAllByMember_Id(memberId, pageable);
        return orderList.stream()
                .map(o -> OrderDetailResponse.from(o, null))
                .toList();
    }

    public record CreateOrderItem(
            Product product,
            String productName,
            Integer unitPrice,
            Integer quantity
    ) {
    }
}
