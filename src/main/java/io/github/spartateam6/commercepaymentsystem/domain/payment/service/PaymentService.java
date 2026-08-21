package io.github.spartateam6.commercepaymentsystem.domain.payment.service;

import io.github.spartateam6.commercepaymentsystem.domain.cart.service.CartService;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderStatus;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderItemService;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderService;
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

    @Transactional
    public void requestPayment(Long memberId, PaymentRequestDto paymentRequestDto) {
        Payment payment = paymentRepository.findByOrderNumberWithOrder(paymentRequestDto.orderNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        Order order = orderService.getOrderByOrderNumber(paymentRequestDto.orderNumber(), memberId);

        // 결제 금액 검증
        // 프론트에서 넘어온 가격 == DB 가격 ?
        if (!payment.getOrderAmount().equals(paymentRequestDto.price())) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 주문 상태 검증
        if (!payment.getStatus().equals(PaymentStatus.PENDING) || !order.getStatus().equals(OrderStatus.PAYMENT_PENDING)) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        // TODO: 결제 요청 로직 (Portone)
        // ...

        // 결제 실패
        if (paymentRequestDto.result() == PaymentRequestDto.PaymentResult.FAIL) {
            payment.changeStatus(PaymentStatus.FAILED);
            // 차감한 재고 복구
            orderItemService.restoreOrderProductStock(order.getId());
            order.updateStatus(OrderStatus.CANCELLED);
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PAID);
        }

        payment.changeStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);
        order.updateStatus(OrderStatus.CONFIRMED);
        cartService.clearCart(memberId);

        pointService.applyPaymentPoint(
                memberId,
                payment,
                payment.getUsedPointAmount(),
                payment.getPgAmount()
        );

        cartService.clearCart(memberId);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void cancelByOrder(String orderNumber, PaymentStatus target) {
        Payment payment = paymentRepository.findByOrderNumberWithOrder(orderNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.changeStatus(target);
        if (target == PaymentStatus.REFUND && !MockData.refundPortone()) {
            // TODO: 실 결제 Portone 붙일 때 webhook 으로 처리
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PAID);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void createPendingPayment(Order order) {
        if (paymentRepository.existsByOrder_Id(order.getId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        Payment payment = Payment.builder()
                .order(order)
                .orderAmount(order.getTotalAmount())
                .usedPointAmount(order.getPointUsedAmount())
                .pgAmount(order.getTotalAmount() - order.getPointUsedAmount())
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);
    }


    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    public Optional<PaymentForOrderResponse> findByOrderId(Long orderId) {
        return paymentRepository.findByOrder_Id(orderId).map(PaymentForOrderResponse::from);
    }


    // TODO: replace Mock data to real data
    static class MockData {
        // TODO: Portone 을 추가할 때 webhook 으로 오류처리 (재시도 및 알림)
        // ...
        private static boolean processPortone() {
            return true;
        }

        private static boolean refundPortone() {
            return true;
        }
    }

}
