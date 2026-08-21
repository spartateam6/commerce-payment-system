package io.github.spartateam6.commercepaymentsystem.domain.payment.facade;

import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentConfirmResponseDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentRequestDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.PaymentStatus;
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

        // 이미 완료된 결제 — PortOne 재조회 없이 idempotent 200 응답
        if (paymentDto.status() == PaymentStatus.PAID) {
            return PaymentConfirmResponseDto.success(paymentDto);
        }

        // 포인트로 전액 결제하면 PG를 안거치도록한다.
        if (paymentDto.pgAmount() == 0){
            paymentService.successPayment(memberId, paymentRequestDto.orderNumber(), 0L);

            return PaymentConfirmResponseDto.success(paymentDto);
        }

        PaymentGatewayResponse pgPayment = paymentGateway.getPayment(paymentDto.portonePaymentId());

        boolean isPaid = pgPayment.isPaid();
        boolean priceOk = pgPayment.totalAmount() != null &&
                pgPayment.totalAmount().equals(paymentDto.pgAmount().longValue());

        if (!isPaid) {
            // PG 미승인 — 실제 카드 승인이 없으므로 보상 취소 없이 바로 실패 처리
            paymentService.failPayment(paymentRequestDto.orderNumber());
            return PaymentConfirmResponseDto.fail();
        }

        if (!priceOk) {
            // PG 승인됐지만 금액 불일치 — 보상 취소 후 실패 처리
            paymentGateway.cancelPayment(paymentDto.portonePaymentId(), "결제 금액 불일치");
            paymentService.failPayment(paymentRequestDto.orderNumber());
            return PaymentConfirmResponseDto.fail();
        }

        paymentService.successPayment(memberId, paymentRequestDto.orderNumber(), pgPayment.totalAmount());
        return PaymentConfirmResponseDto.success(paymentDto);
    }

}
