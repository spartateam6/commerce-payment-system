package io.github.spartateam6.commercepaymentsystem.domain.payment.controller;

import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentConfirmResponseDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentRequestDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.facade.PaymentFacade;
import io.github.spartateam6.commercepaymentsystem.global.annotation.MemberId;
import io.github.spartateam6.commercepaymentsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PostMapping("/confirm")
    public ApiResponse<PaymentConfirmResponseDto> confirmPayment(
            @Valid @RequestBody PaymentRequestDto paymentRequestDto,
            @MemberId Long memberId
    ) {
        return ApiResponse.ok(paymentFacade.confirmPayment(memberId, paymentRequestDto));
    }

}
