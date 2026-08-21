package io.github.spartateam6.commercepaymentsystem.domain.payment.facade;

import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentConfirmResponseDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentRequestDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.port.PaymentGateway;
import io.github.spartateam6.commercepaymentsystem.domain.payment.port.PaymentGatewayResponse;
import io.github.spartateam6.commercepaymentsystem.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 이 클래스에서는 @Transactional 절대 금지 <br>
 * 외부 API 호출을 트랜잭션 안에서 하면 커넥션을 오래 점유하고, 롤백해도 PG 상태는 되돌릴 수 없기 때문..
 * DB 변경은 전부 @Transactional 이 걸린 PaymentService 로 위임한다
 */
@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentService paymentService;
    private final PaymentGateway paymentGateway;

    public PaymentConfirmResponseDto confirmPayment(
            Long memberId,
            PaymentRequestDto paymentRequestDto
    ) {
        PaymentDto paymentDto = paymentService.validPayment(memberId, paymentRequestDto);

        PaymentGatewayResponse pgPayment = paymentGateway.getPayment(paymentDto.portonePaymentId());

        // 가격 검증
        boolean isPaid = pgPayment.isPaid();
        boolean priceOk =
                pgPayment.totalAmount() != null &&
                        pgPayment.totalAmount().equals(paymentDto.orderAmount().longValue());

        if (!isPaid || !priceOk) {
            paymentService.failPayment(paymentRequestDto.orderNumber());
            return PaymentConfirmResponseDto.fail();
        }

        paymentService.successPayment(memberId, paymentRequestDto.orderNumber(), pgPayment.totalAmount());
        return PaymentConfirmResponseDto.success(paymentDto);
    }

}
