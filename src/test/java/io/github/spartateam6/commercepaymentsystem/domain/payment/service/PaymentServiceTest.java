package io.github.spartateam6.commercepaymentsystem.domain.payment.service;

import io.github.spartateam6.commercepaymentsystem.domain.cart.service.CartService;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderStatus;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderItemService;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderService;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentCancelRequestDto;
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
        Order order = Order.create(MemberFixture.members.get(0), ORDER_NUMBER);
        ReflectionTestUtils.setField(order, "totalAmount", totalAmount);
        if (status != OrderStatus.PAYMENT_PENDING) {
            ReflectionTestUtils.setField(order, "status", status);
        }
        return order;
    }

    @Nested
    @DisplayName("결제 요청 (requestPayment)")
    class RequestPayment {

        @Test
        @DisplayName("결제 정보가 없으면 PAYMENT_NOT_FOUND 예외가 발생한다")
        void requestPayment_결제정보없음_예외발생() {
            // given
            PaymentRequestDto dto = new PaymentRequestDto(ORDER_NUMBER);
            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.empty());

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> paymentService.requestPayment(memberId, dto));
            assertEquals(ErrorCode.PAYMENT_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("결제자와 주문자가 다르면 PAYMENT_NOT_MATCH_ORDER 예외가 발생한다")
        void requestPayment_소유권불일치_예외발생() {
            // given
            Payment payment = PaymentFixture.createPayment();
            Order order = createOrder(OrderStatus.PAYMENT_PENDING, 30000);
            PaymentRequestDto dto = new PaymentRequestDto(ORDER_NUMBER);

            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.of(payment));
            given(orderService.getOrderByOrderNumber(ORDER_NUMBER, 2L)).willReturn(order);

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> paymentService.requestPayment(2L, dto));
            assertEquals(ErrorCode.PAYMENT_NOT_MATCH_ORDER, ex.getErrorCode());
        }

        @Test
        @DisplayName("결제 금액이 주문 금액과 다르면 PAYMENT_AMOUNT_MISMATCH 예외가 발생한다")
        void requestPayment_금액불일치_예외발생() {
            // given
            Payment payment = PaymentFixture.createPayment(); // amount = 30000
            Order order = createOrder(OrderStatus.PAYMENT_PENDING, 99999); // totalAmount 불일치
            PaymentRequestDto dto = new PaymentRequestDto(ORDER_NUMBER);

            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.of(payment));
            given(orderService.getOrderByOrderNumber(ORDER_NUMBER, memberId)).willReturn(order);

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> paymentService.requestPayment(memberId, dto));
            assertEquals(ErrorCode.PAYMENT_AMOUNT_MISMATCH, ex.getErrorCode());
        }

        @Test
        @DisplayName("이미 처리된 결제이면 ALREADY_PROCESSED_PAYMENT 예외가 발생한다")
        void requestPayment_이미처리된결제_예외발생() {
            // given
            Payment payment = PaymentFixture.createPaymentWithStatus(PaymentStatus.PAID);
            Order order = createOrder(OrderStatus.CONFIRMED, 30000);
            PaymentRequestDto dto = new PaymentRequestDto(ORDER_NUMBER);

            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.of(payment));
            given(orderService.getOrderByOrderNumber(ORDER_NUMBER, memberId)).willReturn(order);

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> paymentService.requestPayment(memberId, dto));
            assertEquals(ErrorCode.ALREADY_PROCESSED_PAYMENT, ex.getErrorCode());
        }

        @Test
        @DisplayName("결제 상태가 PENDING이고 주문 정보가 일치하면 결제에 성공한다")
        void requestPayment_성공() {
            // given
            Payment payment = PaymentFixture.createPayment(); // PENDING, amount=30000
            Order order = createOrder(OrderStatus.PAYMENT_PENDING, 30000);
            PaymentRequestDto dto = new PaymentRequestDto(ORDER_NUMBER);

            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.of(payment));
            given(orderService.getOrderByOrderNumber(ORDER_NUMBER, memberId)).willReturn(order);
            willDoNothing().given(cartService).clearCart(memberId);

            // when
            boolean result = paymentService.requestPayment(memberId, dto);

            // then
            assertTrue(result);
            assertEquals(PaymentStatus.PAID, payment.getStatus());
            assertNotNull(payment.getCompletedAt());
            assertEquals(OrderStatus.CONFIRMED, order.getStatus());
            then(cartService).should().clearCart(memberId);
            then(paymentRepository).should().save(payment);
        }
    }

    @Nested
    @DisplayName("결제 취소 (cancelPayment)")
    class CancelPayment {

        @Test
        @DisplayName("결제 정보가 없으면 PAYMENT_NOT_FOUND 예외가 발생한다")
        void cancelPayment_결제정보없음_예외발생() {
            // given
            PaymentCancelRequestDto dto = new PaymentCancelRequestDto(ORDER_NUMBER);
            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.empty());

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> paymentService.cancelPayment(memberId, dto));
            assertEquals(ErrorCode.PAYMENT_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("다른 회원의 주문을 취소하면 PAYMENT_NOT_MATCH_ORDER 예외가 발생한다")
        void cancelPayment_소유권불일치_예외발생() {
            // given — 주문은 member(id=1)의 것, 요청자는 id=2
            Payment payment = PaymentFixture.createPayment();
            Order order = createOrder(OrderStatus.PAYMENT_PENDING, 30000);
            PaymentCancelRequestDto dto = new PaymentCancelRequestDto(ORDER_NUMBER);

            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.of(payment));
            given(orderService.getOrderByOrderNumber(ORDER_NUMBER, 2L)).willReturn(order);

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> paymentService.cancelPayment(2L, dto));
            assertEquals(ErrorCode.PAYMENT_NOT_MATCH_ORDER, ex.getErrorCode());
        }

        @Test
        @DisplayName("이미 취소된 주문이면 ALREADY_PROCESSED_REFUND 예외가 발생한다")
        void cancelPayment_이미취소된주문_예외발생() {
            // given
            Payment payment = PaymentFixture.createPaymentWithStatus(PaymentStatus.FAILED);
            Order order = createOrder(OrderStatus.CANCELLED, 30000);
            PaymentCancelRequestDto dto = new PaymentCancelRequestDto(ORDER_NUMBER);

            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.of(payment));
            given(orderService.getOrderByOrderNumber(ORDER_NUMBER, memberId)).willReturn(order);

            // when & then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> paymentService.cancelPayment(memberId, dto));
            assertEquals(ErrorCode.ALREADY_PROCESSED_REFUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("결제 전 취소 시 Payment 상태가 FAILED로, Order 상태가 CANCELLED로 변경된다")
        void cancelPayment_결제전취소_성공() {
            // given
            Payment payment = PaymentFixture.createPaymentWithStatus(PaymentStatus.PENDING);
            Order order = createOrder(OrderStatus.PAYMENT_PENDING, 30000);
            PaymentCancelRequestDto dto = new PaymentCancelRequestDto(ORDER_NUMBER);

            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.of(payment));
            given(orderService.getOrderByOrderNumber(ORDER_NUMBER, memberId)).willReturn(order);
            willDoNothing().given(orderItemService).restoreOrderProductStock(any());

            // when
            assertDoesNotThrow(() -> paymentService.cancelPayment(memberId, dto));

            // then
            assertEquals(PaymentStatus.FAILED, payment.getStatus());
            assertEquals(OrderStatus.CANCELLED, order.getStatus());
            then(orderItemService).should().restoreOrderProductStock(any());
        }

        @Test
        @DisplayName("결제 완료 후 취소(환불) 시 Payment 상태가 REFUND로, Order 상태가 CANCELLED로 변경된다")
        void cancelPayment_결제후환불_성공() {
            // given
            Payment payment = PaymentFixture.createPaymentWithStatus(PaymentStatus.PAID);
            Order order = createOrder(OrderStatus.CONFIRMED, 30000);
            PaymentCancelRequestDto dto = new PaymentCancelRequestDto(ORDER_NUMBER);

            given(paymentRepository.findByOrderNumberWithOrder(ORDER_NUMBER)).willReturn(Optional.of(payment));
            given(orderService.getOrderByOrderNumber(ORDER_NUMBER, memberId)).willReturn(order);
            willDoNothing().given(orderItemService).restoreOrderProductStock(any());

            // when
            assertDoesNotThrow(() -> paymentService.cancelPayment(memberId, dto));

            // then
            assertEquals(PaymentStatus.REFUND, payment.getStatus());
            assertEquals(OrderStatus.CANCELLED, order.getStatus());
            then(orderItemService).should().restoreOrderProductStock(any());
        }
    }
}