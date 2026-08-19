package io.github.spartateam6.commercepaymentsystem.domain.order.repository;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @NonNull
    @EntityGraph(attributePaths = {"orderItems"})
    Optional<Order> findById(@NonNull Long id);

    @EntityGraph(attributePaths = {"member", "orderItems"})
    Optional<Order> findByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = {"orderItems"})
    List<Order> findAllByMember_Id(Long memberId, Pageable pageable);

}
