package io.github.spartateam6.commercepaymentsystem.domain.order.entity;
import io.github.spartateam6.commercepaymentsystem.global.entity.AuditingEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(
                        name = "idx_order_items_order_id",
                        columnList = "order_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_items_order"))
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name_snapshot", nullable = false, length = 200)
    private String productNameSnapshot;

    @Column(name = "unit_price_snapshot", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPriceSnapshot;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    static OrderItem create(
            Order order,
            Long productId,
            String productName,
            BigDecimal unitPrice,
            Integer quantity
    ) {
        if (order == null) {
            throw new IllegalArgumentException(
                    "주문은 필수입니다."
            );
        }

        if (productId == null) {
            throw new IllegalArgumentException(
                    "상품 ID는 필수입니다."
            );
        }

        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException(
                    "상품명은 필수입니다."
            );
        }

        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "상품 가격은 0 이상이어야 합니다."
            );
        }

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "주문 수량은 1개 이상이어야 합니다."
            );
        }

        OrderItem orderItem = new OrderItem();
        orderItem.order = order;
        orderItem.productId = productId;
        orderItem.productNameSnapshot = productName;
        orderItem.unitPriceSnapshot = unitPrice;
        orderItem.quantity = quantity;

        return orderItem;
    }

    public BigDecimal calculateLineAmount() {
        return unitPriceSnapshot.multiply(
                BigDecimal.valueOf(quantity)
        );
    }
}