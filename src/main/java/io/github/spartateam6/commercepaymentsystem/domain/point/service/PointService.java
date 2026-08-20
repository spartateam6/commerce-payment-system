package io.github.spartateam6.commercepaymentsystem.domain.point.service;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.member.repository.MemberRepository;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.point.entity.PointTransaction;
import io.github.spartateam6.commercepaymentsystem.domain.point.entity.PointTransactionType;
import io.github.spartateam6.commercepaymentsystem.domain.point.repository.PointTransactionRepository;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private static final int EARN_RATE_PERCENT = 1;

    private static final List<PointTransactionType> PAYMENT_TYPES =
            List.of(PointTransactionType.USE, PointTransactionType.EARN);

    private static final List<PointTransactionType> REFUND_TYPES =
            List.of(PointTransactionType.USE_RESTORE, PointTransactionType.EARN_REVOKE);

    private final MemberRepository memberRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void setPayment(Long memberId, Payment payment, Integer usedPointAmount, Integer pgAmount) {
        int use = usedPointAmount == null ? 0 : usedPointAmount;
        if (use < 0) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }
        Member member = getMemberForUpdate(memberId);
        if (pointTransactionRepository.existsByMember_IdAndPayment_IdAndTransactionTypeIn(memberId, payment.getId(), PAYMENT_TYPES)) {
            return;
        }
        if (member.getPointBalance() < use) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
        }
        int earn = pgAmount == null ? 0 : pgAmount * EARN_RATE_PERCENT/100;

        saveTransaction(member, payment, PointTransactionType.USE, use);
        saveTransaction(member, payment, PointTransactionType.EARN, earn);

        member.changePoint(earn - use);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void setRefund(Long memberId, Payment payment) {
        Member member = getMemberForUpdate(memberId);
        if (pointTransactionRepository.existsByMember_IdAndPayment_IdAndTransactionTypeIn(memberId, payment.getId(), REFUND_TYPES)) {
            return;
        }
        int restoreAmount = amount(memberId, payment.getId(), PointTransactionType.USE);
        int revokeAmount = amount(memberId, payment.getId(), PointTransactionType.EARN);
        int revokableAmount = Math.min(revokeAmount, member.getPointBalance() + restoreAmount);

        saveTransaction(member, payment, PointTransactionType.USE_RESTORE, restoreAmount);
        saveTransaction(member, payment, PointTransactionType.EARN_REVOKE, revokableAmount);

        member.changePoint(restoreAmount - revokableAmount);
    }

    private int amount(Long memberId, Long paymentId, PointTransactionType transactionType) {

        return pointTransactionRepository.findByMember_IdAndPayment_IdAndTransactionType(memberId, paymentId, transactionType)
                .map(transaction -> Math.abs(transaction.getAmount()))
                .orElse(0);
    }

    private void saveTransaction(Member member, Payment payment,
                                 PointTransactionType transactionType, int amount) {
        if (amount == 0) {
            return;
        }
        pointTransactionRepository.save(new PointTransaction(member, payment, transactionType, amount));
    }


    private Member getMemberForUpdate(Long memberId) {
        return memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

}
