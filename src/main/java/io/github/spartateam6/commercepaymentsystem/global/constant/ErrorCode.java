package io.github.spartateam6.commercepaymentsystem.global.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "COMMON_003", "접근 권한이 없습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "MEMBER_002", "이미 존재하는 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "MEMBER_003", "이메일 또는 비밀번호가 올바르지 않습니다."),


    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_001", "상품을 찾을 수 없습니다."),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "PRODUCT_002", "재고가 부족합니다."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "PRODUCT_003", "가격은 0 이상이어야 합니다."),
    INVALID_STOCK(HttpStatus.BAD_REQUEST, "PRODUCT_004", "재고는 0 이상이어야 합니다."),

    // Cart
    CART_EMPTY(HttpStatus.BAD_REQUEST, "CART_001", "장바구니가 비어있습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_002", "장바구니 항목을 찾을 수 없습니다."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "CART_003", "수량은 1 이상이어야 합니다."),

    // Order

    ORDER_ITEMS_EMPTY(HttpStatus.BAD_REQUEST, "ORDER_001", "주문할 상품이 없습니다."),
    INVALID_ORDER_ITEM_SELECTION(HttpStatus.BAD_REQUEST, "ORDER_002", "주문할 수 없는 상품이 포함되어 있습니다."),
    DUPLICATE_ORDER_ITEM_SELECTION(HttpStatus.BAD_REQUEST, "ORDER_003", "주문 대상 상품이 중복 선택되었습니다."),
    ORDER_STOCK_INSUFFICIENT(HttpStatus.CONFLICT, "ORDER_005", "주문 상품의 재고가 부족합니다."),
    ORDER_MEMBER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "ORDER_006", "회원 ID는 필수입니다."),
    ORDER_NUMBER_REQUIRED(HttpStatus.BAD_REQUEST, "ORDER_007", "주문번호는 필수입니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_008", "주문을 찾을 수 없습니다."),
    ORDER_PAYMENT_INFORMATION_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "ORDER_009", "주문의 결제 정보를 확인할 수 없습니다."),
    ALREADY_ORDER_CANCELED(HttpStatus.CONFLICT, "ORDER_010", "이미 취소된 주문입니다."),


    // Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_001", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT_002", "결제 금액이 일치하지 않습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "PAYMENT_003", "유효하지 않은 결제 상태 변경입니다."),
    PAYMENT_NOT_PAID(HttpStatus.BAD_REQUEST, "PAYMENT_004", "PG사 결제가 완료되지 않았습니다."),
    ALREADY_PROCESSED_PAYMENT(HttpStatus.CONFLICT, "PAYMENT_005", "이미 처리된 결제입니다."),
    PAYMENT_NOT_MATCH_ORDER(HttpStatus.BAD_REQUEST, "PAYMENT_006", "결제 정보가 주문과 일치하지 않습니다."),
    ALREADY_PROCESSED_REFUND(HttpStatus.CONFLICT, "PAYMENT_007", "이미 처리된 환불(취소)요청입니다."),
    PAYMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "PAYMENT_008", "해당 주문의 결제 정보가 이미 존재합니다."),
    PAYMENT_GATEWAY_ERROR(HttpStatus.BAD_GATEWAY, "PAYMENT_008", "PG사 처리 중 오류가 발생했습니다"),

    // Webhook
    INVALID_WEBHOOK_SIGNATURE(HttpStatus.UNAUTHORIZED, "WEBHOOK_001", "웹훅 서명이 유효하지 않습니다."),
    WEBHOOK_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "WEBHOOK_002", "웹훅 이벤트를 찾을 수 없습니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}