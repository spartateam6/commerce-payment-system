package io.github.spartateam6.commercepaymentsystem.domain.payment.port;

/** 모르는 값이 오면 예외 대신 UNKNOWN으로 떨어뜨리는 편이 안전하다 */
public enum GatewayStatus {
    READY,
    PENDING,
    PAID,
    FAILED,
    CANCELLED,
    PARTIAL_CANCELLED,
    UNKNOWN
}
