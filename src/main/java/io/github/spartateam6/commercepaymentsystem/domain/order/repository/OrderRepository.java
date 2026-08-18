package io.github.spartateam6.commercepaymentsystem.domain.order.repository;

import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    Optional<Order> findByIdAndMember(Long orderId, Member member);

    Optional<Order> findByOrderNumberAndMember_Id(String orderNumber, Long memberId);

    List<Order> findAllByMember_Id(Long memberId, Pageable pageable);

    Optional<Order> findByIdAndMember_Id(Long id, Long memberId);
}
