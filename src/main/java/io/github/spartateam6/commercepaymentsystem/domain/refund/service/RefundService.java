package io.github.spartateam6.commercepaymentsystem.domain.refund.service;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderStatus;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderItemService;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.PaymentStatus;
import io.github.spartateam6.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import io.github.spartateam6.commercepaymentsystem.domain.point.service.PointService;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundRequest;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundResponse;
import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.Refund;
import io.github.spartateam6.commercepaymentsystem.domain.refund.repository.RefundRepository;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PointService pointService;
    private final OrderItemService orderItemService;

    @Transactional
    public RefundResult process(Long memberId, RefundRequest request) {
        Payment payment = paymentRepository.findByIdWithOrderAndMemberLock(request.paymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        Order order = payment.getOrder();

        if (!order.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        if (refundRepository.existsByPayment_Id(payment.getId())
                || payment.getStatus() == PaymentStatus.REFUND) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_REFUND);
        }
        if (payment.getStatus() != PaymentStatus.PAID
                || order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PAID);
        }

        Integer pointRefundAmount = order.getPointUsedAmount();
        Integer pgRefundAmount = payment.getPgAmount();
        if (pointRefundAmount == null || pointRefundAmount < 0
                || pgRefundAmount == null || pgRefundAmount < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "환불 금액 정보가 올바르지 않습니다.");
        }
        if (pgRefundAmount > 0
                && (payment.getPortonePaymentId() == null || payment.getPortonePaymentId().isBlank())) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_MATCH_ORDER);
        }

        Refund refund = refundRepository.save(Refund.complete(
                payment,
                request.cancelReason(),
                pointRefundAmount,
                pgRefundAmount
        ));

        pointService.applyRefundPoint(memberId, payment);
        orderItemService.restoreOrderProductStock(order.getId());
        payment.changeStatus(PaymentStatus.REFUND);
        order.updateStatus(OrderStatus.CANCELLED);

        return new RefundResult(
                RefundResponse.from(refund),
                payment.getPortonePaymentId(),
                pgRefundAmount,
                request.cancelReason()
        );
    }

    @Transactional
    public void markFailed(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFUND_NOT_FOUND));
        refund.fail();
    }

    public record RefundResult(
            RefundResponse response,
            String portonePaymentId,
            Integer pgRefundAmount,
            String cancelReason
    ) {
    }
}
