package io.github.spartateam6.commercepaymentsystem.domain.payment.service;

import io.github.spartateam6.commercepaymentsystem.domain.cart.service.CartService;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderStatus;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderItemService;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderService;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentRequestDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.PaymentStatus;
import io.github.spartateam6.commercepaymentsystem.domain.payment.repository.PaymentRepository;
import io.github.spartateam6.commercepaymentsystem.dummy.MemberFixture;
import io.github.spartateam6.commercepaymentsystem.dummy.PaymentFixture;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private CartService cartService;

    private static final String ORDER_NUMBER = "ORD-20260814-0001";
    private Long memberId;

    @BeforeEach
    void setUp() {
        memberId = 1L;
    }

    private Order createOrder(OrderStatus status, int totalAmount) {
        return createOrder(status, totalAmount, 0);
    }

    private Order createOrder(OrderStatus status, int totalAmount, int pointUsedAmount) {
        Order order = Order.create(MemberFixture.members.get(0), ORDER_NUMBER, pointUsedAmount);
        ReflectionTestUtils.setField(order, "totalAmount", totalAmount);
        if (status != OrderStatus.PAYMENT_PENDING) {
            ReflectionTestUtils.setField(order, "status", status);
        }
        return order;
    }

    @Nested
    @DisplayName("결제 유효성 검사 (validPayment)")
    class ValidPayment {

        @Test
        @DisplayName("결제 정보가 없으면 PAYMENT_NOT_FOUND 예외가 발생한다")
        void validPayment_결제정보없음_예외발생() {
            // given
            PaymentRequestDto dto = new PaymentRequestDto(ORDER_NUMBER, 30000);
            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.empty());

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> paymentService.validPayment(memberId, dto));
            assertEquals(ErrorCode.PAYMENT_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("다른 회원의 주문이면 ORDER_NOT_FOUND 예외가 발생한다")
        void validPayment_소유권불일치_예외발생() {
            // given
            Payment payment = PaymentFixture.createPayment();
            PaymentRequestDto dto = new PaymentRequestDto(ORDER_NUMBER, 30000);

            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.of(payment));
            given(orderService.getOrderByOrderNumber(ORDER_NUMBER, 2L))
                    .willThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND));

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> paymentService.validPayment(2L, dto));
            assertEquals(ErrorCode.ORDER_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("결제 금액이 주문 금액과 다르면 PAYMENT_AMOUNT_MISMATCH 예외가 발생한다")
        void validPayment_금액불일치_예외발생() {
            // given
            Payment payment = PaymentFixture.createPayment(); // amount = 30000
            Order order = createOrder(OrderStatus.PAYMENT_PENDING, 30000);
            PaymentRequestDto dto = new PaymentRequestDto(ORDER_NUMBER, 99999);

            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.of(payment));
            given(orderService.getOrderByOrderNumber(ORDER_NUMBER, memberId)).willReturn(order);

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> paymentService.validPayment(memberId, dto));
            assertEquals(ErrorCode.PAYMENT_AMOUNT_MISMATCH, ex.getErrorCode());
        }

        @Test
        @DisplayName("이미 처리된 결제이면 ALREADY_PROCESSED_PAYMENT 예외가 발생한다")
        void validPayment_이미처리된결제_예외발생() {
            // given
            Payment payment = PaymentFixture.createPaymentWithStatus(PaymentStatus.PAID);
            Order order = createOrder(OrderStatus.CONFIRMED, 30000);
            PaymentRequestDto dto = new PaymentRequestDto(ORDER_NUMBER, 30000);

            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.of(payment));
            given(orderService.getOrderByOrderNumber(ORDER_NUMBER, memberId)).willReturn(order);

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> paymentService.validPayment(memberId, dto));
            assertEquals(ErrorCode.ALREADY_PROCESSED_PAYMENT, ex.getErrorCode());
        }

        @Test
        @DisplayName("결제 상태가 PENDING이고 모든 정보가 일치하면 PaymentDto를 반환한다")
        void validPayment_성공() {
            // given
            Payment payment = PaymentFixture.createPayment(); // PENDING, amount=30000
            Order order = createOrder(OrderStatus.PAYMENT_PENDING, 30000);
            PaymentRequestDto dto = new PaymentRequestDto(ORDER_NUMBER, 30000);

            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.of(payment));
            given(orderService.getOrderByOrderNumber(ORDER_NUMBER, memberId)).willReturn(order);

            // when
            PaymentDto result = paymentService.validPayment(memberId, dto);

            // then
            assertNotNull(result);
            assertEquals(PaymentStatus.PENDING, result.status());
            assertEquals(30000, result.orderAmount());
        }
    }

    @Nested
    @DisplayName("결제 실패 처리 (failPayment)")
    class FailPaymentTest {

        @Test
        @DisplayName("결제 정보가 없으면 PAYMENT_NOT_FOUND 예외가 발생한다")
        void failPayment_결제정보없음_예외발생() {
            // given
            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.empty());

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> paymentService.failPayment(ORDER_NUMBER));
            assertEquals(ErrorCode.PAYMENT_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("결제 실패 시 Payment는 FAILED, Order는 CANCELLED 상태가 되고 재고가 복구된다")
        void failPayment_성공() {
            // given
            Payment payment = PaymentFixture.createPayment(); // PENDING
            Order order = payment.getOrder(); // PAYMENT_PENDING

            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.of(payment));
            willDoNothing().given(orderItemService).restoreOrderProductStock(any());

            // when
            paymentService.failPayment(ORDER_NUMBER);

            // then
            assertEquals(PaymentStatus.FAILED, payment.getStatus());
            assertEquals(OrderStatus.CANCELLED, order.getStatus());
            then(orderItemService).should().restoreOrderProductStock(order.getId());
            then(paymentRepository).should().save(payment);
        }
    }

    @Nested
    @DisplayName("결제 성공 처리 (successPayment)")
    class SuccessPaymentTest {

        @Test
        @DisplayName("결제 정보가 없으면 PAYMENT_NOT_FOUND 예외가 발생한다")
        void successPayment_결제정보없음_예외발생() {
            // given
            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.empty());

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> paymentService.successPayment(memberId, ORDER_NUMBER, 30000L));
            assertEquals(ErrorCode.PAYMENT_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("결제 성공 시 Payment는 PAID, Order는 CONFIRMED 상태가 되고 장바구니가 비워진다")
        void successPayment_성공() {
            // given
            Payment payment = PaymentFixture.createPayment(); // PENDING
            Order order = payment.getOrder(); // PAYMENT_PENDING

            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.of(payment));
            willDoNothing().given(cartService).clearCart(memberId);

            // when
            paymentService.successPayment(memberId, ORDER_NUMBER, 30000L);

            // then
            assertEquals(PaymentStatus.PAID, payment.getStatus());
            assertNotNull(payment.getCompletedAt());
            assertEquals(OrderStatus.CONFIRMED, order.getStatus());
            then(cartService).should().clearCart(memberId);
            then(paymentRepository).should().save(payment);
        }
    }

    @Nested
    @DisplayName("주문별 결제 취소 (cancelByOrder)")
    class CancelByOrder {

        @Test
        @DisplayName("결제 정보가 없으면 PAYMENT_NOT_FOUND 예외가 발생한다")
        void cancelByOrder_결제정보없음_예외발생() {
            // given
            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.empty());

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> paymentService.cancelByOrder(ORDER_NUMBER, PaymentStatus.FAILED));
            assertEquals(ErrorCode.PAYMENT_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("결제 전 취소 시 Payment 상태가 FAILED로 변경된다")
        void cancelByOrder_결제전취소_성공() {
            // given
            Payment payment = PaymentFixture.createPaymentWithStatus(PaymentStatus.PENDING);
            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.of(payment));

            // when
            assertDoesNotThrow(() -> paymentService.cancelByOrder(ORDER_NUMBER, PaymentStatus.FAILED));

            // then
            assertEquals(PaymentStatus.FAILED, payment.getStatus());
        }

        @Test
        @DisplayName("결제 완료 후 취소(환불) 시 Payment 상태가 REFUND로 변경된다")
        void cancelByOrder_결제후환불_성공() {
            // given
            Payment payment = PaymentFixture.createPaymentWithStatus(PaymentStatus.PAID);
            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.of(payment));

            // when
            assertDoesNotThrow(() -> paymentService.cancelByOrder(ORDER_NUMBER, PaymentStatus.REFUND));

            // then
            assertEquals(PaymentStatus.REFUND, payment.getStatus());
        }
    }
}