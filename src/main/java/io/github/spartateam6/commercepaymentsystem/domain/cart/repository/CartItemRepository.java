package io.github.spartateam6.commercepaymentsystem.domain.cart.repository;

import io.github.spartateam6.commercepaymentsystem.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCart_IdAndProduct_Id(
            Long cartId,
            Long productId
    );

    @EntityGraph(attributePaths = "product")
    List<CartItem> findAllByCart_Id(
            Long cartId
    );

    void deleteAllByCart_Id(Long cartId);
}
