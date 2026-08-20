package io.github.spartateam6.commercepaymentsystem.domain.refund.entity;

import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.global.entity.AuditingEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "refund")
public class Refund extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
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

    @Size(max = 30)
    @NotNull
    @Column(name = "status", nullable = false, length = 30)
    private String status;


}