package io.github.spartateam6.commercepaymentsystem.domain.order.facade;

import io.github.spartateam6.commercepaymentsystem.domain.cart.service.CartService;
import io.github.spartateam6.commercepaymentsystem.domain.member.service.MemberService;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderStatus;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderItemService;
import io.github.spartateam6.commercepaymentsystem.domain.order.service.OrderService;
import io.github.spartateam6.commercepaymentsystem.domain.payment.service.PaymentService;
import io.github.spartateam6.commercepaymentsystem.domain.product.service.ProductService;
import io.github.spartateam6.commercepaymentsystem.dummy.MemberFixture;
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

@ExtendWith(MockitoExtension.class)
class OrderFacadeCancelTest {

    @InjectMocks
    private OrderFacade orderFacade;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private MemberService memberService;

    @Mock
    private CartService cartService;

    @Mock
    private ProductService productService;

    @Mock
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 완료 주문은 주문 취소 API로 취소할 수 없다")
    void cancelOrder_결제완료주문_환불요청필요() {
        Order order = Order.create(MemberFixture.members.get(0), "ORD-1", 0);
        order.updateStatus(OrderStatus.CONFIRMED);
        given(orderService.getOrder(1L, 10L)).willReturn(order);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderFacade.cancelOrder(1L, 10L)
        );

        assertEquals(ErrorCode.ORDER_REFUND_REQUIRED, exception.getErrorCode());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        then(paymentService).shouldHaveNoInteractions();
        then(orderItemService).shouldHaveNoInteractions();
    }
}
