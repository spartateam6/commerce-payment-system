package io.github.spartateam6.commercepaymentsystem.domain.order.service;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.repository.OrderRepository;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

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

    /**
     * 주문이 없거나 다른 회원의 주문이면 동일하게 ORDER_NOT_FOUND를 반환한다.
     */
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    public Order getOrder(Long orderId, Member member) {
        return orderRepository.findByIdAndMember(orderId, member)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Order getOrderByOrderNumber(String orderNumber, Long memberId) {
        return orderRepository.findByOrderNumberAndMember_Id(orderNumber, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    public record CreateOrderItem(
            Product product,
            String productName,
            BigDecimal unitPrice,
            Integer quantity
    ) {
    }
}
