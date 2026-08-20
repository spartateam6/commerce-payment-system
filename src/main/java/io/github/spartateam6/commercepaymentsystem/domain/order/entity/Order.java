package io.github.spartateam6.commercepaymentsystem.domain.order.entity;
import io.github.spartateam6.commercepaymentsystem.domain.member.entity.Member;
import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode;
import io.github.spartateam6.commercepaymentsystem.global.entity.AuditingEntity;
import io.github.spartateam6.commercepaymentsystem.global.exception.BusinessException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static io.github.spartateam6.commercepaymentsystem.global.constant.ErrorCode.*;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(
        name = "orders",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_orders_order_number",
                columnNames = "order_number"
        ),
        indexes = @Index(
                name = "idx_orders_member_created_at",
                columnList = "member_id, created_at"
        )
)
public class Order extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_orders_member")
    )
    private Member member;

    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "point_used_amount", nullable = false, columnDefinition = "integer default 0")
    private Integer pointUsedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private List<OrderItem> orderItems = new ArrayList<>();

    public static Order create(Member member, String orderNumber, Integer pointUsedAmount) {
        if (member == null) {
            throw new BusinessException(ORDER_MEMBER_ID_REQUIRED);
        }

        if (orderNumber == null || orderNumber.isBlank()) {
            throw new BusinessException(ORDER_NUMBER_REQUIRED);
        }

        if (pointUsedAmount == null || pointUsedAmount < 0) {
            throw new BusinessException(INVALID_POINT_USAGE);
        }

        Order order = new Order();
        order.member = member;
        order.orderNumber = orderNumber;
        order.totalAmount = 0;
        order.pointUsedAmount = pointUsedAmount;
        order.status = OrderStatus.PAYMENT_PENDING;
        return order;
    }

    public void addOrderItem(
            Product product,
            String productName,
            Integer unitPrice,
            Integer quantity
    ) {
        OrderItem orderItem = OrderItem.create(
                this,
                product,
                productName,
                unitPrice,
                quantity
        );

        orderItems.add(orderItem);
        totalAmount += orderItem.calculateLineAmount();
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

    public void updateStatus(OrderStatus target) {
        if (target == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "변경할 주문 상태는 필수입니다.");
        }

        if (status != target && !status.canTransitTo(target)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "주문 상태를 " + status + "에서 " + target + "으로 변경할 수 없습니다."
            );
        }

        status = target;
    }

    public Integer calculatePgPaymentAmount() {
        return totalAmount - pointUsedAmount;
    }
}
