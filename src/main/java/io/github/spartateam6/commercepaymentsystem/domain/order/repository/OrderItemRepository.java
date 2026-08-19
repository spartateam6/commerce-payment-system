package io.github.spartateam6.commercepaymentsystem.domain.order.repository;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @EntityGraph(attributePaths = "product")
    List<OrderItem> findByOrder_Id(Long orderId);
}