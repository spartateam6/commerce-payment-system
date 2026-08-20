package io.github.spartateam6.commercepaymentsystem.infra.portone.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.spartateam6.commercepaymentsystem.domain.payment.port.GatewayStatus;
import io.github.spartateam6.commercepaymentsystem.domain.payment.port.PaymentGatewayResponse;

/**
 * PortOne V2 결제 조회 응답 (GET /payments/{paymentId})
 *
 * 응답 JSON은 필드가 수십 개지만 우리가 쓰는 것은 id · status · amount.total 3개
 * @JsonIgnoreProperties(ignoreUnknown = true) ⇒ 내가 모르는 JSON 필드는 무시하고 버려라.
 * 스펙에 필드가 추가돼도 우리 코드는 흔들리지 않는다.
 *
 * 주요 생략 필드:
 * - transactionId : PortOne 거래 고유 채번 ID
 * - method        : 결제수단 정보
 * - merchantId    : 고객사 ID
 * - customer      : 구매자 정보
 * - channel       : 선택된 채널 정보
 * - webhooks      : 웹훅 발송 내역
 * - requestedAt, updatedAt, statusChangedAt, paidAt : 각 시점 타임스탬프
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOnePaymentResponse(String id, String status, Amount amount) {

    /**
     * amount가 객체로 오기 때문에 중첩 record가 필요하다. Long amount로는 받을 수 없다.
     * @JsonIgnoreProperties는 하위 타입으로 전파되지 않으므로 중첩 record에도 따로 붙인다.
     * (amount 안에도 vat · paid · cancelled처럼 안 쓰는 필드가 있다)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Amount(Long total) {}

    public PaymentGatewayResponse toDomain() {
        return new PaymentGatewayResponse(id, toGatewayStatus(), amount == null ? null : amount.total());
    }

    /** 모르는 상태값이 와도 crash 하지 않도록 UNKNOWN으로 떨어뜨린다 */
    private GatewayStatus toGatewayStatus() {
        try {
            return GatewayStatus.valueOf(status);
        } catch (IllegalArgumentException | NullPointerException e) {
            return GatewayStatus.UNKNOWN;
        }
    }

}
