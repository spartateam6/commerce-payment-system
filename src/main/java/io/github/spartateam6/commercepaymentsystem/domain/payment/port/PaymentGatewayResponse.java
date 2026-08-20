package io.github.spartateam6.commercepaymentsystem.domain.payment.port;

public record PaymentGatewayResponse(
        String paymentId,
        GatewayStatus status,
        Long totalAmount
) {
    public boolean isPaid() {
        return status == GatewayStatus.PAID;
    }
}
