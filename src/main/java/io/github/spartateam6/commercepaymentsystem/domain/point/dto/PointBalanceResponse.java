package io.github.spartateam6.commercepaymentsystem.domain.point.dto;

public record PointBalanceResponse(

        Integer balance

) {
    public static PointBalanceResponse from(Integer balance) {

        return new PointBalanceResponse(balance);
    }
}
