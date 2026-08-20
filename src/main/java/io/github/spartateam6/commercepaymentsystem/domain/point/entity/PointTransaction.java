package io.github.spartateam6.commercepaymentsystem.domain.point.entity;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Size(max = 20)
    @NotNull
    @Column(name = "transaction_type", nullable = false, length = 20)
    private PointTransactionType transactionType;

    @Column(name = "amount", nullable = false)
    private Integer amount;

//    사용하는 쪽에서 어떻게 사용할지 느낌이 안와서 일단은 주석을 달아 두겠습니다.
//    아래와 비슷하게 생성자로 사용하시면 되지만, 주의할 점은 이왕이면
//    N+1 문제가 나지 않도록 memberId, paymentId 등을 활용하거나 fetch join 등을 활용하여 가져올 수 있도록
//    하면 좋을 것 같습니다..
//
//    @Builder
//    public PointTransaction(
//            Member member,
//            Payment payment,
//            PointTransactionType type,
//            int amount
//    ) {
//        if (amount <= 0) {
//            throw new IllegalArgumentException("거래 금액은 0보다 커야 합니다: " + amount);
//        }
//
//        this.member = member;
//        this.payment = payment;
//        this.transactionType = type;
//        this.amount = type.signed(amount);
//    }

}