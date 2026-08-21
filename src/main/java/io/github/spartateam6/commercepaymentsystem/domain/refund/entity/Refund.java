package io.github.spartateam6.commercepaymentsystem.domain.refund.entity;

import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.global.entity.AuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;

import static io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode.INVALID_INPUT;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "refund",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_refund_payment",
                columnNames = "payment_id"
        )
)
public class Refund extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @NotNull
    @Lob
    @Column(name = "cancel_reason", nullable = false)
    private String cancelReason;

    @NotNull
    @Column(name = "point_refund_amount", nullable = false)
    private Integer pointRefundAmount;

    @NotNull
    @Column(name = "pg_refund_amount", nullable = false)
    private Integer pgRefundAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RefundStatus status;

    public static Refund complete(
            Payment payment,
            String cancelReason,
            Integer pointRefundAmount,
            Integer pgRefundAmount
    ) {
        if (payment == null) {
            throw new BusinessException(INVALID_INPUT, "결제 정보는 필수입니다.");
        }
        if (cancelReason == null || cancelReason.isBlank()) {
            throw new BusinessException(INVALID_INPUT, "취소 사유는 필수입니다.");
        }
        if (pointRefundAmount == null || pointRefundAmount < 0
                || pgRefundAmount == null || pgRefundAmount < 0) {
            throw new BusinessException(INVALID_INPUT, "환불 금액은 0 이상이어야 합니다.");
        }

        Refund refund = new Refund();
        refund.payment = payment;
        refund.cancelReason = cancelReason;
        refund.pointRefundAmount = pointRefundAmount;
        refund.pgRefundAmount = pgRefundAmount;
        refund.status = RefundStatus.COMPLETED;
        return refund;
    }

    public void fail() {
        this.status = RefundStatus.FAILED;
    }


}
