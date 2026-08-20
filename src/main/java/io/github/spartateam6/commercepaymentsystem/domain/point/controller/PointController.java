package io.github.spartateam6.commercepaymentsystem.domain.point.controller;

import io.github.spartateam6.commercepaymentsystem.domain.point.dto.PointBalanceResponse;
import io.github.spartateam6.commercepaymentsystem.domain.point.dto.PointTransactionResponse;
import io.github.spartateam6.commercepaymentsystem.domain.point.service.PointService;
import io.github.spartateam6.commercepaymentsystem.domain.product.dto.response.PageResponse;
import io.github.spartateam6.commercepaymentsystem.global.annotation.MemberId;
import io.github.spartateam6.commercepaymentsystem.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping
    public ApiResponse<PointBalanceResponse> getBalance(@MemberId Long memberId) {
        return ApiResponse.ok(pointService.getBalance(memberId));
    }

    @GetMapping("/transactions")
    public ApiResponse<PageResponse<PointTransactionResponse>> getTransactions(
            @MemberId Long memberId, @Parameter(hidden = true) @PageableDefault(
                    size = 20,
                    sort = {"createdAt", "id"},
                    direction = Sort.Direction.DESC
            ) Pageable pageable
            ) {
        return ApiResponse.ok(pointService.getTransactions(memberId, pageable));

    }
}
