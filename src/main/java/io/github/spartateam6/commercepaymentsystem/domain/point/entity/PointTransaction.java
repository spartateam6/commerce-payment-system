package io.github.spartateam6.commercepaymentsystem.domain.point.entity;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.global.entity.AuditingEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "point_transactions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_point_transactions_member_payment_type",
                columnNames = {"member_id", "payment_id", "transaction_type"}
        ),
        indexes = @Index(name = "idx_point_transactions_member_created", columnList = "member_id, created_at")
)
public class PointTransaction extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 결제와 무관한 초기 지급/이벤트 지급 등의 포인트 거래도 원장에 기록할 수 있도록 nullable.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(name = "transaction_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PointTransactionType transactionType;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    public PointTransaction(
            Member member,
            Payment payment,
            PointTransactionType transactionType,
            int amount
    ) {
        if (member == null) {
            throw new IllegalArgumentException("회원은 필수입니다.");
        }
        if (transactionType == null) {
            throw new IllegalArgumentException("거래 유형은 필수입니다.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("거래 금액은 0보다 커야 합니다: " + amount);
        }

        this.member = member;
        this.payment = payment;
        this.transactionType = transactionType;
        this.amount = transactionType.signed(amount);
    }

}