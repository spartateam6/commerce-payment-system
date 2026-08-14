package io.github.spartateam6.commercepaymentsystem.domain.order.dto;

import jakarta.validation.constraints.Positive;

import java.util.List;

public record OrderPreviewRequest(


        // 목록이 비어 있으면 로그인 회원의 장바구니 전체를 조회한다.
        List<@Positive(message = "장바구니 상품 ID는 양수여야 합니다.") Long> cartItemIds

) {
    public OrderPreviewRequest {
        // 요청에서 cartItemIds를 생략하거나 null로 보내도 빈 목록으로 통일한다.
        cartItemIds = cartItemIds == null
                ? List.of()
                : List.copyOf(cartItemIds);
    }
}