package io.github.spartateam6.commercepaymentsystem.infra.portone.client;

import io.github.spartateam6.commercepaymentsystem.domain.payment.port.PaymentGateway;
import io.github.spartateam6.commercepaymentsystem.domain.payment.port.PaymentGatewayResponse;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import io.github.spartateam6.commercepaymentsystem.infra.portone.dto.PortOneCancelRequest;
import io.github.spartateam6.commercepaymentsystem.infra.portone.dto.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.function.Supplier;

/**
 * infrastructure 계층
 * 대표상점으로만 연동하므로 storeId는 보내지 않는다 (하위상점을 쓸 때만 필요)
 */
@Component
@RequiredArgsConstructor
public class PortOneClient implements PaymentGateway {

    private final RestClient portoneRestClient;

    @Override
    public PaymentGatewayResponse getPayment(String paymentId) {
        PortOnePaymentResponse response = execute(() ->
                portoneRestClient.get()
                        .uri("/payments/{paymentId}", paymentId)
                        .retrieve()
                        .body(PortOnePaymentResponse.class)
        );

        return response.toDomain();
    }

    /** amount가 null이면 전액 취소, 값이 있으면 부분 취소 */
    @Override
    public void cancelPayment(String paymentId, String reason, Long amount) {
        execute(() ->
                portoneRestClient.post()
                        .uri("/payments/{paymentId}/cancel", paymentId)
                        .body(new PortOneCancelRequest(reason, amount))
                        .retrieve()
                        .toBodilessEntity()
        );
    }

    /** 예외처리를 위해 모든 요청은 이 함수를 사용 */
    private <T> T execute (Supplier<T> supplier) {
        T resp;

        try {
            resp = supplier.get();
        } catch (
                HttpClientErrorException |
                HttpServerErrorException |
                ResourceAccessException e
        ) {
            // TODO: 예외처리를 좀 더 상세하게 분류하여 서로 다른 Error 코드를 상세화하면 좋을 듯..
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }

        if (resp == null) {
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }
        return resp;
    }

}
