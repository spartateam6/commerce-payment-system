package io.github.spartateam6.commercepaymentsystem.domain.payment.service;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderStatus;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderItemService;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderService;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentCancelRequestDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentRequestDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.PaymentStatus;
import io.github.spartateam6.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final OrderItemService orderItemService;

    @Transactional
    public boolean requestPayment(Long memberId, PaymentRequestDto paymentRequestDto) {
        Payment payment = getPaymentByOrderNumber(paymentRequestDto.orderNumber());
        Order order = orderService.getOrderByOrderNumber(paymentRequestDto.orderNumber(), memberId);

        validatePayment(memberId, payment, order, paymentRequestDto.totalPrice());

        // 주문 상태 검증
        if (!payment.getStatus().equals(PaymentStatus.PENDING) || !order.getStatus().equals(OrderStatus.PAYMENT_PENDING)) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        // TODO: 결제 요청 로직 (Portone)
        // ...
        boolean paymentSuccess = MockData.processPortone();

        // 결제 실패
        if (!paymentSuccess) {
            payment.setStatus(PaymentStatus.FAILED);
            // 차감한 재고 복구
            orderItemService.restoreOrderProductStock(order.getId());

            order.updateStatus(OrderStatus.FAILED);

            return false;
        }

        payment.paySuccess();
        paymentRepository.save(payment);

        order.updateStatus(OrderStatus.CONFIRMED);

        // TODO: 장바구니 비우기 (CartItem 삭제)

        return true;
    }

    @Transactional
    public void cancelPayment(Long memberId, PaymentCancelRequestDto paymentCancelRequestDto) {
        Payment payment = getPaymentByOrderNumber(paymentCancelRequestDto.orderNumber());
        Order order = orderService.getOrderByOrderNumber(paymentCancelRequestDto.orderNumber(), memberId);

        // 내 주문인지?
        if (!order.getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_MATCH_ORDER);
        }

        // 이미 주문 취소?
        if (order.getStatus().equals(OrderStatus.CANCELLED)) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_REFUND);
        }

        if (
                payment.getStatus().equals(PaymentStatus.PENDING) &&
                order.getStatus().equals(OrderStatus.PAYMENT_PENDING)
        ) {
            // Order = 결제대기, Payment = 대기
            // 결제 전 취소

            // 주문 취소
            order.updateStatus(OrderStatus.CANCELLED);
            // 결제 실패
            payment.setStatus(PaymentStatus.FAILED);
            // 재고 복구
            orderItemService.restoreOrderProductStock(order.getId());
        }

        if (
                payment.getStatus().equals(PaymentStatus.PAID) &&
                order.getStatus().equals(OrderStatus.CONFIRMED)
        ) {
            // Order = 주문완료, Payment = 완료
            // 결제 후 취소 (환불)

            // 주문 취소
            order.updateStatus(OrderStatus.CANCELLED);
            // 결제 취소
            payment.setStatus(PaymentStatus.REFUND);
            // 재고 복구
            orderItemService.restoreOrderProductStock(order.getId());

            // 환불 실행
            boolean refundSuccess = MockData.refundPortone();

            if (!refundSuccess) { // TODO: 환불 실패 방어로직이 약함. 실 결제 Portone 붙일 때 추후 webhook 으로 처리
                throw new BusinessException(ErrorCode.PAYMENT_NOT_PAID);
            }
        }
    }

    private Payment getPaymentByOrderNumber(String orderNumber) {
        Payment payment = paymentRepository.findByOrderNumberWithOrder(orderNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        return payment;
    }

    private void validatePayment(
            Long memberId,
            Payment payment,
            Order order,
            Integer totalPrice
    ) {
        // 주문자와 결제 정보 일치하는지?
        if (!order.getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_MATCH_ORDER);
        }

        // 결제 금액 검증
        // Order DB 값 == 결제할 가격 ?
        // front 에서 넘어온 가격 == 결제할 가격 ?
        if (!payment.getAmount().equals(order.getTotalAmount()) || !payment.getAmount().equals(totalPrice)) {
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
