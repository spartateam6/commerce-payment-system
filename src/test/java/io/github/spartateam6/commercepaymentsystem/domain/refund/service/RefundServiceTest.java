package io.github.spartateam6.commercepaymentsystem.domain.refund.service;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderStatus;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderItemService;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.PaymentStatus;
import io.github.spartateam6.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import io.github.spartateam6.commercepaymentsystem.domain.point.service.PointService;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundRequest;
import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.Refund;
import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.RefundStatus;
import io.github.spartateam6.commercepaymentsystem.domain.refund.repository.RefundRepository;
import io.github.spartateam6.commercepaymentsystem.dummy.PaymentFixture;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @InjectMocks
    private RefundService refundService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PointService pointService;

    @Mock
    private OrderItemService orderItemService;

    private Payment paidPayment;
    private RefundRequest request;

    @BeforeEach
    void setUp() {
        paidPayment = PaymentFixture.createPayment();
        ReflectionTestUtils.setField(paidPayment, "id", 10L);
        ReflectionTestUtils.setField(paidPayment.getOrder(), "id", 20L);
        paidPayment.changeStatus(PaymentStatus.PAID, 30_000);
        paidPayment.getOrder().updateStatus(OrderStatus.CONFIRMED);
        request = new RefundRequest(10L, "단순 변심");
    }

    @Test
    @DisplayName("전액 환불 시 포인트와 재고를 복구하고 주문과 결제 상태를 변경한다")
    void process_성공() {
        given(paymentRepository.findByIdWithOrderAndMemberLock(10L))
                .willReturn(Optional.of(paidPayment));
        given(refundRepository.existsByPayment_Id(10L)).willReturn(false);
        willAnswer(invocation -> {
            Refund refund = invocation.getArgument(0);
            ReflectionTestUtils.setField(refund, "id", 100L);
            return refund;
        }).given(refundRepository).save(org.mockito.ArgumentMatchers.any(Refund.class));

        RefundService.RefundResult result = refundService.process(1L, request);

        assertEquals(100L, result.response().refundId());
        assertEquals(RefundStatus.COMPLETED, result.response().status());
        assertEquals(30_000, result.response().pgRefundAmount());
        assertEquals(0, result.response().pointRefundAmount());
        assertEquals(PaymentStatus.REFUND, paidPayment.getStatus());
        assertEquals(OrderStatus.CANCELLED, paidPayment.getOrder().getStatus());
        then(pointService).should().applyRefundPoint(1L, paidPayment);
        then(orderItemService).should().restoreOrderProductStock(20L);
    }

    @Test
    @DisplayName("다른 회원의 결제는 환불할 수 없다")
    void process_소유권불일치_예외발생() {
        given(paymentRepository.findByIdWithOrderAndMemberLock(10L))
                .willReturn(Optional.of(paidPayment));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> refundService.process(2L, request)
        );

        assertEquals(ErrorCode.FORBIDDEN_ACCESS, exception.getErrorCode());
        then(refundRepository).should(never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("완료되지 않은 결제는 환불할 수 없다")
    void process_결제미완료_예외발생() {
        Payment pendingPayment = PaymentFixture.createPayment();
        ReflectionTestUtils.setField(pendingPayment, "id", 10L);
        given(paymentRepository.findByIdWithOrderAndMemberLock(10L))
                .willReturn(Optional.of(pendingPayment));
        given(refundRepository.existsByPayment_Id(10L)).willReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> refundService.process(1L, request)
        );

        assertEquals(ErrorCode.PAYMENT_NOT_PAID, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미 환불 기록이 있으면 중복 환불할 수 없다")
    void process_중복환불_예외발생() {
        given(paymentRepository.findByIdWithOrderAndMemberLock(10L))
                .willReturn(Optional.of(paidPayment));
        given(refundRepository.existsByPayment_Id(10L)).willReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> refundService.process(1L, request)
        );

        assertEquals(ErrorCode.ALREADY_PROCESSED_REFUND, exception.getErrorCode());
        then(pointService).shouldHaveNoInteractions();
        then(orderItemService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("PG 취소 실패 후 환불 상태를 실패로 변경한다")
    void markFailed_성공() {
        Refund refund = Refund.complete(paidPayment, "단순 변심", 0, 30_000);
        given(refundRepository.findById(100L)).willReturn(Optional.of(refund));

        refundService.markFailed(100L);

        assertEquals(RefundStatus.FAILED, refund.getStatus());
    }
}
