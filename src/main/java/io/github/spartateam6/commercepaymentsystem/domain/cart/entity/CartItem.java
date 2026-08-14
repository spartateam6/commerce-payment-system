package io.github.spartateam6.commercepaymentsystem.domain.cart.entity;

import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import io.github.spartateam6.commercepaymentsystem.global.entity.AuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cart_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_items_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    public static CartItem create(
            Cart cart,
            Product product,
            Integer quantity
    ) {
        CartItem cartItem = new CartItem();
        cartItem.cart = cart;
        cartItem.product = product;
        cartItem.quantity = quantity;
        return cartItem;
    }

    public void increaseQuantity(Integer additionalQuantity) {
        this.quantity += additionalQuantity;
    }
}