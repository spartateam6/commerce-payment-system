package io.github.spartateam6.commercepaymentsystem.domain.point.service;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.member.repository.MemberRepository;
import io.github.spartateam6.commercepaymentsystem.domain.payment.entity.Payment;
import io.github.spartateam6.commercepaymentsystem.domain.point.dto.PointBalanceResponse;
import io.github.spartateam6.commercepaymentsystem.domain.point.dto.PointTransactionResponse;
import io.github.spartateam6.commercepaymentsystem.domain.point.entity.PointTransaction;
import io.github.spartateam6.commercepaymentsystem.domain.point.entity.PointTransactionType;
import io.github.spartateam6.commercepaymentsystem.domain.point.repository.PointTransactionRepository;
import io.github.spartateam6.commercepaymentsystem.domain.product.dto.response.PageResponse;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private static final int EARN_RATE_PERCENT = 1;

    private static final List<PointTransactionType> PAYMENT_TYPES =
            List.of(PointTransactionType.USE, PointTransactionType.EARN);

    private static final List<PointTransactionType> REFUND_TYPES =
            List.of(PointTransactionType.USE_RESTORE, PointTransactionType.EARN_REVOKE);

    private final MemberRepository memberRepository;
    private final PointTransactionRepository pointTransactionRepository;

    public PointBalanceResponse getBalance(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return PointBalanceResponse.from(member.getPointBalance());
    }

    public PageResponse<PointTransactionResponse> getTransactions(Long memberId, Pageable pageable) {
        Page<PointTransaction> transactionPage = pointTransactionRepository.findByMember_Id(memberId, pageable);

        List<PointTransactionResponse> content = transactionPage.getContent()
                .stream()
                .map(PointTransactionResponse::from)
                .toList();
        return PageResponse.of(content, transactionPage);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void applyPaymentPoint(Long memberId, Payment payment, Integer usedPoint, Integer pgAmount) {
        int used = usedPoint == null ? 0 : usedPoint;
        if (used < 0) {
            throw new BusinessException(ErrorCode.INVALID_POINT_AMOUNT);
        }
        Member member = getMemberForUpdate(memberId);
        if (hasPaymentTransactions(memberId, payment.getId())) {
            return;
        }
        if (member.getPointBalance() < used) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
        }
        int earned = pgAmount == null ? 0 : pgAmount * EARN_RATE_PERCENT/100;

        saveTransaction(member, payment, PointTransactionType.USE, used);
        saveTransaction(member, payment, PointTransactionType.EARN, earned);

        member.changePoint(earned - used);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void applyRefundPoint(Long memberId, Payment payment) {
        Member member = getMemberForUpdate(memberId);
        if (hasRefundTransactions(memberId, payment.getId())) {
            return;
        }

        int restoreAmount =
                getTransactionAmount(memberId, payment.getId(), PointTransactionType.USE);
        int revokeAmount =
                getTransactionAmount(memberId, payment.getId(), PointTransactionType.EARN);

        saveTransaction(member, payment, PointTransactionType.USE_RESTORE, restoreAmount);
        saveTransaction(member, payment, PointTransactionType.EARN_REVOKE, revokeAmount);

        member.changePoint(restoreAmount - revokeAmount);
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

    private boolean hasPaymentTransactions(Long memberId, Long paymentId) {
        return pointTransactionRepository
                .existsByMember_IdAndPayment_IdAndTransactionTypeIn(
                        memberId,
                        paymentId,
                        PAYMENT_TYPES
                );
    }

    private boolean hasRefundTransactions(Long memberId, Long paymentId) {
        return pointTransactionRepository
                .existsByMember_IdAndPayment_IdAndTransactionTypeIn(
                        memberId,
                        paymentId,
                        REFUND_TYPES
                );
    }

    private int getTransactionAmount(
            Long memberId,
            Long paymentId,
            PointTransactionType transactionType
    ) {
        return pointTransactionRepository
                .findByMember_IdAndPayment_IdAndTransactionType(
                        memberId,
                        paymentId,
                        transactionType
                )
                .map(transaction -> Math.abs(transaction.getAmount()))
                .orElse(0);
    }

}
