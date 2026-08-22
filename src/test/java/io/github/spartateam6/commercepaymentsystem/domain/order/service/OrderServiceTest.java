package io.github.spartateam6.commercepaymentsystem.domain.order.service;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.order.repository.OrderRepository;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 생성 시 선택한 장바구니 항목 ID를 주문 상품에 저장한다")
    void createOrder_장바구니항목ID_저장() {
        Member member = mock(Member.class);
        Product product = mock(Product.class);
        OrderService.CreateOrderItem item = new OrderService.CreateOrderItem(
                10L,
                product,
                "상품",
                10_000,
                2
        );
        given(orderRepository.save(any(Order.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.createOrder(
                member,
                "ORD-TEST-1",
                0,
                List.of(item)
        );

        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getOrderItems().get(0).getCartItemId()).isEqualTo(10L);
    }
}
