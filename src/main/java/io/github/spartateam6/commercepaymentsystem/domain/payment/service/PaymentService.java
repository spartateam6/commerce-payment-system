package io.github.spartateam6.commercepaymentsystem.domain.payment.service;

import io.github.spartateam6.commercepaymentsystem.domain.cart.service.CartService;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderStatus;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderItemService;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderService;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentForOrderResponse;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentRequestDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.PaymentStatus;
import io.github.spartateam6.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import io.github.spartateam6.commercepaymentsystem.domain.point.service.PointService;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final CartService cartService;
    private final PointService pointService;

    public PaymentDto validPayment(Long memberId, PaymentRequestDto paymentRequestDto) {
        Payment payment = paymentRepository.findByOrderNumberWithOrder(paymentRequestDto.orderNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        Order order = orderService.getOrderByOrderNumber(paymentRequestDto.orderNumber(), memberId);

        // 이미 완료된 결제 — Facade에서 idempotent 200 응답 처리
        if (payment.getStatus() == PaymentStatus.PAID && order.getStatus() == OrderStatus.CONFIRMED) {
            return PaymentDto.from(payment);
        }

        // 결제 금액 검증 (프론트에서 넘어온 가격 == DB 가격)
        if (!payment.getOrderAmount().equals(paymentRequestDto.price())) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 주문 상태 검증
        if (payment.getStatus() != PaymentStatus.PENDING || order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        return PaymentDto.from(payment);
    }

    @Transactional
    public void failPayment(String orderNumber) {
        Payment payment = paymentRepository.findByOrderNumberWithOrderLock(orderNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 처리된 결제인지 확인
        if (payment.getStatus() == PaymentStatus.FAILED) {
            return;
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        Order order = payment.getOrder();
        payment.changeStatus(PaymentStatus.FAILED);
        order.updateStatus(OrderStatus.CANCELLED);
        // 차감한 재고 복구
        orderItemService.restoreOrderProductStock(order.getId());
        paymentRepository.save(payment);
    }

    @Transactional
    public void successPayment(Long memberId, String orderNumber, Long pgAmount) {
        Payment payment = paymentRepository.findByOrderNumberWithOrderLock(orderNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 처리된 결제인지 확인
        if (payment.getStatus() == PaymentStatus.PAID) {
            return;
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        Order order = payment.getOrder();
        payment.changeStatus(PaymentStatus.PAID, pgAmount.intValue());
        order.updateStatus(OrderStatus.CONFIRMED);

        pointService.applyPaymentPoint(
                memberId,
                payment,
                order.getPointUsedAmount(),
                payment.getPgAmount()
        );

        List<Long> purchasedCartItemIds = orderItemService.getCartItemIds(order.getId());
        cartService.deletePurchasedItems(memberId, purchasedCartItemIds);
        paymentRepository.save(payment);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void cancelPendingByOrder(String orderNumber) {
        Payment payment = paymentRepository.findByOrderNumberWithOrderLock(orderNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.changeStatus(PaymentStatus.FAILED);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Payment createPendingPayment(Order order) {
        if (paymentRepository.existsByOrder_Id(order.getId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        Payment payment = Payment.builder()
                .order(order)
                .orderAmount(order.getTotalAmount())
                .pgAmount(order.getTotalAmount() - order.getPointUsedAmount())
                .status(PaymentStatus.PENDING)
                .build();

        return paymentRepository.save(payment);
    }


    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    public Optional<PaymentForOrderResponse> findByOrderId(Long orderId) {
        return paymentRepository.findByOrder_Id(orderId).map(PaymentForOrderResponse::from);
    }
}
