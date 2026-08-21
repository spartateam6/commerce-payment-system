package io.github.spartateam6.commercepaymentsystem.infra.portone.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * PortOne V2 결제 취소 요청 (POST /payments/{paymentId}/cancel)
 *
 * amount를 생략하면 전액 취소, 지정하면 부분 취소.
 * 가이드의 부분 환불이 이 파라미터 하나로 구현된다.
 * (@JsonInclude(NON_NULL) 덕분에 전액 취소일 때는 amount 필드 자체가 전송되지 않는다)
 *
 * 대표상점 연동이므로 storeId는 보내지 않는다.
 *
 * 주요 생략 필드:
 * - taxFreeAmount : 취소 금액 중 면세 금액 (미입력 시 전액 과세 취소)
 * - vatAmount     : 취소 금액 중 부가세액 (미입력 시 자동 계산)
 * - requester     : 결제 취소 요청 주체 (CUSTOMER / ADMIN)
 * - currentCancellableAmount : 취소 가능 잔액 (입력 시 잔액이 일치할 때만 취소 진행)
 * - refundAccount : 환불 계좌 정보
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PortOneCancelRequest(
        String reason,   // [필수] 취소 사유
        Long amount      // [선택] 미입력 시 전액 취소
) {}
