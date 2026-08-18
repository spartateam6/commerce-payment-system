package io.github.spartateam6.commercepaymentsystem.domain.cart.repository;

import io.github.spartateam6.commercepaymentsystem.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCart_IdAndProduct_Id(
            Long cartId,
            Long productId
    );

    Optional<CartItem> findByIdAndCart_Member_Id(
            Long cartItemId,
            Long memberId
    );

    List<CartItem> findAllByCart_Id(
            Long cartId
    );

    void deleteAllByCart_Id(Long cartId);
}
