package io.github.spartateam6.commercepaymentsystem.domain.point.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointTransactionType {

    USE(-1, "사용"),
    EARN(1, "적립"),
    USE_RESTORE(1, "사용복구"),
    EARN_REVOKE(-1, "적립회수");

    private final int sign;
    private final String label;

    public int signed(int amount) {
        return amount * sign;
    }
}
