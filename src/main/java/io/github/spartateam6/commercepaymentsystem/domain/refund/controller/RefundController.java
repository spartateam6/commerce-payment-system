package io.github.spartateam6.commercepaymentsystem.domain.refund.controller;

import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundRequest;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundResponse;
import io.github.spartateam6.commercepaymentsystem.domain.refund.facade.RefundFacade;
import io.github.spartateam6.commercepaymentsystem.global.annotation.MemberId;
import io.github.spartateam6.commercepaymentsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundFacade refundFacade;

    @PostMapping
    public ResponseEntity<ApiResponse<RefundResponse>> refund(
            @MemberId Long memberId,
            @Valid @RequestBody RefundRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(refundFacade.refund(memberId, request)));
    }
}
