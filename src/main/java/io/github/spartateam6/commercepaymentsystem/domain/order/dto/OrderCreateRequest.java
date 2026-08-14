package io.github.spartateam6.commercepaymentsystem.domain.order.dto;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record OrderCreateRequest(
        List<@Positive(message = "장바구니 상품 ID는 양수여야 합니다.") Long> cartItemIds
) {
    public OrderCreateRequest {
        cartItemIds = cartItemIds == null
                ? List.of()
                : List.copyOf(cartItemIds);
    }
}