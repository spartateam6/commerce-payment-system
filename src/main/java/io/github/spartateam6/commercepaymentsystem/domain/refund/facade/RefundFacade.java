package io.github.spartateam6.commercepaymentsystem.domain.refund.facade;

import io.github.spartateam6.commercepaymentsystem.domain.payment.port.PaymentGateway;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundRequest;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundResponse;
import io.github.spartateam6.commercepaymentsystem.domain.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefundFacade {

    private final RefundService refundService;
    private final PaymentGateway paymentGateway;

    public RefundResponse refund(Long memberId, RefundRequest request) {
        RefundService.RefundResult result = refundService.process(memberId, request);

        if (result.pgRefundAmount() == 0) {
            return result.response();
        }

        try {
            paymentGateway.cancelPayment(
                    result.portonePaymentId(),
                    result.cancelReason(),
                    result.pgRefundAmount().longValue()
            );
        } catch (RuntimeException exception) {
            refundService.markFailed(result.response().refundId());
            throw exception;
        }

        return result.response();
    }
}
