package io.github.spartateam6.commercepaymentsystem.domain.refund.controller;

import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundRequest;
import io.github.spartateam6.commercepaymentsystem.domain.refund.dto.RefundResponse;
import io.github.spartateam6.commercepaymentsystem.domain.refund.entity.RefundStatus;
import io.github.spartateam6.commercepaymentsystem.domain.refund.facade.RefundFacade;
import io.github.spartateam6.commercepaymentsystem.global.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RefundControllerTest {

    @InjectMocks
    private RefundController refundController;

    @Mock
    private RefundFacade refundFacade;

    @Test
    void refund_성공하면_환불결과와_200을_반환한다() {
        RefundRequest request = new RefundRequest(10L, "단순 변심");
        RefundResponse refundResponse =
                new RefundResponse(100L, RefundStatus.COMPLETED, 20_000, 10_000);
        given(refundFacade.refund(1L, request)).willReturn(refundResponse);

        ResponseEntity<ApiResponse<RefundResponse>> response =
                refundController.refund(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(refundResponse);
        then(refundFacade).should().refund(1L, request);
    }
}
