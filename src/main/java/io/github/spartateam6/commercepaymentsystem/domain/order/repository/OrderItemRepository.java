package io.github.spartateam6.commercepaymentsystem.domain.order.repository;

import io.github.spartateam6.commercepaymentsystem.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @EntityGraph(attributePaths = "product")
    List<OrderItem> findByOrder_Id(Long orderId);

    @Query("""
            SELECT oi.cartItemId
            FROM OrderItem oi
            WHERE oi.order.id = :orderId
              AND oi.cartItemId IS NOT NULL
            """)
    List<Long> findCartItemIdsByOrderId(@Param("orderId") Long orderId);
}
