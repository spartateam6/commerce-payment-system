package io.github.spartateam6.commercepaymentsystem.domain.payment.entity;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.entity.AuditingEntity;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "payment")
@AttributeOverride(name = "createdAt", column = @Column(nullable = false))
public class Payment extends AuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @NotNull
    @Column(name = "order_amount", nullable = false)
    private Integer orderAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "portone_payment_id")
    private String portonePaymentId;

    @Column(name = "used_point_amount", nullable = false)
    private Integer usedPointAmount;

    @Column(name = "pg_amount", nullable = false)
    private Integer pgAmount;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    public Payment(
        Order order,
        Integer orderAmount,
        Integer usedPointAmount,
        Integer pgAmount,
        PaymentStatus status
    ) {
        this.order = order;
        this.orderAmount = orderAmount;
        this.usedPointAmount = usedPointAmount;
        this.pgAmount = pgAmount;
        this.status = status;
        this.completedAt = null;
    }

    public void changeStatus(PaymentStatus newStatus) {
        if (!this.status.canTransitTo(newStatus)) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        this.status = newStatus;
        if (newStatus == PaymentStatus.PAID) {
            this.completedAt = LocalDateTime.now();
        }
    }
}