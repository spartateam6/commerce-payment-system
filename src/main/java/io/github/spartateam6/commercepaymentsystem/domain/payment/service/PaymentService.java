package io.github.spartateam6.commercepaymentsystem.domain.payment.service;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
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

    private void validatePayment(Member member, Payment payment, Order order, Integer totalPrice) {
        // 주문자와 결제 정보 일치하는지?
        if (!order.getMember().getId().equals(member.getId())) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_MATCH_ORDER);
        }

        // 결제 금액 검증
        // Order DB 값 = 결제할 가격
        // front 에서 넘어온 가격 = 결제할 가격
        if (!payment.getAmount().equals(order.getPriceTotal()) || !payment.getAmount().equals(totalPrice)) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    @Transactional
    public boolean requestPayment(Member member, Long paymentId, PaymentRequestDto paymentRequestDto) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        Order order = MockData.getOrder(paymentRequestDto.orderNumber()); // TODO: OrderDTO 로 받을 수 있게 (orderitem, order join fetch 로)
                                                                          // order.id, orderitem.quantity, ...

        validatePayment(member, payment, order, paymentRequestDto.totalPrice());

        // 주문 상태 검증
        if (!payment.getStatus().equals(PaymentStatus.PENDING)) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        // TODO: 결제 요청 로직 (Portone)
        // ...
        boolean paymentSuccess = MockData.processPortone();

        // 결제 실패
        if (!paymentSuccess) {
            payment.setStatus(PaymentStatus.FAILED);
            // 차감한 재고 복구
            // MockData.restoreStock(order.getProduct().getId(), order.getQuantity());
            return false;
        }

        payment.paySuccess();
        return true;
    }

    @Transactional
    public void cancelPayment(Member member, Long paymentId, PaymentCancelRequestDto paymentCancelRequestDto) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        Order order = MockData.getOrder(paymentCancelRequestDto.orderNumber());

        if (!order.getMember().getId().equals(member.getId())) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_MATCH_ORDER);
        }

        switch (payment.getStatus()) {
            case PENDING -> {
                // 주문 취소
                order.setStatus("CANCELED"); // TODO: OrderStatus Enum 으로 변경
                // 재고 복구
                // MockData.restoreStock(order.getProduct().getId(), order.getQuantity());
            }
            case REFUND, FAILED -> {
                throw new BusinessException(ErrorCode.ALREADY_PROCESSED_REFUND);
            }
            case PAID -> {
                boolean refundSuccess = MockData.refundPortone();

                if (!refundSuccess) { // TODO: 환불 실패 방어로직이 약함. 실 결제 Portone 붙일 때 추후 webhook 으로 처리
                    throw new BusinessException(ErrorCode.PAYMENT_NOT_PAID);
                }

                payment.setStatus(PaymentStatus.REFUND);
                order.setStatus("CANCELED"); // TODO: OrderStatus Enum 으로 변경
                // 재고 복구
                // MockData.restoreStock(order.getProduct().getId(), order.getQuantity());
            }
        }
    }

    // TODO: replace Mock data to real data
    static class MockData {
        private static Order getOrder(String orderNumber) {
            return new Order();
        }

        // 재고 복구
        private static void restoreStock(Long productId, Integer stock) {
            //...
        }

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
