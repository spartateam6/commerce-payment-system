package io.github.spartateam6.commercepaymentsystem.dummy;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.PaymentStatus;

public class PaymentFixture {

    public static Payment createPayment() {
        return createPaymentWithStatus(PaymentStatus.PENDING);
    }

    public static Payment createPaymentWithStatus(PaymentStatus status) {
        Order order = Order.create(MemberFixture.members.get(0), "ORD-20260814-0001", 0);
        return Payment.builder()
                .order(order)
                .orderAmount(30000)
                .usedPointAmount(0)
                .pgAmount(30000)
                .status(status)
                .build();
    }

}
