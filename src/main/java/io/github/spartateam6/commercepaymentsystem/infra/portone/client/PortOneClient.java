package io.github.spartateam6.commercepaymentsystem.infra.portone.client;

import io.github.spartateam6.commercepaymentsystem.domain.payment.port.PaymentGateway;
import io.github.spartateam6.commercepaymentsystem.domain.payment.port.PaymentGatewayResponse;
import io.github.spartateam6.commercepaymentsystem.infra.portone.dto.PortOneCancelRequest;
import io.github.spartateam6.commercepaymentsystem.infra.portone.dto.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * infrastructure 계층
 * 대표상점으로만 연동하므로 storeId는 보내지 않는다 (하위상점을 쓸 때만 필요)
 */
@Component
@RequiredArgsConstructor
public class PortOneClient implements PaymentGateway {

    private final RestClient portOneRestClient;

    @Override
    public PaymentGatewayResponse getPayment(String paymentId) {
        PortOnePaymentResponse response = portOneRestClient.get()
                .uri("/payments/{paymentId}", paymentId)
                .retrieve()
                .body(PortOnePaymentResponse.class);

        // PortOne JSON을 그대로 흘리지 않고 우리 언어의 DTO로 번역해서 반환한다
        return response.toDomain();
    }

    /** amount가 null이면 전액 취소, 값이 있으면 부분 취소 */
    @Override
    public void cancelPayment(String paymentId, String reason, Long amount) {
        portOneRestClient.post()
                .uri("/payments/{paymentId}/cancel", paymentId)
                .body(new PortOneCancelRequest(reason, amount))
                .retrieve()
                .toBodilessEntity();
    }

}
