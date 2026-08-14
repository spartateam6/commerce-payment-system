package io.github.spartateam6.commercepaymentsystem.domain.cart.repository;

import io.github.spartateam6.commercepaymentsystem.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByMember_Id(Long memberId);
}