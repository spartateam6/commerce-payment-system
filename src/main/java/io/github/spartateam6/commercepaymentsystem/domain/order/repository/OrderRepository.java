package io.github.spartateam6.commercepaymentsystem.domain.order.repository;
import io.github.spartateam6.commercepaymentsystem.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
