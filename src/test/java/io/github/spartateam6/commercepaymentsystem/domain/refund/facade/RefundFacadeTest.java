package io.github.spartateam6.commercepaymentsystem.domain.refund.facade;

import io.github.spartateam6.commercepaymentsystem.domain.payment.port.PaymentGateway;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundRequest;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundResponse;
import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.RefundStatus;
import io.github.spartateam6.commercepaymentsystem.domain.refund.service.RefundService;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class RefundFacadeTest {

    @InjectMocks
    private RefundFacade refundFacade;

    @Mock
    private RefundService refundService;

    @Mock
    private PaymentGateway paymentGateway;

    @Test
    @DisplayName("PG 결제 금액이 있으면 PortOne 취소를 요청한다")
    void refund_PG환불_성공() {
        RefundRequest request = new RefundRequest(10L, "단순 변심");
        RefundResponse response = new RefundResponse(100L, RefundStatus.COMPLETED, 20_000, 10_000);
        given(refundService.process(1L, request)).willReturn(
                new RefundService.RefundResult(response, "payment-1", 20_000, "단순 변심")
        );

        RefundResponse result = refundFacade.refund(1L, request);

        assertEquals(response, result);
        then(paymentGateway).should().cancelPayment("payment-1", "단순 변심", 20_000L);
    }

    @Test
    @DisplayName("포인트 전액 결제 환불은 PortOne 취소를 호출하지 않는다")
    void refund_포인트전액결제_성공() {
        RefundRequest request = new RefundRequest(10L, "단순 변심");
        RefundResponse response = new RefundResponse(100L, RefundStatus.COMPLETED, 0, 30_000);
        given(refundService.process(1L, request)).willReturn(
                new RefundService.RefundResult(response, "payment-1", 0, "단순 변심")
        );

        RefundResponse result = refundFacade.refund(1L, request);

        assertEquals(response, result);
        then(paymentGateway).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("PortOne 취소가 실패하면 환불 상태를 실패로 변경하고 예외를 전달한다")
    void refund_PG환불실패_상태변경() {
        RefundRequest request = new RefundRequest(10L, "단순 변심");
        RefundResponse response = new RefundResponse(100L, RefundStatus.COMPLETED, 30_000, 0);
        given(refundService.process(1L, request)).willReturn(
                new RefundService.RefundResult(response, "payment-1", 30_000, "단순 변심")
        );
        willThrow(new BusinessException(ErrorCode.PAYMENT_GATEWAY_ERROR))
                .given(paymentGateway)
                .cancelPayment("payment-1", "단순 변심", 30_000L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> refundFacade.refund(1L, request)
        );

        assertEquals(ErrorCode.PAYMENT_GATEWAY_ERROR, exception.getErrorCode());
        then(refundService).should().markFailed(100L);
    }
}
