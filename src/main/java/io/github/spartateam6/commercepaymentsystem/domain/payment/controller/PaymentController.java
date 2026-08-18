package io.github.spartateam6.commercepaymentsystem.domain.payment.controller;

import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentRequestDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.service.PaymentService;
import io.github.spartateam6.commercepaymentsystem.global.annotation.MemberId;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
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

    private final PaymentService paymentService;

    record PaymentResponse(
            String status
    ) {}

    @PostMapping("/confirm")
    public ApiResponse<PaymentResponse> confirmPayment(
            @Valid @RequestBody PaymentRequestDto paymentRequestDto,
            @MemberId Long memberId
    ) {
        boolean success = paymentService.requestPayment(memberId, paymentRequestDto);

        if (!success) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PAID);
        }

        return ApiResponse.ok(new PaymentResponse("ok"));
    }

}
