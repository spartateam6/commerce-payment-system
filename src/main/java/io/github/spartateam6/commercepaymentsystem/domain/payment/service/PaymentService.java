package io.github.spartateam6.commercepaymentsystem.domain.payment.service;

import io.github.spartateam6.commercepaymentsystem.domain.cart.service.CartService;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderStatus;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderItemService;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderService;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentRequestDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.PaymentStatus;
import io.github.spartateam6.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final CartService cartService;

    @Transactional
    public boolean requestPayment(Long memberId, PaymentRequestDto paymentRequestDto) {
        Payment payment = getPaymentByOrderNumber(paymentRequestDto.orderNumber());
        Order order = orderService.getOrderByOrderNumber(paymentRequestDto.orderNumber(), memberId);

        validatePayment(memberId, payment, order);

        // 주문 상태 검증
        if (!payment.getStatus().equals(PaymentStatus.PENDING) || !order.getStatus().equals(OrderStatus.PAYMENT_PENDING)) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        // TODO: 결제 요청 로직 (Portone)
        // ...
        boolean paymentSuccess = MockData.processPortone();

        // 결제 실패
        if (!paymentSuccess) {
            payment.changeStatus(PaymentStatus.FAILED);
            // 차감한 재고 복구
            orderItemService.restoreOrderProductStock(order.getId());

            order.updateStatus(OrderStatus.FAILED);

            return false;
        }

        payment.changeStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);

        order.updateStatus(OrderStatus.CONFIRMED);

        cartService.clearCart(memberId);

        return true;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void cancelByOrder(String orderNumber, PaymentStatus target) {
        Payment payment = getPaymentByOrderNumber(orderNumber);
        payment.changeStatus(target);
        if (target == PaymentStatus.REFUND && !MockData.refundPortone()) {
            // TODO: 실 결제 Portone 붙일 때 webhook 으로 처리
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PAID);
        }
    }

    private Payment getPaymentByOrderNumber(String orderNumber) {
        return paymentRepository.findByOrderNumberWithOrder(orderNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    private void validatePayment(
            Long memberId,
            Payment payment,
            Order order
    ) {
        // 주문자와 결제 정보 일치하는지?
        if (!order.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_MATCH_ORDER);
        }

        // 결제 금액 검증
        // Order DB 값 == 결제할 가격 ?
        if (!payment.getAmount().equals(order.getTotalAmount())) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
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
