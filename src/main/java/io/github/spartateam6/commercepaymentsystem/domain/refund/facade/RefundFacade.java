package io.github.spartateam6.commercepaymentsystem.domain.refund.facade;

import io.github.spartateam6.commercepaymentsystem.domain.payment.port.PaymentGateway;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundRequest;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundResponse;
import io.github.spartateam6.commercepaymentsystem.domain.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundFacade {

    private final RefundService refundService;
    private final PaymentGateway paymentGateway;

    public RefundResponse refund(Long memberId, RefundRequest request) {
        RefundService.RefundResult result = refundService.process(memberId, request);

        if (result.pgRefundAmount() == 0) {
            log.info("PG없이 포인트 전액 환불 refundId={} paymentId={} pgRefundAmount={} pointRefundAmount={} pgCancelRequired=false",
                    result.response().refundId(), request.paymentId(), result.response().pgRefundAmount(), result.response().pointRefundAmount());

            return result.response();
        }

        try {
            paymentGateway.cancelPayment(
                    result.portonePaymentId(),
                    result.cancelReason(),
                    result.pgRefundAmount().longValue()
            );
            log.info("PG취소 후 환불 완료 refundId={} paymentId={} pgRefundAmount={} pointRefundAmount={} pgCancelRequired=true",
                    result.response().refundId(), request.paymentId(), result.response().pgRefundAmount(), result.response().pointRefundAmount());

        } catch (RuntimeException exception) {

            refundService.markFailed(result.response().refundId());
            log.warn("PG취소 실패 후 실패 처리 refundId={} paymentId={} pgRefundAmount={}",
                    result.response().refundId(), request.paymentId(), result.pgRefundAmount(), exception);

            throw exception;
        }

        return result.response();
    }
}
