package io.github.spartateam6.commercepaymentsystem.domain.payment.controller;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentCancelRequestDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.dto.PaymentRequestDto;
import io.github.spartateam6.commercepaymentsystem.domain.payment.service.PaymentService;
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
            @Valid @RequestBody PaymentRequestDto paymentRequestDto
    ) {
        Member mockdataMember = new Member(); // TODO: custom annotation 으로 받아오는 것이 가장 이상적일 듯
        Long mockdataPaymentId = 1L;

        boolean success = paymentService.requestPayment(mockdataMember, mockdataPaymentId, paymentRequestDto);

        if (!success)
            return ApiResponse.error(ErrorCode.PAYMENT_NOT_PAID, new PaymentResponse("fail"));

        return ApiResponse.ok(new PaymentResponse("ok"));
    }

    @PostMapping("/cancel")
    public ApiResponse<Void> cancelPayment(
            @Valid @RequestBody PaymentCancelRequestDto paymentCancelRequestDto
    ) {
        Member mockdataMember = new Member(); // TODO: custom annotation 으로 받아오는 것이 가장 이상적일 듯
        Long mockdataPaymentId = 1L;

        paymentService.cancelPayment(mockdataMember, mockdataPaymentId, paymentCancelRequestDto);
        return ApiResponse.ok();
    }

}
