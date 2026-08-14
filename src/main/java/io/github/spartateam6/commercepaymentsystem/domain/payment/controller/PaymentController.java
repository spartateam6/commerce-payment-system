package io.github.spartateam6.commercepaymentsystem.domain.payment.controller;

import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentCancelRequestDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentRequestDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.service.PaymentService;
import io.github.spartateam6.commercepaymentsystem.global.annotation.MemberInfo;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
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
            @MemberInfo Long memberId
    ) {
        Long mockdataPaymentId = 1L;

        boolean success = paymentService.requestPayment(memberId, mockdataPaymentId, paymentRequestDto);

        if (!success)
            return ApiResponse.error(ErrorCode.PAYMENT_NOT_PAID, new PaymentResponse("fail"));

        return ApiResponse.ok(new PaymentResponse("ok"));
    }

    @PostMapping("/cancel")
    public ApiResponse<Void> cancelPayment(
            @Valid @RequestBody PaymentCancelRequestDto paymentCancelRequestDto,
            @MemberInfo Long memberId
    ) {
        Long mockdataPaymentId = 1L;

        paymentService.cancelPayment(memberId, mockdataPaymentId, paymentCancelRequestDto);
        return ApiResponse.ok();
    }

}
