package io.github.spartateam6.commercepaymentsystem.domain.refund.entity;

import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.dummy.PaymentFixture;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RefundTest {

    @Test
    @DisplayName("환불 완료 정보를 생성한다")
    void complete_성공() {
        Payment payment = PaymentFixture.createPayment();

        Refund refund = Refund.complete(payment, "단순 변심", 10_000, 20_000);

        assertEquals(payment, refund.getPayment());
        assertEquals("단순 변심", refund.getCancelReason());
        assertEquals(10_000, refund.getPointRefundAmount());
        assertEquals(20_000, refund.getPgRefundAmount());
        assertEquals(RefundStatus.COMPLETED, refund.getStatus());
    }

    @Test
    @DisplayName("PG 취소에 실패하면 환불 상태를 실패로 변경한다")
    void fail_성공() {
        Refund refund = Refund.complete(PaymentFixture.createPayment(), "단순 변심", 0, 30_000);

        refund.fail();

        assertEquals(RefundStatus.FAILED, refund.getStatus());
    }

    @Test
    @DisplayName("취소 사유가 비어 있으면 환불 정보를 생성할 수 없다")
    void complete_취소사유없음_예외발생() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> Refund.complete(PaymentFixture.createPayment(), " ", 0, 30_000)
        );

        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    @DisplayName("환불 금액이 음수이면 환불 정보를 생성할 수 없다")
    void complete_음수환불금액_예외발생() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> Refund.complete(PaymentFixture.createPayment(), "단순 변심", -1, 30_000)
        );

        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }
}
