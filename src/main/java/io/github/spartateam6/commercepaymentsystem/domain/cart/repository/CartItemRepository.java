package io.github.spartateam6.commercepaymentsystem.domain.cart.repository;

import io.github.spartateam6.commercepaymentsystem.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCart_IdAndProduct_Id(
            Long cartId,
            Long productId
    );
}